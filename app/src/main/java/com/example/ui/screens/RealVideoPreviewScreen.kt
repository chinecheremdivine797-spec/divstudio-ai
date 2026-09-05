package com.example.ui.screens

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.data.repository.VideoEditRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

/** Real editor: import/select MP4 -> send trim/effect recipe to FFmpeg -> download rendered MP4. */
@Composable
fun RealVideoPreviewScreen(
    project: ProjectEntity?,
    onSaveProject: (ProjectEntity) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val editor = remember { VideoEditRepository(context) }
    var sourceUri by remember(project?.videoUrl) { mutableStateOf(project?.videoUrl?.takeIf { it.isNotBlank() }?.let { Uri.fromFile(File(it)) }) }
    var sourceFile by remember(sourceUri) { mutableStateOf(sourceUri?.let { uriToLocalFile(context, it) }) }
    var duration by remember(sourceFile) { mutableFloatStateOf(readDurationSeconds(sourceFile).coerceAtLeast(1f)) }
    var startSeconds by remember { mutableFloatStateOf(0f) }
    var endSeconds by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(1f) }
    var volume by remember { mutableFloatStateOf(1f) }
    var muted by remember { mutableStateOf(false) }
    var rotation by remember { mutableIntStateOf(0) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            sourceUri = it
            sourceFile = uriToLocalFile(context, it)
            duration = readDurationSeconds(sourceFile).coerceAtLeast(1f)
            startSeconds = 0f
            endSeconds = 0f
            exportedFile = null
            notice = "Video loaded. Adjust the timeline, then export."
        }
    }

    DisposableEffect(Unit) { onDispose { videoView?.stopPlayback() } }

    fun export() {
        val uri = sourceUri ?: return
        if (isExporting) return
        isExporting = true
        notice = "Uploading video and rendering with FFmpeg…"
        scope.launch {
            try {
                val end = if (endSeconds > 0f) endSeconds.coerceAtMost(duration) else duration
                require(end > startSeconds) { "End time must be after start time." }
                val result = editor.transform(uri, startSeconds, end, speed, volume, muted, rotation.takeIf { it != 0 }?.toString(), if (flipHorizontal) "horizontal" else null)
                notice = "FFmpeg finished. Downloading the rendered MP4…"
                val file = editor.downloadResult(result.url)
                exportedFile = file
                project?.let { onSaveProject(it.copy(videoUrl = file.absolutePath, status = "completed", progress = 100, currentStep = "Edited MP4 ready", updatedAt = System.currentTimeMillis())) }
                notice = "Export complete. Your new MP4 is ready."
            } catch (e: Exception) {
                notice = e.message ?: "FFmpeg export failed."
            } finally { isExporting = false }
        }
    }

    val previewFile = exportedFile ?: sourceFile
    val previewReady = previewFile?.exists() == true && previewFile.length() > 0L

    Column(modifier = Modifier.fillMaxSize().background(DarkCanvas).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(project?.name ?: "DIVSTUDIO AI Editor", color = TextPrimary, fontSize = 20.sp)
                Text("Trim → Edit → Export • FFmpeg", color = NeonCyan, fontSize = 12.sp)
            }
            Icon(Icons.Filled.MovieEdit, null, tint = NeonCyan, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { picker.launch(arrayOf("video/mp4", "video/*")) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.VideoLibrary, null); Spacer(Modifier.width(7.dp)); Text(if (sourceFile == null) "Import video" else "Choose another video")
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(235.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black)) {
            if (previewReady && previewFile != null) {
                AndroidView(factory = { ctx -> VideoView(ctx).also { view ->
                    videoView = view
                    view.setVideoPath(previewFile.absolutePath)
                    view.setOnPreparedListener { it.isLooping = false }
                    view.setOnCompletionListener { isPlaying = false }
                } }, modifier = Modifier.fillMaxSize())
            } else Text("Import an MP4 to begin editing.", color = TextSecondary, modifier = Modifier.align(Alignment.Center))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IconButton(enabled = previewReady, onClick = { videoView?.seekTo(0); videoView?.start(); isPlaying = true }) { Icon(Icons.Filled.Replay, "Restart", tint = if (previewReady) NeonCyan else DarkBorder) }
            IconButton(enabled = previewReady, onClick = { if (videoView?.isPlaying == true) { videoView?.pause(); isPlaying = false } else { videoView?.start(); isPlaying = true } }) { Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play", tint = if (previewReady) NeonCyan else DarkBorder) }
        }
        Spacer(Modifier.height(4.dp))
        Surface(color = DarkSurface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp)) {
                Text("TIMELINE • ${duration.toInt()}s", color = NeonCyan, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(7.dp)).background(DarkSurfaceVariant)) { Row(Modifier.fillMaxSize().padding(5.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) { repeat(12) { Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(if (it % 2 == 0) NeonIndigo else DarkSurface)) } } }
                Text("Trim ${startSeconds.toInt()}s → ${if (endSeconds > 0f) endSeconds.toInt() else duration.toInt()}s", color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                Slider(value = startSeconds, onValueChange = { startSeconds = it.coerceAtMost((if (endSeconds > 0f) endSeconds else duration) - 0.1f).coerceAtLeast(0f) }, valueRange = 0f..duration, enabled = previewReady)
                Slider(value = endSeconds.coerceAtLeast(0f).coerceAtMost(duration), onValueChange = { endSeconds = it.coerceAtLeast(startSeconds + 0.1f).coerceAtMost(duration) }, valueRange = 0f..duration, enabled = previewReady)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SmallTool("Speed ${"%.2f".format(speed)}×") { speed = if (speed >= 4f) 0.25f else speed + 0.25f }
            SmallTool("Volume ${"%.1f".format(volume)}×") { volume = if (volume >= 4f) 0f else volume + 0.5f; muted = volume == 0f }
            SmallTool("Rotate $rotation°") { rotation = (rotation + 90) % 360 }
            SmallTool(if (flipHorizontal) "Unflip" else "Flip") { flipHorizontal = !flipHorizontal }
            SmallTool(if (muted) "Unmute" else "Mute") { muted = !muted }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { export() }, enabled = previewReady && !isExporting, modifier = Modifier.fillMaxWidth()) {
            if (isExporting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White) else Icon(Icons.Filled.MovieFilter, null)
            Spacer(Modifier.width(8.dp)); Text(if (isExporting) "Rendering MP4…" else "Export edited MP4")
        }
        exportedFile?.let { file ->
            Spacer(Modifier.height(6.dp))
            Button(onClick = { notice = saveToMovies(context, file, project?.name ?: "DIVSTUDIO_AI") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Download, null); Spacer(Modifier.width(8.dp)); Text("Save exported MP4 to phone")
            }
        }
        notice?.let { Spacer(Modifier.height(7.dp)); Text(it, color = if (it.contains("complete", true)) NeonCyan else TextSecondary, fontSize = 11.sp) }
    }
}

