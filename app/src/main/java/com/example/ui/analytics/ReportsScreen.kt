package com.example.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.BarChart
import com.example.ui.components.LineChart
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val results by viewModel.userResults.collectAsState()
    var selectedReportPeriod by remember { mutableStateOf("Overall") }

    val totalQuizzes = results.size
    val avgScore = if (totalQuizzes > 0) results.map { it.scorePercentage }.average().toFloat() else 0f
    val totalCorrect = results.sumOf { it.correctCount }
    val avgTime = if (totalQuizzes > 0) results.map { it.timeTakenSeconds }.average().toInt() else 0

    // Category breakdown
    val categoryPerformance = results.groupBy { it.categoryName }.mapValues { entry ->
        entry.value.map { it.scorePercentage }.average().toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Reports") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Period Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Overall", "Daily", "Weekly", "Monthly").forEach { period ->
                    FilterChip(
                        selected = selectedReportPeriod == period,
                        onClick = { selectedReportPeriod = period },
                        label = { Text(period) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard("Quizzes Played", "$totalQuizzes", Icons.Default.Analytics, Modifier.weight(1f))
                ReportMetricCard("Avg Accuracy", String.format(Locale.US, "%.1f%%", avgScore), Icons.Default.CheckCircle, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard("Total Correct", "$totalCorrect", Icons.Default.Category, Modifier.weight(1f))
                ReportMetricCard("Avg Time", "${avgTime}s", Icons.Default.Schedule, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Wise Performance Bar Chart
            if (categoryPerformance.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Category Wise Performance (%)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BarChart(data = categoryPerformance.entries.map { Pair(it.key, it.value) })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Score History Graph
            if (results.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Overall Score Progression",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LineChart(scores = results.take(15).reversed().map { it.scorePercentage })
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ReportMetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
