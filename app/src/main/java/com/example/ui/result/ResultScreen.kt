package com.example.ui.result

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.BarChart
import com.example.ui.components.DonutChart
import com.example.ui.components.LineChart
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseError
import com.example.utils.ExportUtils
import com.example.utils.PdfGenerator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onReviewAnswers: () -> Unit,
    onRetakeQuiz: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val latestResult by viewModel.latestSubmittedResult.collectAsState()
    val userResults by viewModel.userResults.collectAsState()

    if (latestResult == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recent quiz result found.")
        }
        return
    }

    val res = latestResult!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Result Summary") },
                actions = {
                    IconButton(onClick = onHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Score Banner Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (res.scorePercentage >= 70f) Icons.Default.EmojiEvents else Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = GoldAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (res.scorePercentage >= 90f) "Outstanding!" else if (res.scorePercentage >= 70f) "Great Job!" else "Keep Practicing!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${res.categoryName} • ${res.difficulty}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ResultValuePill("Score", String.format(Locale.US, "%.1f%%", res.scorePercentage))
                        ResultValuePill("Grade", res.grade)
                        ResultValuePill("Time", "${res.timeTakenSeconds}s")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Charts Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Accuracy Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DonutChart(
                        correctCount = res.correctCount,
                        wrongCount = res.wrongCount,
                        skippedCount = res.skippedCount
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem("Correct (${res.correctCount})", EmeraldSuccess)
                        LegendItem("Wrong (${res.wrongCount})", RoseError)
                        LegendItem("Skipped (${res.skippedCount})", GoldAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score History Canvas Line Chart
            if (userResults.size >= 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Score History Trend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LineChart(scores = userResults.take(10).reversed().map { it.scorePercentage })
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Export Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val file = PdfGenerator.generateCertificatePdf(context, res)
                        if (file != null) {
                            Toast.makeText(context, "Certificate PDF exported to ${file.name}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Cert")
                }

                OutlinedButton(
                    onClick = {
                        val file = ExportUtils.exportResultToCsv(context, res)
                        if (file != null) {
                            Toast.makeText(context, "CSV exported: ${file.name}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Buttons Row
            Button(
                onClick = onReviewAnswers,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FindInPage, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review Answers & Explanations", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRetakeQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake Quiz")
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ResultValuePill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp)
    }
}
