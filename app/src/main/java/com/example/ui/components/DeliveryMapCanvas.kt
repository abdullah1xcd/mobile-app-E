package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.Primary

@Composable
fun DeliveryMapCanvas(
    etaMinutes: Int = 15,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_animation")

    // Pulsing radar for destination
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Animated truck progress along route (0.4 to 0.75)
    val truckProgress by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "truck_pos"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw background road grid
                val gridRoadColor = Color(0xFFE2E8F0)
                val riverColor = Color(0xFFD6E4FF)

                // Gentle river/lake curve on top
                val riverPath = Path().apply {
                    moveTo(0f, h * 0.15f)
                    cubicTo(w * 0.3f, h * 0.05f, w * 0.7f, h * 0.25f, w, h * 0.12f)
                }
                drawPath(riverPath, riverColor, style = Stroke(width = 16f, cap = StrokeCap.Round))

                // Minor streets
                drawLine(gridRoadColor, Offset(0f, h * 0.45f), Offset(w, h * 0.42f), strokeWidth = 10f)
                drawLine(gridRoadColor, Offset(0f, h * 0.78f), Offset(w, h * 0.75f), strokeWidth = 10f)
                drawLine(gridRoadColor, Offset(w * 0.25f, 0f), Offset(w * 0.22f, h), strokeWidth = 10f)
                drawLine(gridRoadColor, Offset(w * 0.55f, 0f), Offset(w * 0.52f, h), strokeWidth = 10f)
                drawLine(gridRoadColor, Offset(w * 0.85f, 0f), Offset(w * 0.82f, h), strokeWidth = 10f)

                // Park / Green area blocks
                drawRoundRect(
                    color = Color(0xFFE6F4EA),
                    topLeft = Offset(w * 0.05f, h * 0.52f),
                    size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )

                drawRoundRect(
                    color = Color(0xFFE6F4EA),
                    topLeft = Offset(w * 0.62f, h * 0.28f),
                    size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.15f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )

                // Route definition: Depot (left) -> turns -> Customer (right)
                val startP = Offset(w * 0.15f, h * 0.75f)
                val corner1 = Offset(w * 0.42f, h * 0.75f)
                val corner2 = Offset(w * 0.42f, h * 0.38f)
                val endP = Offset(w * 0.82f, h * 0.38f)

                // Route background outline
                val routePath = Path().apply {
                    moveTo(startP.x, startP.y)
                    lineTo(corner1.x, corner1.y)
                    lineTo(corner2.x, corner2.y)
                    lineTo(endP.x, endP.y)
                }

                // White road highlight under route
                drawPath(
                    routePath,
                    Color.White,
                    style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Colored route line (Brand Indigo)
                drawPath(
                    routePath,
                    Primary,
                    style = Stroke(
                        width = 8f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
                    )
                )

                // Destination Radar pulse
                drawCircle(
                    color = AccentGreen.copy(alpha = pulseAlpha),
                    radius = 22f * pulseScale,
                    center = endP
                )

                // Destination Pin Base
                drawCircle(
                    color = AccentGreen,
                    radius = 18f,
                    center = endP
                )
                drawCircle(
                    color = Color.White,
                    radius = 7f,
                    center = endP
                )

                // Depot / Fulfillment Center Base
                drawCircle(
                    color = Color(0xFF6B7280),
                    radius = 14f,
                    center = startP
                )

                // Calculate current truck position along route
                // Path has 3 segments:
                // seg1: (0.42 - 0.15) * w = 0.27 * w
                // seg2: (0.75 - 0.38) * h = 0.37 * h
                // seg3: (0.82 - 0.42) * w = 0.40 * w
                // Position truck dynamically in seg 2/3
                val truckPos = Offset(
                    x = corner2.x + (endP.x - corner2.x) * ((truckProgress - 0.5f) / 0.5f).coerceIn(0f, 1f),
                    y = corner2.y
                )

                // Courier halo
                drawCircle(
                    color = Primary.copy(alpha = 0.25f),
                    radius = 26f,
                    center = truckPos
                )
                drawCircle(
                    color = Primary,
                    radius = 16f,
                    center = truckPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = truckPos
                )
            }

            // Depot Label overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, bottom = 14.dp)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📦 Fulfillment Hub",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
            }

            // Destination Label overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 14.dp, top = 14.dp)
                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                    .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "📍 Villa 221",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
            }

            // Top Center Live ETA chip
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp),
                shape = RoundedCornerShape(20.dp),
                color = Primary,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "🚚 Arriving in $etaMinutes min",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}
