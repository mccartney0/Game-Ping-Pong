package com.foldcut.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.content.FileProvider
import com.foldcut.app.domain.ExportSettings
import com.foldcut.app.domain.ExportState
import com.foldcut.app.domain.Project
import com.foldcut.app.engine.ExportHandle
import com.foldcut.app.engine.Media3ExportEngine
import com.foldcut.app.ui.EditorViewModel
import com.foldcut.app.ui.FoldCutTheme
import com.foldcut.app.ui.HomeViewModel
import com.foldcut.app.ui.screens.EditorScreen
import com.foldcut.app.ui.screens.HomeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@UnstableApi
class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val editorViewModel: EditorViewModel by viewModels()

    private lateinit var player: ExoPlayer
    private lateinit var videoPicker: ActivityResultLauncher<Array<String>>
    private lateinit var audioPicker: ActivityResultLauncher<Array<String>>
    private lateinit var exportEngine: Media3ExportEngine
    private var exportHandle: ExportHandle? = null
    private var exportedFile: java.io.File? = null
    private var screen by mutableStateOf(Screen.HOME)
    private var exportState by mutableStateOf(ExportState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        exportEngine = Media3ExportEngine(this)

        videoPicker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            val project = homeViewModel.createProject(this, uris)
            if (project != null) {
                editorViewModel.open(project)
                screen = Screen.EDITOR
            }
        }
        audioPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            editorViewModel.addAudio(this, uri)
        }

        setContent {
            FoldCutTheme {
                val projects by homeViewModel.projects.collectAsStateWithLifecycle()
                val project by editorViewModel.project.collectAsStateWithLifecycle()
                val playheadMs by editorViewModel.playheadMs.collectAsStateWithLifecycle()
                val selectedClipId by editorViewModel.selectedClipId.collectAsStateWithLifecycle()
                val selectedAudioId by editorViewModel.selectedAudioId.collectAsStateWithLifecycle()
                var isPlaying by remember { mutableStateOf(false) }

                LaunchedEffect(project?.timeline?.videoClips) {
                    val current = project ?: return@LaunchedEffect
                    val items = current.timeline.videoClips.map { clip ->
                        MediaItem.Builder()
                            .setUri(clip.sourceUri)
                            .setClippingConfiguration(
                                MediaItem.ClippingConfiguration.Builder()
                                    .setStartPositionMs(clip.sourceStartMs)
                                    .setEndPositionMs(clip.sourceEndMs)
                                    .build()
                            )
                            .build()
                    }
                    player.setMediaItems(items)
                    player.prepare()
                    seekPlayerTo(current, playheadMs)
                }

                LaunchedEffect(Unit) {
                    while (isActive) {
                        if (player.isPlaying) {
                            isPlaying = true
                            project?.let { editorViewModel.setPlayhead(readProjectPosition(it)) }
                        } else {
                            isPlaying = false
                        }
                        delay(120L)
                    }
                }

                when (screen) {
                    Screen.HOME -> HomeScreen(
                        projects = projects,
                        onNewProject = { videoPicker.launch(arrayOf("video/*")) },
                        onOpenProject = {
                            editorViewModel.open(it)
                            screen = Screen.EDITOR
                        },
                        onDeleteProject = { homeViewModel.delete(it.id) }
                    )
                    Screen.EDITOR -> project?.let { current ->
                        EditorScreen(
                            project = current,
                            playheadMs = playheadMs,
                            selectedClipId = selectedClipId,
                            selectedAudioId = selectedAudioId,
                            isPlaying = isPlaying,
                            canUndo = editorViewModel.canUndo(),
                            canRedo = editorViewModel.canRedo(),
                            player = player,
                            exportState = exportState,
                            onBack = {
                                player.pause()
                                screen = Screen.HOME
                                homeViewModel.refresh()
                            },
                            onPlayToggle = {
                                if (player.isPlaying) player.pause() else player.play()
                                isPlaying = player.isPlaying
                            },
                            onSeek = { position ->
                                editorViewModel.setPlayhead(position)
                                seekPlayerTo(current, position)
                            },
                            onSelectVideo = editorViewModel::selectVideo,
                            onSelectAudio = editorViewModel::selectAudio,
                            onSplit = editorViewModel::splitAtPlayhead,
                            onDelete = editorViewModel::deleteSelected,
                            onUndo = editorViewModel::undo,
                            onRedo = editorViewModel::redo,
                            onTrimStart = { editorViewModel.trimSelectedStart(500L) },
                            onTrimEnd = { editorViewModel.trimSelectedEnd(500L) },
                            onMuteOriginal = editorViewModel::removeOriginalAudio,
                            onVolumeChange = { volume ->
                                if (selectedClipId != null) editorViewModel.setSelectedVideoVolume(volume)
                                else editorViewModel.setSelectedAudioVolume(volume)
                            },
                            onMoveSelected = editorViewModel::moveSelected,
                            onAddAudio = { audioPicker.launch(arrayOf("audio/mpeg", "audio/aac", "audio/wav", "audio/mp4")) },
                            onExport = ::startExport,
                            onCancelExport = {
                                exportHandle?.cancel()
                                exportState = ExportState()
                            },
                            onShare = { exportedFile?.let(::shareVideo) },
                            onWatch = { exportedFile?.let(::watchVideo) },
                            onOpenFile = { exportedFile?.let(::watchVideo) }
                        )
                    }
                }
            }
        }
    }

    private fun startExport(settings: ExportSettings) {
        val project = editorViewModel.project.value ?: return
        exportState = ExportState(running = true, progress = 0)
        val startedAt = System.currentTimeMillis()
        exportHandle = exportEngine.start(
            project = project,
            settings = settings,
            onProgress = { progress ->
                exportState = ExportState(running = true, progress = progress, elapsedMs = System.currentTimeMillis() - startedAt)
            },
            onCompleted = { file ->
                exportedFile = file
                exportState = ExportState(running = false, progress = 100, elapsedMs = System.currentTimeMillis() - startedAt, outputUri = file.absolutePath)
            },
            onError = { message ->
                exportState = ExportState(running = false, error = message)
            }
        )
    }

    private fun readProjectPosition(project: Project): Long {
        val index = player.currentMediaItemIndex.coerceAtLeast(0)
        val clip = project.timeline.videoClips.getOrNull(index) ?: return 0L
        return (clip.timelineStartMs + player.currentPosition).coerceIn(0L, project.timeline.durationMs)
    }

    private fun seekPlayerTo(project: Project, positionMs: Long) {
        val target = project.timeline.videoClips.indexOfFirst { positionMs in it.timelineStartMs..it.timelineEndMs }
        if (target < 0) return
        val clip = project.timeline.videoClips[target]
        player.seekTo(target, (positionMs - clip.timelineStartMs).coerceAtLeast(0L))
    }

    private fun shareVideo(file: java.io.File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Compartilhar FoldCut"))
    }

    private fun watchVideo(file: java.io.File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }

    override fun onDestroy() {
        exportEngine.cancel()
        player.release()
        super.onDestroy()
    }

    private enum class Screen { HOME, EDITOR }
}
