package com.foldcut.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foldcut.app.domain.AudioClip
import com.foldcut.app.domain.Project
import com.foldcut.app.domain.VideoClip
import kotlin.math.roundToInt

@Composable
fun TimelineView(
    project: Project,
    playheadMs: Long,
    selectedClipId: String?,
    selectedAudioId: String?,
    onSelectVideo: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSeek: (Long) -> Unit,
    onMoveSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    val scroll = rememberScrollState()
    val pixelsPerSecond = 72f * zoom
    val durationMs = project.timeline.durationMs.coerceAtLeast(1_000L)
    val contentWidth = (durationMs / 1_000f * pixelsPerSecond).coerceAtLeast(480f)
    val playheadX = (playheadMs / 1_000f * pixelsPerSecond).coerceIn(0f, contentWidth)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.45f, 3.5f)
                }
            }
    ) {
        Text(
            "Timeline  •  pinch para ampliar",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .horizontalScroll(scroll)
        ) {
            Box(modifier = Modifier.width(contentWidth.dp).fillMaxHeight()) {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Ruler(durationMs, pixelsPerSecond)
                    TrackRow(
                        label = "VÍDEO",
                        height = 64.dp,
                        width = contentWidth.dp,
                        clips = project.timeline.videoClips,
                        selectedId = selectedClipId,
                        color = Color(0xFF2E678B),
                        onSelect = onSelectVideo,
                        onSeek = onSeek,
                        onMove = onMoveSelected,
                        pixelsPerSecond = pixelsPerSecond
                    )
                    project.timeline.audioTracks.forEachIndexed { index, track ->
                        AudioTrackRow(
                            label = "ÁUDIO ${index + 1}",
                            height = 42.dp,
                            width = contentWidth.dp,
                            clips = track.audioClips,
                            selectedId = selectedAudioId,
                            onSelect = onSelectAudio,
                            onSeek = onSeek,
                            onMove = onMoveSelected,
                            pixelsPerSecond = pixelsPerSecond
                        )
                    }
                }
                val playheadColor = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxHeight().width(contentWidth.dp)) {
                    drawLine(
                        color = playheadColor,
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawCircle(playheadColor, radius = 7.dp.toPx(), center = Offset(playheadX, 5.dp.toPx()))
                }
            }
        }
    }
}

@Composable
private fun Ruler(durationMs: Long, pixelsPerSecond: Float) {
    Row(modifier = Modifier.height(22.dp)) {
        var second = 0
        while (second * 1_000L <= durationMs) {
            Text(
                formatTime(second * 1_000L),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width((pixelsPerSecond * 1f).dp).padding(start = 4.dp)
            )
            second += 1
        }
    }
}

@Composable
private fun TrackRow(
    label: String,
    height: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp,
    clips: List<VideoClip>,
    selectedId: String?,
    color: Color,
    onSelect: (String) -> Unit,
    onSeek: (Long) -> Unit,
    onMove: (Long) -> Unit,
    pixelsPerSecond: Float
) {
    Box(modifier = Modifier.width(width).height(height)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(8.dp))
        clips.forEach { clip ->
            ClipBlock(
                clip = clip,
                selected = selectedId == clip.id,
                color = color,
                pixelsPerSecond = pixelsPerSecond,
                onSelect = onSelect,
                onSeek = onSeek,
                onMove = onMove
            )
        }
    }
}

@Composable
private fun AudioTrackRow(
    label: String,
    height: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp,
    clips: List<AudioClip>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onSeek: (Long) -> Unit,
    onMove: (Long) -> Unit,
    pixelsPerSecond: Float
) {
    Box(modifier = Modifier.width(width).height(height)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(6.dp))
        clips.forEach { clip ->
            val x = (clip.timelineStartMs / 1_000f * pixelsPerSecond).dp
            val clipWidth = (clip.durationMs / 1_000f * pixelsPerSecond).coerceAtLeast(54f).dp
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .padding(start = x)
                    .width(clipWidth)
                    .height(height - 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF65528D))
                    .border(2.dp, if (selectedId == clip.id) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                    .pointerInput(clip.id) {
                        detectTapGestures(onTap = { onSelect(clip.id) })
                    }
                    .pointerInput(clip.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onSelect(clip.id)
                            onMove((dragAmount.x / pixelsPerSecond * 1_000f).roundToInt().toLong())
                        }
                    }
            ) {
                Text(clip.sourceName, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun ClipBlock(
    clip: VideoClip,
    selected: Boolean,
    color: Color,
    pixelsPerSecond: Float,
    onSelect: (String) -> Unit,
    onSeek: (Long) -> Unit,
    onMove: (Long) -> Unit
) {
    val x = (clip.timelineStartMs / 1_000f * pixelsPerSecond).dp
    val clipWidth = (clip.durationMs / 1_000f * pixelsPerSecond).coerceAtLeast(72f).dp
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .padding(start = x)
            .width(clipWidth)
            .height(60.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
            .pointerInput(clip.id) {
                detectTapGestures(
                    onTap = { offset ->
                        onSelect(clip.id)
                        onSeek(clip.timelineStartMs + (offset.x / pixelsPerSecond * 1_000f).roundToInt())
                    }
                )
            }
            .pointerInput(clip.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onSelect(clip.id)
                    onMove((dragAmount.x / pixelsPerSecond * 1_000f).roundToInt().toLong())
                }
            }
    ) {
        AsyncImage(
            model = clip.sourceUri,
            contentDescription = clip.sourceName,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(color.copy(alpha = 0.5f)))
        Text(clip.sourceName, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1_000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
