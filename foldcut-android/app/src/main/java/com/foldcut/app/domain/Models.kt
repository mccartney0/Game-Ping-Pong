package com.foldcut.app.domain

import kotlinx.serialization.Serializable
import java.util.UUID

private fun newId(): String = UUID.randomUUID().toString()

@Serializable
enum class CanvasPreset(val label: String, val ratio: String) {
    ORIGINAL("Original", "source"),
    YOUTUBE_16_9("YouTube 16:9", "16:9"),
    SHORTS_9_16("YouTube Shorts 9:16", "9:16"),
    REELS_9_16("Instagram/Reels 9:16", "9:16"),
    SQUARE_1_1("Quadrado 1:1", "1:1")
}

@Serializable
enum class ExportResolution(val label: String, val shortLabel: String, val height: Int) {
    P720("720p", "720", 720),
    P1080("1080p", "1080", 1080),
    P1440("1440p", "1440", 1440),
    P2160("2160p / 4K", "4K", 2160)
}

@Serializable
enum class ExportFps(val label: String, val value: Int?) {
    ORIGINAL("Original", null), FPS24("24", 24), FPS30("30", 30), FPS60("60", 60)
}

@Serializable
enum class ExportQuality(val label: String, val bitrateFactor: Float) {
    ECONOMY("Econômica", 0.65f), NORMAL("Normal", 1.0f), HIGH("Alta", 1.35f), VERY_HIGH("Muito alta", 1.8f)
}

@Serializable
data class TransformState(
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

@Serializable
data class VideoClip(
    val id: String = newId(),
    val sourceUri: String,
    val sourceName: String,
    val sourceDurationMs: Long,
    val sourceStartMs: Long = 0L,
    val sourceEndMs: Long = sourceDurationMs,
    val timelineStartMs: Long = 0L,
    val volume: Float = 1f,
    val muted: Boolean = false,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val transform: TransformState = TransformState()
) {
    val durationMs: Long get() = (sourceEndMs - sourceStartMs).coerceAtLeast(0L)
    val timelineEndMs: Long get() = timelineStartMs + durationMs
}

@Serializable
data class AudioClip(
    val id: String = newId(),
    val sourceUri: String,
    val sourceName: String,
    val sourceDurationMs: Long,
    val sourceStartMs: Long = 0L,
    val sourceEndMs: Long = sourceDurationMs,
    val timelineStartMs: Long = 0L,
    val volume: Float = 1f,
    val muted: Boolean = false,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L
) {
    val durationMs: Long get() = (sourceEndMs - sourceStartMs).coerceAtLeast(0L)
    val timelineEndMs: Long get() = timelineStartMs + durationMs
}

@Serializable
data class Track(
    val id: String = newId(),
    val name: String,
    val audioClips: List<AudioClip> = emptyList(),
    val muted: Boolean = false,
    val volume: Float = 1f
)

@Serializable
data class Timeline(
    val videoClips: List<VideoClip> = emptyList(),
    val audioTracks: List<Track> = emptyList()
) {
    val durationMs: Long
        get() = maxOf(
            videoClips.maxOfOrNull { it.timelineEndMs } ?: 0L,
            audioTracks.flatMap { it.audioClips }.maxOfOrNull { it.timelineEndMs } ?: 0L
        )
}

@Serializable
data class Project(
    val id: String = newId(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val canvasPreset: CanvasPreset = CanvasPreset.ORIGINAL,
    val timeline: Timeline
)

@Serializable
data class ExportSettings(
    val resolution: ExportResolution = ExportResolution.P1080,
    val fps: ExportFps = ExportFps.ORIGINAL,
    val quality: ExportQuality = ExportQuality.NORMAL,
    val useHevc: Boolean = false
)

@Serializable
data class ExportState(
    val running: Boolean = false,
    val progress: Int = 0,
    val elapsedMs: Long = 0L,
    val outputUri: String? = null,
    val error: String? = null
)
