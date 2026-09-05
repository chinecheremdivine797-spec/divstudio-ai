package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkBorder),
        tonalElevation = 2.dp,
        content = content
    )
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val label = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    Surface(
        modifier = modifier,
        color = when (status.lowercase()) {
            "completed" -> AccentEmerald.copy(alpha = 0.18f)
            "failed" -> AccentRose.copy(alpha = 0.18f)
            "generating", "processing", "rendering", "queued" -> NeonIndigo.copy(alpha = 0.20f)
            else -> DarkSurfaceVariant
        },
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Text(label, color = TextPrimary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
    }
}
