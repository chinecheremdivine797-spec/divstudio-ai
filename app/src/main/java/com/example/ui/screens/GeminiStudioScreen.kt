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

private data class GeminiMessage(val text: String, val fromUser: Boolean)

@Composable
fun GeminiStudioScreen(onOpenEditor: () -> Unit, onOpenCreate: () -> Unit) {
    val messages = remember { mutableStateListOf<GeminiMessage>() }
    var prompt by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(DarkCanvas).padding(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("DIV EDIT AI", color = TextPrimary, fontSize = 24.sp); Text("AI Creative Assistant", color = NeonCyan, fontSize = 13.sp) }
            Icon(Icons.Outlined.AutoAwesome, null, tint = NeonCyan)
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (messages.isEmpty()) item { Text("Create a story, plan scenes, or prepare a video prompt.", color = TextSecondary, modifier = Modifier.padding(12.dp)) }
            items(messages) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
                    Surface(color = if (message.fromUser) DarkSurfaceVariant else DarkSurface, shape = RoundedCornerShape(18.dp), modifier = Modifier.widthIn(max = 330.dp)) { Text(message.text, color = TextPrimary, modifier = Modifier.padding(14.dp)) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenCreate) { Icon(Icons.Outlined.Add, "Create", tint = TextSecondary) }
            TextField(value = prompt, onValueChange = { prompt = it }, modifier = Modifier.weight(1f), placeholder = { Text("Describe what you want to create") })
            IconButton(onClick = { if (prompt.isNotBlank()) { messages += GeminiMessage(prompt.trim(), true); messages += GeminiMessage("Your request is ready for the DIV EDIT AI backend. Configure the backend endpoint to generate the final result.", false); prompt = "" } }) { Icon(Icons.Outlined.ArrowUpward, "Send", tint = NeonCyan) }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenEditor, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Movie, null); Spacer(Modifier.width(6.dp)); Text("Open Video Editor") }
    }
}
