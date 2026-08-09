package com.example.ui.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: MainViewModel,
    onQuizSubmitted: () -> Unit,
    onExitQuiz: () -> Unit
) {
    val session by viewModel.quizSession.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }
    var showQuestionGridSheet by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }

    if (session == null || session!!.questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading questions...")
            }
        }
        return
    }

    val currentSession = session!!
    val currentQuestion = currentSession.questions[currentSession.currentQuestionIndex]
    val selectedOption = currentSession.selectedAnswers[currentSession.currentQuestionIndex]
    val isBookmarked = currentSession.bookmarkedQuestions.contains(currentSession.currentQuestionIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${currentSession.categoryName} (${currentSession.difficulty})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Question ${currentSession.currentQuestionIndex + 1} of ${currentSession.questions.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showSubmitDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit")
                    }
                },
                actions = {
                    if (currentQuestion.hint.isNotBlank()) {
                        IconButton(onClick = { showHintDialog = true }) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = GoldAccent)
                        }
                    }
                    IconButton(onClick = { viewModel.toggleBookmarkCurrentQuestion() }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showQuestionGridSheet = true }) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid")
                    }
                }
            )
        },
        bottomBar = {
            BottomQuizNavigationRow(
                currentIndex = currentSession.currentQuestionIndex,
                totalQuestions = currentSession.questions.size,
                onPrevious = { viewModel.previousQuestion() },
                onSkip = { viewModel.nextQuestion() },
                onNext = {
                    if (currentSession.currentQuestionIndex == currentSession.questions.size - 1) {
                        showSubmitDialog = true
                    } else {
                        viewModel.nextQuestion()
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
                .verticalScroll(rememberScrollState())
        ) {
            // Timer & Progress Header Bar
            if (appSettings.timerEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (currentSession.remainingSeconds <= 5) RoseError else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Time Left: ${currentSession.remainingSeconds}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentSession.remainingSeconds <= 5) RoseError else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Answered: ${currentSession.selectedAnswers.size}/${currentSession.questions.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                val progress = (currentSession.currentQuestionIndex + 1).toFloat() / currentSession.questions.size
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Question Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Q${currentSession.currentQuestionIndex + 1}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentQuestion.questionText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Options List
            listOf(
                Pair("A", currentQuestion.optionA),
                Pair("B", currentQuestion.optionB),
                Pair("C", currentQuestion.optionC),
                Pair("D", currentQuestion.optionD)
            ).forEachIndexed { optionIndex, optionPair ->
                val isSelected = selectedOption == optionIndex
                OptionCard(
                    prefix = optionPair.first,
                    text = optionPair.second,
                    isSelected = isSelected,
                    onClick = { viewModel.selectOption(optionIndex) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Question Jump Bottom Sheet
    if (showQuestionGridSheet) {
        ModalBottomSheet(onDismissRequest = { showQuestionGridSheet = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Question Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    itemsIndexed(currentSession.questions) { idx, _ ->
                        val isAnswered = currentSession.selectedAnswers.containsKey(idx)
                        val isCurrent = idx == currentSession.currentQuestionIndex
                        val isMarked = currentSession.bookmarkedQuestions.contains(idx)

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isMarked -> GoldAccent
                                        isAnswered -> EmeraldSuccess
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable {
                                    viewModel.jumpToQuestion(idx)
                                    showQuestionGridSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent || isAnswered || isMarked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Submit Quiz Dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Quiz?") },
            text = {
                val answeredCount = currentSession.selectedAnswers.size
                val totalCount = currentSession.questions.size
                Text("You have answered $answeredCount of $totalCount questions. Are you sure you want to finish?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitQuiz()
                        onQuizSubmitted()
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Continue Quiz")
                }
            }
        )
    }

    // Hint Dialog
    if (showHintDialog) {
        AlertDialog(
            onDismissRequest = { showHintDialog = false },
            title = { Text("Hint") },
            text = { Text(currentQuestion.hint) },
            confirmButton = {
                TextButton(onClick = { showHintDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun OptionCard(
    prefix: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prefix,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

@Composable
fun BottomQuizNavigationRow(
    currentIndex: Int,
    totalQuestions: Int,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Prev")
            }

            TextButton(onClick = onSkip) {
                Text("Skip")
            }

            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (currentIndex == totalQuestions - 1) "Submit" else "Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (currentIndex == totalQuestions - 1) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}
