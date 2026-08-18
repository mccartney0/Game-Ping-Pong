package com.foldcut.app.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.foldcut.app.domain.ExportFps
import com.foldcut.app.domain.ExportQuality
import com.foldcut.app.domain.ExportResolution
import com.foldcut.app.domain.ExportSettings
import com.foldcut.app.domain.ExportState
import com.foldcut.app.domain.Project
import com.foldcut.app.ui.components.TimelineView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: Project,
    playheadMs: Long,
    selectedClipId: String?,
    selectedAudioId: String?,
    isPlaying: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    player: ExoPlayer,
    exportState: ExportState,
    onBack: () -> Unit,
    onPlayToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onSelectVideo: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onTrimStart: () -> Unit,
    onTrimEnd: () -> Unit,
    onMuteOriginal: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onMoveSelected: (Long) -> Unit,
    onAddAudio: () -> Unit,
    onExport: (ExportSettings) -> Unit,
    onCancelExport: () -> Unit,
    onShare: () -> Unit,
    onWatch: () -> Unit,
    onOpenFile: () -> Unit
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var showFullScreen by remember { mutableStateOf(false) }
    val selectedVideo = project.timeline.videoClips.firstOrNull { it.id == selectedClipId }
    val selectedAudio = project.timeline.audioTracks.flatMap { it.audioClips }.firstOrNull { it.id == selectedAudioId }
    val selectedVolume = selectedVideo?.volume ?: selectedAudio?.volume ?: 1f

    Scaffold(
        topBar = {
            if (!showFullScreen) TopAppBar(
                title = { Text(project.name, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar") } },
                actions = {
                    IconButton(onClick = onUndo, enabled = canUndo) { Icon(Icons.Outlined.Undo, contentDescription = "Desfazer") }
                    IconButton(onClick = onRedo, enabled = canRedo) { Icon(Icons.Outlined.Redo, contentDescription = "Refazer") }
                    Button(onClick = { showExportDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar")
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { insets ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val wideLayout = maxWidth >= 680.dp
            if (showFullScreen) {
                PreviewCard(player, playheadMs, project.timeline.durationMs, isPlaying, true, onPlayToggle, onSeek, { showFullScreen = false })
            } else if (wideLayout) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PreviewCard(player, playheadMs, project.timeline.durationMs, isPlaying, showFullScreen, onPlayToggle, onSeek, { showFullScreen = !showFullScreen })
                        ActionBar(onSplit, onDelete, onTrimStart, onTrimEnd, onMuteOriginal, onAddAudio)
                        Inspector(selectedVideo != null || selectedAudio != null, selectedVolume, onVolumeChange)
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TimelineView(
                            project = project,
                            playheadMs = playheadMs,
                            selectedClipId = selectedClipId,
                            selectedAudioId = selectedAudioId,
                            onSelectVideo = onSelectVideo,
                            onSelectAudio = onSelectAudio,
                            onSeek = onSeek,
                            onMoveSelected = onMoveSelected,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                        Text("${formatTime(playheadMs)} / ${formatTime(project.timeline.durationMs)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PreviewCard(player, playheadMs, project.timeline.durationMs, isPlaying, showFullScreen, onPlayToggle, onSeek, { showFullScreen = !showFullScreen })
                    TimelineView(
                        project = project,
                        playheadMs = playheadMs,
                        selectedClipId = selectedClipId,
                        selectedAudioId = selectedAudioId,
                        onSelectVideo = onSelectVideo,
                        onSelectAudio = onSelectAudio,
                        onSeek = onSeek,
                        onMoveSelected = onMoveSelected,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    ActionBar(onSplit, onDelete, onTrimStart, onTrimEnd, onMuteOriginal, onAddAudio)
                    Inspector(selectedVideo != null || selectedAudio != null, selectedVolume, onVolumeChange)
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(onDismiss = { showExportDialog = false }, onExport = { settings -> showExportDialog = false; onExport(settings) })
    }
    if (exportState.running) {
        ExportProgressDialog(exportState.progress, onCancelExport)
    } else if (exportState.outputUri != null) {
        ExportCompleteDialog(onShare, onWatch, onOpenFile)
    }
}

@Composable
private fun PreviewCard(
    player: ExoPlayer,
    playheadMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    fullScreen: Boolean,
    onPlayToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullScreen: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().then(if (fullScreen) Modifier.fillMaxHeight() else Modifier.height(250.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        this.player = player
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                },
                update = { it.player = player },
                modifier = Modifier.fillMaxSize()
            )
            Row(modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = Color.Black.copy(alpha = 0.72f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                        IconButton(onClick = { onSeek((playheadMs - 5_000L).coerceAtLeast(0L)) }) { Text("−5", color = Color.White) }
                        IconButton(onClick = onPlayToggle) { Icon(if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White) }
                        IconButton(onClick = { onSeek((playheadMs + 5_000L).coerceAtMost(durationMs)) }) { Text("+5", color = Color.White) }
                        Text("${formatTime(playheadMs)} / ${formatTime(durationMs)}", color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = onFullScreen) { Icon(if (fullScreen) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen, contentDescription = if (fullScreen) "Sair da tela cheia" else "Tela cheia", tint = Color.White) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBar(onSplit: () -> Unit, onDelete: () -> Unit, onTrimStart: () -> Unit, onTrimEnd: () -> Unit, onMute: () -> Unit, onAddAudio: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onSplit, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.ContentCut, contentDescription = null); Spacer(Modifier.width(5.dp)); Text("Dividir") }
        OutlinedButton(onClick = onTrimStart, modifier = Modifier.weight(1f)) { Text("Cortar início") }
        OutlinedButton(onClick = onTrimEnd, modifier = Modifier.weight(1f)) { Text("Cortar fim") }
        IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, contentDescription = "Excluir trecho", tint = MaterialTheme.colorScheme.error) }
        IconButton(onClick = onMute) { Icon(Icons.Outlined.VolumeOff, contentDescription = "Remover áudio original") }
        IconButton(onClick = onAddAudio) { Icon(Icons.Outlined.Add, contentDescription = "Adicionar áudio") }
    }
}

@Composable
private fun Inspector(hasSelection: Boolean, volume: Float, onVolumeChange: (Float) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (volume <= 0f) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
            Text("Volume", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
            Slider(value = volume, onValueChange = onVolumeChange, enabled = hasSelection, valueRange = 0f..2f, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
            Text("${(volume * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ExportDialog(onDismiss: () -> Unit, onExport: (ExportSettings) -> Unit) {
    var resolution by remember { mutableStateOf(ExportResolution.P1080) }
    var fps by remember { mutableStateOf(ExportFps.ORIGINAL) }
    var quality by remember { mutableStateOf(ExportQuality.NORMAL) }
    var hevc by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar vídeo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Resolução", style = MaterialTheme.typography.labelLarge)
                ChoiceRow(ExportResolution.values().toList(), resolution, { resolution = it }) { it.label }
                Text("FPS", style = MaterialTheme.typography.labelLarge)
                ChoiceRow(ExportFps.values().toList(), fps, { fps = it }) { it.label }
                Text("Qualidade", style = MaterialTheme.typography.labelLarge)
                ChoiceRow(ExportQuality.values().toList(), quality, { quality = it }) { it.label }
                FilterChip(selected = hevc, onClick = { hevc = !hevc }, label = { Text("HEVC/H.265 quando suportado") }, leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) })
                Text("Saída: MP4 • vídeo H.264/H.265 • áudio AAC", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { Button(onClick = { onExport(ExportSettings(resolution, fps, quality, hevc)) }) { Text("Exportar") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun <T> ChoiceRow(values: List<T>, selected: T, onSelect: (T) -> Unit, label: (T) -> String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEach { value ->
            FilterChip(selected = value == selected, onClick = { onSelect(value) }, label = { Text(label(value)) })
        }
    }
}

@Composable
private fun ExportProgressDialog(progress: Int, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Exportando vídeo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                Text("$progress%", style = MaterialTheme.typography.titleMedium)
                Text("O processamento acontece localmente. Você pode continuar acompanhando o progresso.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {},
        dismissButton = { OutlinedButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Composable
private fun ExportCompleteDialog(onShare: () -> Unit, onWatch: () -> Unit, onOpenFile: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Exportação concluída") },
        text = { Text("Seu MP4 está pronto. Escolha uma ação para continuar.") },
        confirmButton = { Button(onClick = onShare) { Text("Compartilhar") } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onWatch) { Text("Assistir") }
                OutlinedButton(onClick = onOpenFile) { Text("Abrir arquivo") }
            }
        }
    )
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1_000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
