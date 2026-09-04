package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.CharacterEntity
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterStudioScreen(
    characters: List<CharacterEntity>,
    onSaveCharacter: (CharacterEntity) -> Unit,
    onDuplicateCharacter: (CharacterEntity) -> Unit,
    onDeleteCharacter: (String) -> Unit,
    onUseInProject: (CharacterEntity) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingChar by remember { mutableStateOf<CharacterEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("character_studio_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Character Studio",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create and save persistent character rigs across all your animations.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_character_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Rig", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (characters.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No characters saved yet. Create your first character rig!", color = TextMuted)
                }
            }
        } else {
            items(characters) { char ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("character_card_${char.name}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, NeonCyan, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(
                                        if (char.avatarResName == "hero_animation_art") R.drawable.hero_animation_art
                                        else R.drawable.character_david
                                    ),
                                    contentDescription = char.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = char.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = NeonIndigo.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = char.animationStyle,
                                            color = NeonIndigoLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${char.gender}, ${char.age} • Voice: ${char.voice}",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = char.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            Text("Outfit: ", color = TextMuted, fontSize = 11.sp)
                            Text(char.clothing, color = TextPrimary, fontSize = 11.sp)
                        }
                        Row {
                            Text("Personality: ", color = TextMuted, fontSize = 11.sp)
                            Text(char.personality, color = TextPrimary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onUseInProject(char) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Use in Project", fontSize = 11.sp, color = Color.White)
                            }

                            Row {
                                TextButton(onClick = { editingChar = char }) {
                                    Text("Edit", color = NeonCyan, fontSize = 11.sp)
                                }
                                TextButton(onClick = { onDuplicateCharacter(char) }) {
                                    Text("Duplicate", color = TextSecondary, fontSize = 11.sp)
                                }
                                TextButton(onClick = { onDeleteCharacter(char.id) }) {
                                    Text("Delete", color = AccentRose, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Character Dialog
    if (showCreateDialog || editingChar != null) {
        val target = editingChar
        var name by remember { mutableStateOf(target?.name ?: "") }
        var age by remember { mutableStateOf(target?.age ?: "Young Adult") }
        var gender by remember { mutableStateOf(target?.gender ?: "Male") }
        var hair by remember { mutableStateOf(target?.hair ?: "Neat Afro") }
        var clothing by remember { mutableStateOf(target?.clothing ?: "Modern yellow hoodie with Ankara trims") }
        var personality by remember { mutableStateOf(target?.personality ?: "Charismatic and visionary") }
        var description by remember { mutableStateOf(target?.description ?: "") }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                editingChar = null
            },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = if (target != null) "Edit Character" else "Create Character Rig",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Character Name", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender", color = TextSecondary) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age", color = TextSecondary) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hair,
                        onValueChange = { hair = it },
                        label = { Text("Hair Style & Color", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = clothing,
                        onValueChange = { clothing = it },
                        label = { Text("Clothing Style", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        label = { Text("Personality Traits", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Background Description", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toSave = target?.copy(
                            name = name.ifBlank { "Hero Character" },
                            age = age,
                            gender = gender,
                            hair = hair,
                            clothing = clothing,
                            personality = personality,
                            description = description
                        ) ?: CharacterEntity(
                            id = "char_${UUID.randomUUID().toString().take(8)}",
                            userId = "user_default_01",
                            name = name.ifBlank { "New Character" },
                            age = age,
                            gender = gender,
                            hair = hair,
                            clothing = clothing,
                            personality = personality,
                            description = description,
                            avatarResName = "character_david"
                        )
                        onSaveCharacter(toSave)
                        showCreateDialog = false
                        editingChar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Save Rig", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    editingChar = null
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
