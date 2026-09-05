package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeBackend
import com.google.firebase.ai.generativeModel
import kotlinx.coroutines.launch

private data class GeminiMessage(val text: String, val fromUser: Boolean)

@Composable
fun GeminiStudioScreen(
    onOpenEditor: () -> Unit,
    onOpenCreate: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<GeminiMessage>() }
    var prompt by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val model = remember {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.7-flash")
    }
    val chat = remember { model.startChat() }

    fun sendPrompt(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || busy) return
        prompt = ""
        error = null
        messages += GeminiMessage(clean, true)
        busy = true
        scope.launch {
            try {
                val response = chat.sendMessage(clean)
                messages += GeminiMessage(response.text ?: "I couldn't generate a response.", false)
            } catch (t: Throwable) {
                error = t.message ?: "Gemini could not respond. Check Firebase AI Logic setup and App Check."
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkCanvas).padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("DIVSTUDIO AI", color = TextPrimary, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("Gemini Creative Assistant", color = NeonCyan, fontSize = 13.sp)
            }
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
        }

        if (messages.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("What do you want to create?", color = TextPrimary, fontSize = 25.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Ask Gemini to write a script, plan scenes, improve an idea, or help edit a video.", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(onClick = { sendPrompt("Create a short cartoon story for me") }, label = { Text("Create a story") }, leadingIcon = { Icon(Icons.Outlined.AutoAwesome, null) })
                    AssistChip(onClick = onOpenEditor, label = { Text("Edit video") }, leadingIcon = { Icon(Icons.Outlined.Movie, null) })
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { message ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
                        Surface(
                            color = if (message.fromUser) DarkSurfaceVariant else DarkSurface,
                            shape = RoundedCornerShape(18.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 330.dp)
                        ) {
                            Text(message.text, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
                if (busy) item {
                    Text("Gemini is thinking…", color = NeonCyan, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }

        error?.let {
            Text(it, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Surface(color = DarkSurface, shape = RoundedCornerShape(26.dp), border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder), modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp, end = 4.dp)) {
                    IconButton(onClick = onOpenCreate) { Icon(Icons.Outlined.Add, "Attach/create", tint = TextSecondary) }
                    TextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Ask DIVSTUDIO AI…", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 5
                    )
                    IconButton(onClick = { sendPrompt(prompt) }, enabled = prompt.isNotBlank() && !busy) {
                        Icon(Icons.Outlined.ArrowUpward, "Send", tint = if (prompt.isNotBlank() && !busy) NeonCyan else TextMuted)
                    }
                }
            }
        }
        Text("Gemini responses can be inaccurate. Review AI output before using it in a final project.", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(bottom = 6.dp))
    }
}
