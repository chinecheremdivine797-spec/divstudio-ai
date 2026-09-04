package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    onNavigate: (NavDestination) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("landing_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = NeonIndigo.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Next-Gen AI Animation Platform by DIVSTUDIO",
                            color = NeonCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "Create Amazing Animations With AI",
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Turn your ideas, scripts, images, and characters into animated videos with DIV AI.",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Hero CTA Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigate(NavDestination.CREATE) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("landing_hero_create_btn")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Animation", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { onNavigate(NavDestination.DASHBOARD) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("landing_hero_explore_btn")
                    ) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explore DIV AI", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hero Visual Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurfaceVariant
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_animation_art),
                            contentDescription = "DIV AI Animation Studio",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0x990A0D14))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonCyan)
                            ) {
                                Text(
                                    text = "4K 60FPS • African Animation Rig",
                                    color = NeonCyanLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = NeonIndigo.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "AI Motion Lip-Sync",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Features Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Studio Features",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Professional AI animation tools built into a unified studio canvas.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                val features = listOf(
                    Triple("Text to Animation", "Generate complex animated sequences from descriptive text", Icons.Filled.EditNote),
                    Triple("Image to Animation", "Bring 2D illustrations, portraits, and sketches to life with physics", Icons.Filled.Image),
                    Triple("Script to Animation", "Automate script breakdown into storyboard scenes with dialogue", Icons.Filled.Description),
                    Triple("AI Characters", "Create and save persistent character rigs with consistent identity", Icons.Filled.Face),
                    Triple("AI Scenes", "Generate high-detail African, urban, and fantasy animation backdrops", Icons.Filled.Landscape),
                    Triple("AI Voice & Lip Sync", "Multilingual voiceover with realistic mouth movement alignment", Icons.Filled.RecordVoiceOver),
                    Triple("Character Motion", "Choose from walk, run, dance, wave, fight, and custom gestures", Icons.Filled.DirectionsRun),
                    Triple("Video Editor", "Multi-track timeline with video, audio, dialogue, and subtitle tracks", Icons.Filled.MovieCreation),
                    Triple("Automatic Subtitles", "Auto-generate, timecode-edit, and export SRT / VTT captions", Icons.Filled.Subtitles),
                    Triple("Multiple Video Ratios", "Export in 16:9 Landscape, 9:16 Reels/TikTok, 1:1 Square, 4:5 Post", Icons.Filled.AspectRatio),
                    Triple("Export Videos", "High-bitrate MP4 rendering with custom resolution up to 4K", Icons.Filled.Download)
                )

                features.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { (title, desc, icon) ->
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                backgroundColor = DarkSurface
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = NeonIndigoLight,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // How It Works Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "How It Works",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                val steps = listOf(
                    "1. Describe your idea" to "Enter a text prompt, paste a story script, or upload reference artwork.",
                    "2. Create your characters and scenes" to "Define character styles, personality, voices, and animated environments.",
                    "3. Generate your animation" to "DIV AI renders scene keyframes, optical motion flows, and voice lip-sync.",
                    "4. Edit your video" to "Fine-tune scene timings, add audio effects, and adjust subtitles on the timeline.",
                    "5. Export your finished animation" to "Download high-definition MP4 videos ready for social media or streaming."
                )

                steps.forEachIndexed { index, (stepTitle, stepDesc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = NeonIndigo.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonIndigoLight),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = NeonIndigoLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stepTitle,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stepDesc,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Use Cases Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Built For Every Creator",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val useCases = listOf(
                    "YouTube & Long-form", "TikTok & Reels", "Instagram Shorts",
                    "African Folklore & Stories", "Children's Cartoons", "Educational Series",
                    "Brand Advertisements", "Independent Short Films", "Anime Music Videos"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        useCases.take(5).forEach { item ->
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        useCases.drop(5).forEach { item ->
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // African Storytelling Focus Banner
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                backgroundColor = DarkSurfaceElevated,
                borderColor = AccentAmber.copy(alpha = 0.5f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "African Animation & Native Voices",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Native support for Nigerian English, Pidgin, Yoruba, Igbo, and Hausa voice synthesis, alongside authentic African character rigs and Lagos environments.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = DarkBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "DIV AI",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "Powered by DIVSTUDIO",
                    color = NeonIndigoLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Empowering creators to animate the world's most vivid stories with AI.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
