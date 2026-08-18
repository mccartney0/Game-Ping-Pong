package com.foldcut.app

import com.foldcut.app.domain.AudioClip
import com.foldcut.app.domain.Timeline
import com.foldcut.app.domain.VideoClip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelTest {
    @Test
    fun videoClipDurationUsesSourceWindow() {
        val clip = VideoClip(
            sourceUri = "content://video/1",
            sourceName = "clip.mp4",
            sourceDurationMs = 10_000L,
            sourceStartMs = 1_500L,
            sourceEndMs = 7_250L
        )
        assertEquals(5_750L, clip.durationMs)
        assertEquals(5_750L, clip.timelineEndMs)
    }

    @Test
    fun timelineDurationIncludesAudioThatOutlivesVideo() {
        val video = VideoClip(
            sourceUri = "content://video/1",
            sourceName = "clip.mp4",
            sourceDurationMs = 5_000L,
            sourceEndMs = 5_000L
        )
        val audio = AudioClip(
            sourceUri = "content://audio/1",
            sourceName = "music.mp3",
            sourceDurationMs = 20_000L,
            timelineStartMs = 7_000L
        )
        val timeline = Timeline(videoClips = listOf(video), audioTracks = listOf(com.foldcut.app.domain.Track(name = "Música", audioClips = listOf(audio))))
        assertEquals(27_000L, timeline.durationMs)
        assertTrue(timeline.audioTracks.single().audioClips.single().volume == 1f)
    }
}
