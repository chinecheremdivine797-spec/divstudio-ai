package com.example.ui.screens

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.repository.VideoEditRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

/** Real editor flow: import -> trim/edit -> FFmpeg backend -> MP4 -> preview/save. */
@Composable
fun BackendVideoEditorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { VideoEditRepository(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var sourceName by remember { mutableStateOf("No video selected") }
    var start by remember { mutableStateOf(0f) }
    var end by remember { mutableStateOf(30f) }
    var speed by remember { mutableStateOf(1f) }
    var volume by remember { mutableStateOf(1f) }
    var mute by remember { mutableStateOf(false) }
    var rotate by remember { mutableStateOf<String?>(null) }
    var flip by remember { mutableStateOf<String?>(null) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var exportedFile by remember { mutableStateOf<File?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            sourceName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected video"
            notice = "Video imported. Set the edit controls, then export."
            exportedFile = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkCanvas).padding(16.dp)
    ) {
        Text("DIVSTUDIO AI Editor", color = TextPrimary, fontSize = 24.sp)
        Text("Real FFmpeg export", color = NeonCyan, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))

        Button(onClick = { picker.launch("video/*") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.UploadFile, null)
            Spacer(Modifier.width(8.dp))
            Text("Import video")
        }
        Text(sourceName, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))

        exportedFile?.let { file ->
            AndroidView(
                factory = { ctx -> VideoView(ctx).apply { setVideoPath(file.absolutePath); setOnPreparedListener { it.isLooping = false } } },
                modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.Black, RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(10.dp))
        }

        Text("Trim", color = TextPrimary, fontSize = 15.sp)
        Text("Start: ${"%.1f".format(start)}s", color = TextSecondary, fontSize = 12.sp)
        Slider(value = start, onValueChange = { start = it.coerceAtMost(end - 0.1f).coerceAtLeast(0f) }, valueRange = 0f..300f)
        Text("End: ${"%.1f".format(end)}s", color = TextSecondary, fontSize = 12.sp)
        Slider(value = end, onValueChange = { end = it.coerceAtLeast(start + 0.1f).coerceAtMost(300f) }, valueRange = 0.1f..300f)

        Text("Speed: ${"%.2f".format(speed)}×", color = TextSecondary, fontSize = 12.sp)
        Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.25f..4f)
        Text("Volume: ${"%.1f".format(volume)}×", color = TextSecondary, fontSize = 12.sp)
        Slider(value = volume, onValueChange = { volume = it }, valueRange = 0f..2f)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = mute, onCheckedChange = { mute = it })
            Text("Mute audio", color = TextPrimary)
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { rotate = if (rotate == "90") null else "90" }) { Text(if (rotate == "90") "Rotate 90° ✓" else "Rotate 90°") }
            TextButton(onClick = { flip = if (flip == "horizontal") null else "horizontal" }) { Text(if (flip == "horizontal") "Flip ✓" else "Flip") }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            enabled = selectedUri != null && !exporting,
            onClick = {
                val uri = selectedUri ?: return@Button
                exporting = true
                notice = "Uploading video and rendering with FFmpeg…"
                scope.launch {
                    try {
                        val result = repository.transform(uri, start, end, speed, volume, mute, rotate, flip)
                        val file = repository.downloadResult(result.url)
                        exportedFile = file
                        notice = "Export complete. New MP4 is ready."
                    } catch (error: Throwable) {
                        notice = error.message ?: "Export failed."
                    } finally {
                        exporting = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Movie, null)
            Spacer(Modifier.width(8.dp))
            Text(if (exporting) "Rendering…" else "Export MP4")
        }

        exportedFile?.let { file ->
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { saveMp4(context, file) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("Save MP4 to phone")
            }
        }

        notice?.let { Text(it, color = if (it.contains("complete", true)) NeonCyan else TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
    }
}

private fun saveMp4(context: android.content.Context, source: File) {
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "DIVSTUDIO_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DIVSTUDIO AI")
        put(MediaStore.Video.Media.IS_PENDING, 1)
    }
    val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return
    try {
        context.contentResolver.openOutputStream(uri)?.use { out -> source.inputStream().use { it.copyTo(out) } }
        values.clear(); values.put(MediaStore.Video.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, values, null, null)
    } catch (_: Throwable) { context.contentResolver.delete(uri, null, null) }
}
