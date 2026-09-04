package com.aistudio.divai

import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ImageToVideoScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ImageToVideoRepository() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var prompt by remember { mutableStateOf("Animate this cartoon character naturally: walk forward, wave, smile, and gently move the camera closer. Keep the character's appearance consistent.") }
    var motion by remember { mutableStateOf("Walk") }
    var camera by remember { mutableStateOf("Zoom") }
    var aspect by remember { mutableStateOf("16:9") }
    var status by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var output by remember { mutableStateOf<File?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        imageUri = uri
        error = null
        output = null
        val file = File(context.cacheDir, "divstudio_input_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { input.copyTo(it) } }
        imageFile = file
    }

    LaunchedEffect(motion, camera) {
        prompt = "Animate this cartoon character naturally: $motion. Camera movement: $camera. Preserve the character's identity, colors and clothing. Smooth cartoon animation."
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Image → Cartoon Video")
            OutlinedButton(onClick = onBack) { Text("Back") }
        }

        OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(if (imageUri == null) "Choose cartoon image" else "Choose another image")
        }

        imageUri?.let { uri ->
            Card(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(model = uri, contentDescription = "Selected cartoon", modifier = Modifier.fillMaxWidth().height(240.dp))
            }
        }

        Text("Motion: $motion")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Walk", "Run", "Talk", "Wave", "Dance").forEach { option ->
                OutlinedButton(onClick = { motion = option }) { Text(option) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Jump", "Sit", "Stand", "Point", "Custom").forEach { option ->
                OutlinedButton(onClick = { motion = option }) { Text(option) }
            }
        }

        Text("Camera: $camera")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Static", "Zoom", "Pan", "Follow").forEach { option ->
                OutlinedButton(onClick = { camera = option }) { Text(option) }
            }
        }

        Text("Aspect ratio")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { aspect = "16:9" }) { Text("16:9") }
            OutlinedButton(onClick = { aspect = "9:16" }) { Text("9:16") }
        }

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Describe the animation") }
        )

        Button(
            enabled = imageFile != null && status != "Generating video…" && status != "Uploading image…" && status != "Downloading MP4…",
            onClick = {
                val file = imageFile ?: return@Button
                error = null
                output = null
                scope.launch {
                    repository.generateVideoFromImage(file, prompt, aspect, "720p") { status = it }
                        .onSuccess { output = it }
                        .onFailure { error = it.message ?: "Generation failed"; status = "" }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Generate real MP4") }

        if (status.isNotBlank() && output == null) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
            Text(status)
        }
        error?.let { Text("Error: $it") }

        output?.let { file ->
            Text("Your MP4 is ready")
            AndroidView(
                factory = { VideoView(context).apply { setVideoPath(file.absolutePath); setOnPreparedListener { it.isLooping = true; start() } } },
                modifier = Modifier.fillMaxWidth().height(260.dp)
            )
            OutlinedButton(onClick = {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, file.name)
                    put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/DIVSTUDIO AI")
                }
                context.contentResolver.insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { input -> input.copyTo(out) } }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save MP4 to phone") }
        }
    }
}
