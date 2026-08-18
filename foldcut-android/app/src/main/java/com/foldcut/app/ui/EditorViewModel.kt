package com.foldcut.app.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.foldcut.app.data.ProjectStore
import com.foldcut.app.domain.AudioClip
import com.foldcut.app.domain.Project
import com.foldcut.app.domain.Timeline
import com.foldcut.app.domain.Track
import com.foldcut.app.domain.VideoClip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ProjectStore(application)
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _playheadMs = MutableStateFlow(0L)
    val playheadMs: StateFlow<Long> = _playheadMs.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedAudioId = MutableStateFlow<String?>(null)
    val selectedAudioId: StateFlow<String?> = _selectedAudioId.asStateFlow()

    private val undoStack = ArrayDeque<Project>()
    private val redoStack = ArrayDeque<Project>()

    fun open(project: Project) {
        _project.value = project
        _playheadMs.value = 0L
        _selectedClipId.value = project.timeline.videoClips.firstOrNull()?.id
        _selectedAudioId.value = null
        undoStack.clear()
        redoStack.clear()
    }

    fun selectVideo(id: String?) {
        _selectedClipId.value = id
        _selectedAudioId.value = null
    }

    fun selectAudio(id: String?) {
        _selectedAudioId.value = id
        _selectedClipId.value = null
    }

    fun setPlayhead(positionMs: Long) {
        _playheadMs.value = positionMs.coerceIn(0L, _project.value?.timeline?.durationMs ?: 0L)
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun undo() {
        val current = _project.value ?: return
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(current)
        _project.value = previous
        normalizeSelection(previous)
        persist(previous)
    }

    fun redo() {
        val current = _project.value ?: return
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(current)
        _project.value = next
        normalizeSelection(next)
        persist(next)
    }

    fun trimSelectedStart(deltaMs: Long) {
        val current = _project.value ?: return
        val id = _selectedClipId.value ?: return
        val updated = current.timeline.videoClips.map { clip ->
            if (clip.id != id) clip else {
                val maxDelta = (clip.durationMs - 500L).coerceAtLeast(0L)
                val delta = deltaMs.coerceIn(0L, maxDelta)
                clip.copy(
                    sourceStartMs = clip.sourceStartMs + delta,
                    timelineStartMs = clip.timelineStartMs + delta
                )
            }
        }.reflowVideoClips()
        record(current.copy(timeline = current.timeline.copy(videoClips = updated)))
    }

    fun trimSelectedEnd(deltaMs: Long) {
        val current = _project.value ?: return
        val id = _selectedClipId.value ?: return
        val updated = current.timeline.videoClips.map { clip ->
            if (clip.id != id) clip else {
                val maxDelta = (clip.durationMs - 500L).coerceAtLeast(0L)
                clip.copy(sourceEndMs = clip.sourceEndMs - deltaMs.coerceIn(0L, maxDelta))
            }
        }.reflowVideoClips()
        record(current.copy(timeline = current.timeline.copy(videoClips = updated)))
    }

    fun splitAtPlayhead() {
        val current = _project.value ?: return
        val position = _playheadMs.value
        val target = current.timeline.videoClips.firstOrNull { position > it.timelineStartMs && position < it.timelineEndMs }
            ?: return
        val splitOffset = position - target.timelineStartMs
        val first = target.copy(sourceEndMs = target.sourceStartMs + splitOffset)
        val second = target.copy(
            id = UUID.randomUUID().toString(),
            sourceStartMs = first.sourceEndMs,
            timelineStartMs = position
        )
        val updated = current.timeline.videoClips.flatMap { if (it.id == target.id) listOf(first, second) else listOf(it) }
            .reflowVideoClips()
        record(current.copy(timeline = current.timeline.copy(videoClips = updated)))
        _selectedClipId.value = second.id
    }

    fun deleteSelected() {
        val current = _project.value ?: return
        val id = _selectedClipId.value
        if (id != null) {
            val updated = current.timeline.videoClips.filterNot { it.id == id }.reflowVideoClips()
            record(current.copy(timeline = current.timeline.copy(videoClips = updated)))
            _selectedClipId.value = updated.firstOrNull()?.id
            return
        }
        val audioId = _selectedAudioId.value ?: return
        val tracks = current.timeline.audioTracks.map { track ->
            track.copy(audioClips = track.audioClips.filterNot { it.id == audioId })
        }.filter { it.audioClips.isNotEmpty() }
        record(current.copy(timeline = current.timeline.copy(audioTracks = tracks)))
        _selectedAudioId.value = null
    }

    fun moveSelected(deltaMs: Long) {
        val current = _project.value ?: return
        val id = _selectedClipId.value
        if (id != null) {
            val clips = current.timeline.videoClips.map { clip ->
                if (clip.id == id) clip.copy(timelineStartMs = (clip.timelineStartMs + deltaMs).coerceAtLeast(0L)) else clip
            }
            record(current.copy(timeline = current.timeline.copy(videoClips = clips)))
            return
        }
        val audioId = _selectedAudioId.value ?: return
        val tracks = current.timeline.audioTracks.map { track ->
            track.copy(audioClips = track.audioClips.map { clip ->
                if (clip.id == audioId) clip.copy(timelineStartMs = (clip.timelineStartMs + deltaMs).coerceAtLeast(0L)) else clip
            })
        }
        record(current.copy(timeline = current.timeline.copy(audioTracks = tracks)))
    }

    fun setSelectedVideoVolume(volume: Float) {
        val current = _project.value ?: return
        val id = _selectedClipId.value ?: return
        val clips = current.timeline.videoClips.map { if (it.id == id) it.copy(volume = volume.coerceIn(0f, 2f), muted = volume <= 0f) else it }
        record(current.copy(timeline = current.timeline.copy(videoClips = clips)))
    }

    fun toggleSelectedMute() {
        val current = _project.value ?: return
        val id = _selectedClipId.value ?: return
        val clips = current.timeline.videoClips.map { if (it.id == id) it.copy(muted = !it.muted) else it }
        record(current.copy(timeline = current.timeline.copy(videoClips = clips)))
    }

    fun removeOriginalAudio() {
        val current = _project.value ?: return
        val clips = current.timeline.videoClips.map { it.copy(muted = true, volume = 0f) }
        record(current.copy(timeline = current.timeline.copy(videoClips = clips)))
    }

    fun addAudio(context: Context, uri: Uri) {
        val current = _project.value ?: return
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val duration = MediaMetadataReader.durationMs(context, uri)
        val audio = AudioClip(
            sourceUri = uri.toString(),
            sourceName = MediaMetadataReader.displayName(context, uri),
            sourceDurationMs = duration,
            timelineStartMs = _playheadMs.value
        )
        val track = current.timeline.audioTracks.firstOrNull() ?: Track(name = "Áudio 1")
        val updatedTrack = track.copy(audioClips = track.audioClips + audio)
        val tracks = if (current.timeline.audioTracks.isEmpty()) listOf(updatedTrack)
        else listOf(updatedTrack) + current.timeline.audioTracks.drop(1)
        record(current.copy(timeline = current.timeline.copy(audioTracks = tracks)))
        _selectedAudioId.value = audio.id
        _selectedClipId.value = null
    }

    fun setSelectedAudioVolume(volume: Float) {
        val current = _project.value ?: return
        val id = _selectedAudioId.value ?: return
        val tracks = current.timeline.audioTracks.map { track ->
            track.copy(audioClips = track.audioClips.map { if (it.id == id) it.copy(volume = volume.coerceIn(0f, 2f), muted = volume <= 0f) else it })
        }
        record(current.copy(timeline = current.timeline.copy(audioTracks = tracks)))
    }

    private fun record(next: Project) {
        val current = _project.value ?: return
        undoStack.addLast(current)
        if (undoStack.size > 80) undoStack.removeFirst()
        redoStack.clear()
        val saved = next.copy(updatedAt = System.currentTimeMillis())
        _project.value = saved
        persist(saved)
    }

    private fun persist(project: Project) {
        store.upsert(project)
    }

    private fun normalizeSelection(project: Project) {
        if (project.timeline.videoClips.none { it.id == _selectedClipId.value }) {
            _selectedClipId.value = project.timeline.videoClips.firstOrNull()?.id
        }
        if (project.timeline.audioTracks.none { track -> track.audioClips.any { it.id == _selectedAudioId.value } }) {
            _selectedAudioId.value = null
        }
    }
}

private fun List<VideoClip>.reflowVideoClips(): List<VideoClip> {
    var cursor = 0L
    return sortedBy { it.timelineStartMs }.map { clip ->
        val moved = clip.copy(timelineStartMs = cursor)
        cursor += moved.durationMs
        moved
    }
}
