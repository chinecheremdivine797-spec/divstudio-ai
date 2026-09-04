package com.example.ui.screens

import android.content.ContentValues
import android.provider.MediaStore
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.entities.ProjectEntity
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

/**
 * Real MP4 preview and device-save screen.
 * The player only reports a completed video when the generated MP4 exists locally.
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
    var notice by remember { mutableStateOf<String?>(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            videoView?.stopPlayback()
            videoView = null
        }
    }

    LaunchedEffect(videoReady, localFile?.absolutePath) {
        if (!videoReady) {
            isPlaying = false
            videoView?.stopPlayback()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(14.dp)
    ) {
        Text(
            text = project?.name ?: "DIVSTUDIO AI Video",
            color = TextPrimary,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (videoReady) "Real MP4 ready" else "No generated MP4 available yet",
            color = if (videoReady) NeonCyan else TextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
        ) {
            if (videoReady && localFile != null) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).also { view ->
                            videoView = view
                            view.setVideoPath(localFile.absolutePath)
                            view.setOnPreparedListener { prepared ->
                                prepared.isLooping = false
                            }
                            view.setOnCompletionListener {
                                isPlaying = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Generate a video first.\nThe real MP4 will appear here when generation completes.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                enabled = videoReady,
                onClick = {
                    videoView?.seekTo(0)
                    videoView?.start()
                    isPlaying = true
                }
            ) {
                Icon(Icons.Filled.Replay, contentDescription = "Restart", tint = if (videoReady) NeonCyan else DarkBorder)
            }
            IconButton(
                enabled = videoReady,
                onClick = {
                    if (videoView?.isPlaying == true) {
                        videoView?.pause()
                        isPlaying = false
                    } else {
                        videoView?.start()
                        isPlaying = true
                    }
                },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (videoReady) NeonCyan else DarkBorder
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            enabled = videoReady,
            onClick = {
                if (localFile != null) {
                    notice = saveVideoToMovies(context, localFile, project?.name ?: "DIVSTUDIO_AI")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Save MP4 to phone")
        }

        if (!notice.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notice!!,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

private fun resolveLocalVideoFile(videoUrl: String): File? {
    if (videoUrl.isBlank()) return null
    val raw = videoUrl.removePrefix("file://")
    val file = File(raw)
    return if (file.isAbsolute) file else null
}

private fun saveVideoToMovies(
    context: android.content.Context,
    source: File,
    projectName: String
): String {
    return runCatching {
        val resolver = context.contentResolver
        val safeName = projectName.replace(Regex("[^A-Za-z0-9_-]"), "_").take(60)
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "${safeName}_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DIVSTUDIO AI")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: return "Could not create a video file in Movies/DIVSTUDIO AI."

        try {
            resolver.openOutputStream(uri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: throw IllegalStateException("Could not open the destination video file.")

            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            "MP4 saved successfully in Movies/DIVSTUDIO AI."
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }.getOrElse { error ->
        "Save failed: ${error.message ?: "unknown error"}"
    }
}
