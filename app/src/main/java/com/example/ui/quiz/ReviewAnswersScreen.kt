package com.example.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAnswersScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val session by viewModel.quizSession.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    if (session == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active session to review.")
        }
        return
    }

    val currentSession = session!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Answers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Correct", "Wrong", "Bookmarked").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            val filteredQuestions = currentSession.questions.mapIndexed { idx, q ->
                Triple(idx, q, currentSession.selectedAnswers[idx])
            }.filter { (idx, q, selected) ->
                when (selectedFilter) {
                    "Correct" -> selected == q.correctAnswerIndex
                    "Wrong" -> selected != null && selected != q.correctAnswerIndex
                    "Bookmarked" -> currentSession.bookmarkedQuestions.contains(idx)
                    else -> true
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(filteredQuestions) { _, (origIdx, q, selected) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${origIdx + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        selected == q.correctAnswerIndex -> EmeraldSuccess.copy(alpha = 0.2f)
                                        selected != null -> RoseError.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            selected == q.correctAnswerIndex -> "Correct"
                                            selected != null -> "Wrong"
                                            else -> "Skipped"
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            selected == q.correctAnswerIndex -> EmeraldSuccess
                                            selected != null -> RoseError
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = q.questionText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Options Review List
                            listOf(q.optionA, q.optionB, q.optionC, q.optionD).forEachIndexed { optIdx, optText ->
                                val isCorrectOption = optIdx == q.correctAnswerIndex
                                val isUserSelected = selected == optIdx

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isCorrectOption -> EmeraldSuccess.copy(alpha = 0.15f)
                                        isUserSelected -> RoseError.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${'A' + optIdx}. $optText",
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            fontWeight = if (isCorrectOption || isUserSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isCorrectOption) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess)
                                        } else if (isUserSelected) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = RoseError)
                                        }
                                    }
                                }
                            }

                            if (q.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Explanation: ${q.explanation}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
