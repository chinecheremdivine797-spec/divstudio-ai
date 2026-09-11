package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*

@Composable
fun LandingScreen(onNavigate: (NavDestination) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(DarkCanvas).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Spacer(Modifier.height(30.dp))
            Icon(Icons.Filled.AutoAwesome, null, tint = NeonCyan, modifier = Modifier.size(54.dp))
            Text("DIV EDIT AI", color = TextPrimary, fontSize = 32.sp)
            Text("AI filmmaking • animation • video editing", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Text("Create stories, generate scenes, and edit real MP4 video in one studio.", color = TextPrimary, fontSize = 16.sp)
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onNavigate(NavDestination.CREATE) }, Modifier.fillMaxWidth()) { Icon(Icons.Filled.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Create Animation") }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { onNavigate(NavDestination.EDITOR) }, Modifier.fillMaxWidth()) { Icon(Icons.Filled.Movie, null); Spacer(Modifier.width(8.dp)); Text("Open Video Editor") }
            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Version 10", color = TextPrimary, fontSize = 18.sp); Text("Final feature build. Payments are not included.", color = TextSecondary, modifier = Modifier.padding(top = 6.dp)); Text("Google Cloud deployment has been removed.", color = Color.Gray, modifier = Modifier.padding(top = 6.dp)) } }
        }
    }
}