@Composable
private fun SmallTool(label: String, onClick: () -> Unit) { OutlinedButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)) { Text(label, fontSize = 10.sp) } }

private fun uriToLocalFile(context: android.content.Context, uri: Uri): File? {
    if (uri.scheme == "file") return uri.path?.let(::File)
    val file = File(context.cacheDir, "editor-source-${System.currentTimeMillis()}.mp4")
    return runCatching { context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { input.copyTo(it) } } ?: return null; file }.getOrNull()
}

private fun readDurationSeconds(file: File?): Float {
    if (file == null || !file.exists()) return 15f
    return runCatching {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val ms = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 15000L
        retriever.release(); ms / 1000f
    }.getOrDefault(15f)
}

private fun saveToMovies(context: android.content.Context, source: File, projectName: String): String = runCatching {
    val resolver = context.contentResolver
    val safe = projectName.replace(Regex("[^A-Za-z0-9_-]"), "_").take(50)
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "${safe}_edited_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DIVSTUDIO AI")
        put(MediaStore.Video.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return "Could not create destination video."
    try {
        resolver.openOutputStream(uri)?.use { out -> source.inputStream().use { it.copyTo(out) } } ?: error("Could not open destination video.")
        values.clear(); values.put(MediaStore.Video.Media.IS_PENDING, 0); resolver.update(uri, values, null, null)
        "Edited MP4 saved in Movies/DIVSTUDIO AI."
    } catch (e: Exception) { resolver.delete(uri, null, null); throw e }
}.getOrElse { "Save failed: ${it.message ?: "unknown error"}" }
