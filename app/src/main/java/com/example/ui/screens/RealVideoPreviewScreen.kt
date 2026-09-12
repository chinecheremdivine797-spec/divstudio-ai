package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProjectEntity
import com.example.ui.theme.*

@Composable
fun RealVideoPreviewScreen(project: ProjectEntity?, onSaveProject: (ProjectEntity) -> Unit) {
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { sourceUri = it }
    Column(Modifier.fillMaxSize().background(DarkCanvas).padding(16.dp)) {
        Text(project?.name ?: "DIV EDIT AI Editor", color = TextPrimary, fontSize = 21.sp)
        Text("Import → Edit → Export", color = NeonCyan, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { picker.launch(arrayOf("video/mp4", "video/*")) }) { Icon(Icons.Filled.VideoLibrary, null); Spacer(Modifier.width(8.dp)); Text(if (sourceUri == null) "Import video" else "Video selected") }
        Spacer(Modifier.height(16.dp))
        Surface(modifier = Modifier.fillMaxWidth().height(220.dp), color = Color.Black) { Text(if (sourceUri == null) "Import an MP4 to begin editing." else "Video ready for the FFmpeg editor.", color = TextSecondary, modifier = Modifier.padding(16.dp)) }
        Spacer(Modifier.height(16.dp))
        Button(modifier = Modifier.fillMaxWidth(), onClick = { if (sourceUri != null) project?.let { onSaveProject(it.copy(status = "queued", currentStep = "Ready for FFmpeg export")) } }, enabled = sourceUri != null) { Icon(Icons.Filled.Movie, null); Spacer(Modifier.width(8.dp)); Text("Export edited MP4") }
    }
}
