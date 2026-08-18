package com.foldcut.app.data

import android.content.Context
import com.foldcut.app.domain.Project
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ProjectStore(context: Context) {
    private val preferences = context.getSharedPreferences("foldcut_projects", Context.MODE_PRIVATE)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun loadProjects(): List<Project> = runCatching {
        val payload = preferences.getString(KEY_PROJECTS, null) ?: return emptyList()
        json.decodeFromString(ListSerializer(Project.serializer()), payload)
            .sortedByDescending { it.updatedAt }
    }.getOrDefault(emptyList())

    fun saveProjects(projects: List<Project>) {
        val payload = json.encodeToString(ListSerializer(Project.serializer()), projects)
        preferences.edit().putString(KEY_PROJECTS, payload).apply()
    }

    fun upsert(project: Project) {
        val updated = loadProjects().filterNot { it.id == project.id } + project
        saveProjects(updated.sortedByDescending { it.updatedAt })
    }

    fun delete(projectId: String) {
        saveProjects(loadProjects().filterNot { it.id == projectId })
    }

    companion object {
        private const val KEY_PROJECTS = "projects"
    }
}
