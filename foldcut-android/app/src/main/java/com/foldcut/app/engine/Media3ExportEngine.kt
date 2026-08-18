package com.foldcut.app.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.foldcut.app.domain.AudioClip
import com.foldcut.app.domain.ExportSettings
import com.foldcut.app.domain.Project
import com.foldcut.app.domain.VideoClip
import java.io.File

@UnstableApi
class Media3ExportEngine(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var transformer: Transformer? = null
    private var progressRunnable: Runnable? = null

    fun start(
        project: Project,
        settings: ExportSettings,
        onProgress: (Int) -> Unit,
        onCompleted: (File) -> Unit,
        onError: (String) -> Unit
    ): ExportHandle {
        val output = File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES),
            "foldcut-${System.currentTimeMillis()}.mp4"
        )
        output.parentFile?.mkdirs()
        if (output.exists()) output.delete()

        val videoItems = project.timeline.videoClips.map(::editedVideoItem)
        if (videoItems.isEmpty()) {
            onError("Adicione pelo menos um vídeo antes de exportar.")
            return ExportHandle { }
        }

        val sequences = mutableListOf<EditedMediaItemSequence>()
        sequences += EditedMediaItemSequence.Builder(videoItems).build()

        val audioItems = project.timeline.audioTracks
            .flatMap { it.audioClips }
            .filterNot { it.muted }
            .map(::editedAudioItem)
        if (audioItems.isNotEmpty()) {
            sequences += EditedMediaItemSequence.Builder(audioItems).build()
        }

        val composition = Composition.Builder(sequences).build()
        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, result: ExportResult) {
                stopProgressPolling()
                onProgress(100)
                onCompleted(output)
            }

            override fun onError(
                composition: Composition,
                result: ExportResult,
                exception: ExportException
            ) {
                stopProgressPolling()
                onError(exception.errorCodeName ?: "Falha ao exportar o vídeo")
            }
        }

        val builder = Transformer.Builder(context)
            .setVideoMimeType(if (settings.useHevc) MimeTypes.VIDEO_H265 else MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(listener)
        transformer = builder.build()
        transformer?.start(composition, output.absolutePath)
        startProgressPolling(onProgress)
        return ExportHandle { cancel() }
    }

    fun cancel() {
        stopProgressPolling()
        transformer?.cancel()
        transformer = null
    }

    private fun editedVideoItem(clip: VideoClip): EditedMediaItem {
        val mediaItem = MediaItem.Builder()
            .setUri(clip.sourceUri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.sourceStartMs)
                    .setEndPositionMs(clip.sourceEndMs)
                    .build()
            )
            .build()
        return EditedMediaItem.Builder(mediaItem)
            .setRemoveAudio(clip.muted || clip.volume <= 0f)
            .build()
    }

    private fun editedAudioItem(clip: AudioClip): EditedMediaItem {
        val mediaItem = MediaItem.Builder()
            .setUri(clip.sourceUri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.sourceStartMs)
                    .setEndPositionMs(clip.sourceEndMs)
                    .build()
            )
            .build()
        return EditedMediaItem.Builder(mediaItem).build()
    }

    private fun startProgressPolling(onProgress: (Int) -> Unit) {
        val progressHolder = ProgressHolder()
        val runnable = object : Runnable {
            override fun run() {
                val current = transformer ?: return
                if (current.getProgress(progressHolder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                    onProgress(progressHolder.progress)
                }
                mainHandler.postDelayed(this, 350L)
            }
        }
        progressRunnable = runnable
        mainHandler.post(runnable)
    }

    private fun stopProgressPolling() {
        progressRunnable?.let(mainHandler::removeCallbacks)
        progressRunnable = null
    }
}

class ExportHandle(private val cancelAction: () -> Unit) {
    fun cancel() = cancelAction()
}
