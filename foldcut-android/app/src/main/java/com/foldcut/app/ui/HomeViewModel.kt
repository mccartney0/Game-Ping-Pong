package com.foldcut.app.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.foldcut.app.data.ProjectStore
import com.foldcut.app.domain.Project
import com.foldcut.app.domain.Timeline
import com.foldcut.app.domain.VideoClip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ProjectStore(application)
    private val _projects = MutableStateFlow(store.loadProjects())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    fun refresh() {
        _projects.value = store.loadProjects()
    }

    fun delete(projectId: String) {
        store.delete(projectId)
        refresh()
    }

    fun createProject(context: Context, uris: List<Uri>, name: String = "Projeto sem título"): Project? {
        if (uris.isEmpty()) return null
        val clips = mutableListOf<VideoClip>()
        uris.forEachIndexed { index, uri ->
            tryPersistReadPermission(context, uri)
            val clip = VideoClip(
                sourceUri = uri.toString(),
                sourceName = MediaMetadataReader.displayName(context, uri).ifBlank { "Vídeo ${index + 1}" },
                sourceDurationMs = MediaMetadataReader.durationMs(context, uri),
                timelineStartMs = clips.sumOf { it.durationMs }
            )
            clips += clip
        }
        val project = Project(
            name = name.ifBlank { "Projeto sem título" },
            timeline = Timeline(videoClips = clips)
        )
        store.upsert(project)
        refresh()
        return project
    }

    fun save(project: Project) {
        store.upsert(project)
        refresh()
    }

    private fun tryPersistReadPermission(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }
}

object MediaMetadataReader {
    fun durationMs(context: Context, uri: Uri): Long {
        return runCatching {
            android.media.MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 1_000L
            }
        }.getOrDefault(1_000L).coerceAtLeast(1_000L)
    }

    fun displayName(context: Context, uri: Uri): String {
        val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
        return runCatching {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                } else null
            }
        }.getOrNull() ?: (uri.lastPathSegment?.substringAfterLast('/') ?: "mídia")
    }
}
