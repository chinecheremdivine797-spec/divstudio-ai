package com.example.ui.screens

import android.content.ContentValues
import android.provider.MediaStore
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.entities.ProjectEntity
import com.example.ui.theme.*
import java.io.File

/**
 * DIVSTUDIO AI editor workspace.
 * Editing is non-destructive: controls change the edit recipe while the source MP4 stays untouched.
 * The recipe is ready to be sent to the FFmpeg editing backend when that service is deployed.
 */
@Composable
fun RealVideoPreviewScreen(
    project: ProjectEntity?,
    onSaveProject: (ProjectEntity) -> Unit
) {
    val context = LocalContext.current
    val videoPath = remember(project?.videoUrl) { project?.videoUrl.orEmpty() }
    val localFile = remember(videoPath) { resolveLocalVideoFile(videoPath) }
    val videoReady = localFile?.exists() == true && localFile.length() > 0L

    var isPlaying by remember { mutableStateOf(false) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var startSeconds by remember { mutableFloatStateOf(0f) }
    var endSeconds by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(1f) }
    var volume by remember { mutableFloatStateOf(1f) }
    var muted by remember { mutableStateOf(false) }
    var rotation by remember { mutableIntStateOf(0) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { videoView?.stopPlayback() }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkCanvas).padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(project?.name ?: "DIVSTUDIO AI Editor", color = TextPrimary, fontSize = 20.sp)
                Text("Professional video editor", color = NeonCyan, fontSize = 12.sp)
            }
            Icon(Icons.Filled.MovieEdit, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black)
        ) {
            if (videoReady && localFile != null) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).also { view ->
                            videoView = view
                            view.setVideoPath(localFile.absolutePath)
                            view.setOnPreparedListener { it.isLooping = false }
                            view.setOnCompletionListener { isPlaying = false }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    "Import or generate an MP4 to start editing.",
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IconButton(enabled = videoReady, onClick = { videoView?.seekTo(0); videoView?.start(); isPlaying = true }) {
                Icon(Icons.Filled.Replay, "Restart", tint = if (videoReady) NeonCyan else DarkBorder)
            }
            IconButton(enabled = videoReady, onClick = {
                if (videoView?.isPlaying == true) { videoView?.pause(); isPlaying = false }
                else { videoView?.start(); isPlaying = true }
            }) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play", tint = if (videoReady) NeonCyan else DarkBorder)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("TIMELINE", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        Surface(color = DarkSurface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp)) {
                Box(Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(8.dp)).background(DarkSurfaceVariant)) {
                    Row(Modifier.fillMaxSize().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(8) { Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(if (it % 2 == 0) NeonIndigo else DarkSurface)) }
                    }
                }
                Text(
                    "Trim: ${startSeconds.toInt()}s → ${if (endSeconds > 0) endSeconds.toInt().toString() else "end"}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EditorTool(Icons.Filled.ContentCut, "Trim") { startSeconds = (startSeconds + 1f).coerceAtLeast(0f); notice = "Trim start set to ${startSeconds.toInt()}s" }
            EditorTool(Icons.Filled.Speed, "Speed") { speed = if (speed >= 4f) 0.25f else speed + 0.25f; notice = "Speed set to ${"%.2f".format(speed)}×" }
            EditorTool(Icons.Filled.VolumeUp, "Volume") { volume = if (volume >= 4f) 0f else volume + 1f; muted = volume == 0f; notice = "Volume set to ${volume.toInt()}×" }
            EditorTool(Icons.Filled.RotateRight, "Rotate") { rotation = (rotation + 90) % 360; notice = "Rotation: $rotation°" }
            EditorTool(Icons.Filled.Flip, "Flip") { flipHorizontal = !flipHorizontal; notice = if (flipHorizontal) "Horizontal flip enabled" else "Horizontal flip disabled" }
            EditorTool(Icons.Filled.VolumeOff, "Mute") { muted = !muted; notice = if (muted) "Audio muted" else "Audio restored" }
        }

        Spacer(Modifier.height(10.dp))
        Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("EDIT SETTINGS", color = NeonCyan, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Text("Start ${startSeconds.toInt()}s", color = TextSecondary, fontSize = 11.sp)
                Slider(value = startSeconds, onValueChange = { startSeconds = it }, valueRange = 0f..60f, enabled = videoReady)
                Text("End ${if (endSeconds == 0f) "auto" else "${endSeconds.toInt()}s"}", color = TextSecondary, fontSize = 11.sp)
                Slider(value = endSeconds, onValueChange = { endSeconds = it }, valueRange = 0f..300f, enabled = videoReady)
                Text("Speed ${"%.2f".format(speed)}×", color = TextSecondary, fontSize = 11.sp)
                Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.25f..4f, enabled = videoReady)
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(
            enabled = videoReady,
            onClick = {
                notice = "Edit recipe ready: trim=${startSeconds.toInt()}-${if (endSeconds > 0) endSeconds.toInt() else "end"}s, speed=${"%.2f".format(speed)}×, volume=${if (muted) 0 else volume}×, rotation=$rotation°, flip=$flipHorizontal. Backend export will render the final MP4."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.MovieFilter, null)
            Spacer(Modifier.width(8.dp))
            Text("Prepare Export")
        }

        Spacer(Modifier.height(8.dp))
        Button(
            enabled = videoReady,
            onClick = { if (localFile != null) notice = saveVideoToMovies(context, localFile, project?.name ?: "DIVSTUDIO_AI") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Save Source MP4")
        }

        notice?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
private fun EditorTool(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp)
    }
}

private fun resolveLocalVideoFile(videoUrl: String): File? {
    if (videoUrl.isBlank()) return null
    val file = File(videoUrl.removePrefix("file://"))
    return if (file.isAbsolute) file else null
}

private fun saveVideoToMovies(context: android.content.Context, source: File, projectName: String): String = runCatching {
    val resolver = context.contentResolver
    val safeName = projectName.replace(Regex("[^A-Za-z0-9_-]"), "_").take(60)
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "${safeName}_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DIVSTUDIO AI")
        put(MediaStore.Video.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return "Could not create destination video."
    try {
        resolver.openOutputStream(uri)?.use { output -> source.inputStream().use { input -> input.copyTo(output) } }
            ?: throw IllegalStateException("Could not open destination video.")
        values.clear(); values.put(MediaStore.Video.Media.IS_PENDING, 0); resolver.update(uri, values, null, null)
        "MP4 saved successfully in Movies/DIVSTUDIO AI."
    } catch (e: Exception) { resolver.delete(uri, null, null); throw e }
}.getOrElse { "Save failed: ${it.message ?: "unknown error"}" }
