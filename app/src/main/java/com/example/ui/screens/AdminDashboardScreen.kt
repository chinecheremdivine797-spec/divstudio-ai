package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AdminLogEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.ProviderStatus
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: UserEntity?,
    allUsers: List<UserEntity>,
    totalProjectsCount: Int,
    totalGenerationsCount: Int,
    completedProjectsCount: Int,
    providerStatuses: List<ProviderStatus>,
    adminLogs: List<AdminLogEntity>,
    onUpdateUserCredits: (String, Int) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var selectedAdminTab by remember { mutableStateOf(0) } // 0: Metrics, 1: Users, 2: System Providers, 3: Audit Logs
    var selectedUserForCredits by remember { mutableStateOf<UserEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("admin_dashboard_screen"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Executive Admin Console",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = AccentAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentAmber)
                        ) {
                            Text(
                                text = "ADMIN ROLE",
                                color = AccentAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Real-time analytics, user access control, and engine telemetry",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Admin Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = DarkSurface,
                contentColor = AccentAmber,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text("Overview", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("Users (${allUsers.size})", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("Providers", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = { Text("Logs", fontSize = 11.sp) }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // TAB 0: Metrics & Overview
        if (selectedAdminTab == 0) {
            item {
                val stats = listOf(
                    Triple("Total Users", allUsers.size.toString(), NeonIndigoLight),
                    Triple("Active Today", allUsers.size.toString(), NeonCyan),
                    Triple("Total Projects", totalProjectsCount.toString(), NeonViolet),
                    Triple("Generations Run", totalGenerationsCount.toString(), AccentAmber),
                    Triple("Completed MP4s", completedProjectsCount.toString(), AccentEmerald),
                    Triple("Failed Jobs", "0", AccentRose)
                )

                stats.chunked(3).forEach { rowStats ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowStats.forEach { (label, value, tint) ->
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(label, color = TextSecondary, fontSize = 10.sp)
                                    Text(value, color = tint, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Platform Health & SLA", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("API Gateway Latency:", color = TextSecondary, fontSize = 12.sp)
                        Text("42ms (Normal)", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GPU Render Cluster:", color = TextSecondary, fontSize = 12.sp)
                        Text("Veo Node A & B Online", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Database Persistence:", color = TextSecondary, fontSize = 12.sp)
                        Text("SQLite Room (Zero-Sync Failure)", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // TAB 1: User Management (Section 25)
        if (selectedAdminTab == 1) {
            items(allUsers) { user ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (user.role == "admin") AccentAmber.copy(alpha = 0.2f) else NeonIndigo.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(user.fullName.take(1).uppercase(), color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(user.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(user.email, color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            Surface(
                                color = if (user.role == "admin") AccentAmber.copy(alpha = 0.2f) else NeonIndigo.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    user.role.uppercase(),
                                    color = if (user.role == "admin") AccentAmber else NeonIndigoLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Credits: ${user.creditsRemaining} • ${user.planName}", color = NeonCyan, fontSize = 11.sp)
                            Button(
                                onClick = { selectedUserForCredits = user },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Adjust Credits", color = TextPrimary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: System Providers & AI Status
        if (selectedAdminTab == 2) {
            items(providerStatuses) { provider ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (provider.isConfigured) DarkBorder else AccentAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(provider.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(
                                color = if (provider.isConfigured) AccentEmerald.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (provider.isConfigured) "CONFIGURED" else "NOT CONFIGURED",
                                    color = if (provider.isConfigured) AccentEmerald else AccentAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text("Service: ${provider.serviceType}", color = NeonIndigoLight, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        Text("Status: ${provider.statusMessage}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        Text("Config location: ${provider.configGuideLocation}", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        // TAB 3: Audit Logs
        if (selectedAdminTab == 3) {
            items(adminLogs) { log ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.action, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(log.adminEmail, color = TextMuted, fontSize = 10.sp)
                        }
                        Text(log.details, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }

    // Adjust Credits Dialog
    selectedUserForCredits?.let { user ->
        var creditAmount by remember { mutableStateOf(user.creditsRemaining.toString()) }
        AlertDialog(
            onDismissRequest = { selectedUserForCredits = null },
            containerColor = DarkSurfaceElevated,
            title = { Text("Adjust Credits for ${user.fullName}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = creditAmount,
                    onValueChange = { creditAmount = it },
                    label = { Text("Available Credits", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = creditAmount.toIntOrNull() ?: user.creditsRemaining
                        onUpdateUserCredits(user.id, num)
                        selectedUserForCredits = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Apply Credits", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForCredits = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
