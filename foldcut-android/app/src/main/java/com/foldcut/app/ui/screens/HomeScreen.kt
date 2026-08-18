package com.foldcut.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foldcut.app.domain.Project
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    projects: List<Project>,
    onNewProject: () -> Unit,
    onOpenProject: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Movie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("FoldCut", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Edição local, simples e precisa", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = onNewProject,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Novo projeto", fontWeight = FontWeight.SemiBold)
            }

            Text("Projetos recentes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (projects.isEmpty()) {
                EmptyProjects(onNewProject)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectCard(project, onOpenProject, onDeleteProject)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyProjects(onNewProject: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Nenhum projeto ainda", style = MaterialTheme.typography.titleMedium)
        Text("Importe um ou mais vídeos para começar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = onNewProject) { Text("Importar vídeos") }
    }
}

@Composable
private fun ProjectCard(project: Project, onOpen: (Project) -> Unit, onDelete: (Project) -> Unit) {
    val clip = project.timeline.videoClips.firstOrNull()
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(project) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (clip != null) {
                AsyncImage(
                    model = clip.sourceUri,
                    contentDescription = "Miniatura de ${project.name}",
                    modifier = Modifier.size(width = 112.dp, height = 72.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(width = 112.dp, height = 72.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Movie, contentDescription = null) }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(project.updatedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("${project.timeline.videoClips.size} clipe(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onDelete(project) }) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Excluir projeto", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
