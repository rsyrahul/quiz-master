package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseError

@Composable
fun DonutChart(
    correctCount: Int,
    wrongCount: Int,
    skippedCount: Int,
    modifier: Modifier = Modifier.size(160.dp)
) {
    val total = (correctCount + wrongCount + skippedCount).coerceAtLeast(1)
    val correctAngle = (correctCount.toFloat() / total) * 360f
    val wrongAngle = (wrongCount.toFloat() / total) * 360f
    val skippedAngle = (skippedCount.toFloat() / total) * 360f

    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "donut_anim"
    )

    LaunchedEffect(Unit) {
        animationProgress = 1f
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 28.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f

            // Correct Segment
            if (correctAngle > 0) {
                drawArc(
                    color = EmeraldSuccess,
                    startAngle = startAngle,
                    sweepAngle = correctAngle * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += correctAngle
            }

            // Wrong Segment
            if (wrongAngle > 0) {
                drawArc(
                    color = RoseError,
                    startAngle = startAngle,
                    sweepAngle = wrongAngle * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += wrongAngle
            }

            // Skipped Segment
            if (skippedAngle > 0) {
                drawArc(
                    color = GoldAccent,
                    startAngle = startAngle,
                    sweepAngle = skippedAngle * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        val accuracyPct = ((correctCount.toFloat() / total) * 100).toInt()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$accuracyPct%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Accuracy",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Float>>, // Label to value (0-100)
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
) {
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val barWidth = (size.width / (data.size * 2)).coerceIn(16f, 48f)
        val space = (size.width - (barWidth * data.size)) / (data.size + 1)
        val maxHeight = size.height - 40f

        data.forEachIndexed { index, pair ->
            val x = space + index * (barWidth + space)
            val barHeight = (pair.second / 100f) * maxHeight
            val y = maxHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }
    }
}

@Composable
fun LineChart(
    scores: List<Float>, // Values 0 to 100
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        if (scores.size < 2) return@Canvas

        val width = size.width
        val height = size.height - 30f
        val maxScore = 100f
        val xStep = width / (scores.size - 1)

        val path = Path()
        scores.forEachIndexed { i, score ->
            val x = i * xStep
            val y = height - ((score / maxScore) * height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

            // Draw point dot
            drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
