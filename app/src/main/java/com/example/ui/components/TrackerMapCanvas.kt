package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun TrackerMapCanvas(
    latitude: Double,
    longitude: Double,
    isSharingActive: Boolean,
    modifier: Modifier = Modifier
) {
    // Pulse animation for location marker radar ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    // Animated dash path for power grid lines to simulate flow/transmission
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("telemetry_map_card"),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(OutlineSlate)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MidnightBase
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw custom modern security radar maps grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)

                // Draw radar circles / sectors
                drawCircle(
                    color = OutlineSlate.copy(alpha = 0.4f),
                    radius = 80f,
                    center = center,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = OutlineSlate.copy(alpha = 0.2f),
                    radius = 200f,
                    center = center,
                    style = Stroke(width = 2f)
                )

                // Grid background lines
                val numGridLines = 8
                val stepW = w / numGridLines
                for (i in 1..numGridLines) {
                    val x = i * stepW
                    drawLine(
                        color = OutlineSlate.copy(alpha = 0.15f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                }
                val stepH = h / 4
                for (i in 1..4) {
                    val y = i * stepH
                    drawLine(
                        color = OutlineSlate.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Vector simulated roads matching modern aesthetic
                // Road 1
                drawLine(
                    color = OutlineSlate.copy(alpha = 0.3f),
                    start = Offset(0f, h * 0.4f),
                    end = Offset(w, h * 0.6f),
                    strokeWidth = 16f
                )
                // Road 2 (intersecting)
                drawLine(
                    color = OutlineSlate.copy(alpha = 0.3f),
                    start = Offset(w * 0.3f, 0f),
                    end = Offset(w * 0.7f, h),
                    strokeWidth = 16f
                )

                // Telemetry Route Path Line active tracking line representation
                val pathColor = if (isSharingActive) SoftAqua else DimGray
                drawLine(
                    color = pathColor.copy(alpha = 0.8f),
                    start = Offset(w * 0.1f, h * 0.8f),
                    end = center,
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(15f, 15f),
                        dashPhase
                    )
                )

                // Live GPS Marker Pulse Ring
                val pulseColor = if (isSharingActive) SoftAqua else EmergencyRuby
                drawCircle(
                    color = pulseColor.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Main position dot
                drawCircle(
                    color = pulseColor,
                    radius = 12f,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = center
                )
            }

            // Real-time coordinates info overlays positioned elegantly inside the map
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My coordinates",
                        tint = if (isSharingActive) SoftAqua else SecureEmerald,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "LAT: %.6f  •  LNG: %.6f", latitude, longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSharingActive) SoftAqua.copy(alpha = 0.2f) else SoftWhite.copy(alpha = 0.1f),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        text = if (isSharingActive) "SYNC ONLINE" else "LOCAL ONLY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSharingActive) SoftAqua else SoftWhite
                    )
                }
            }
        }
    }
}
