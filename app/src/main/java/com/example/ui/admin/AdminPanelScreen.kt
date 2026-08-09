package com.example.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Question
import com.example.ui.MainViewModel
import com.example.utils.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val questions by viewModel.allQuestions.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var questionToEdit by remember { mutableStateOf<Question?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val filteredQuestions = questions.filter { q ->
        (selectedCategoryFilter == "All" || q.categoryName == selectedCategoryFilter) &&
                (searchQuery.isBlank() || q.questionText.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Question Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Add Category")
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import CSV")
                    }
                    IconButton(onClick = {
                        val file = ExportUtils.exportQuestionsToCsv(context, questions)
                        if (file != null) {
                            Toast.makeText(context, "Exported ${questions.size} questions to ${file.name}", Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    questionToEdit = null
                    showAddEditDialog = true
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Question")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search questions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter
            ScrollableTabRow(
                selectedTabIndex = if (selectedCategoryFilter == "All") 0 else categories.indexOfFirst { it.name == selectedCategoryFilter } + 1,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedCategoryFilter == "All",
                    onClick = { selectedCategoryFilter = "All" }
                ) {
                    Text("All (${questions.size})", modifier = Modifier.padding(12.dp))
                }
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategoryFilter == cat.name,
                        onClick = { selectedCategoryFilter = cat.name }
                    ) {
                        Text(cat.name, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredQuestions) { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${q.categoryName} • ${q.difficulty}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            questionToEdit = q
                                            showAddEditDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteQuestion(q) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = q.questionText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditQuestionDialog(
            categories = categories.map { it.name },
            initialQuestion = questionToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { q ->
                if (q.id == 0L) {
                    viewModel.addQuestion(q) { showAddEditDialog = false }
                } else {
                    viewModel.updateQuestion(q) { showAddEditDialog = false }
                }
            }
        )
    }

    // CSV Import Dialog
    if (showImportDialog) {
        ImportQuestionsDialog(
            onDismiss = { showImportDialog = false },
            onImport = { csvText ->
                val parsed = ExportUtils.parseQuestionsFromCsv(csvText)
                if (parsed.isNotEmpty()) {
                    viewModel.importQuestions(parsed)
                    Toast.makeText(context, "Successfully imported ${parsed.size} questions!", Toast.LENGTH_SHORT).show()
                    showImportDialog = false
                } else {
                    Toast.makeText(context, "No valid questions found in CSV format", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Custom Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catDesc,
                        onValueChange = { catDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.addCategory(catName, catDesc)
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditQuestionDialog(
    categories: List<String>,
    initialQuestion: Question?,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var categoryName by remember { mutableStateOf(initialQuestion?.categoryName ?: categories.firstOrNull() ?: "Python") }
    var difficulty by remember { mutableStateOf(initialQuestion?.difficulty ?: "Medium") }
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var optA by remember { mutableStateOf(initialQuestion?.optionA ?: "") }
    var optB by remember { mutableStateOf(initialQuestion?.optionB ?: "") }
    var optC by remember { mutableStateOf(initialQuestion?.optionC ?: "") }
    var optD by remember { mutableStateOf(initialQuestion?.optionD ?: "") }
    var correctIdx by remember { mutableStateOf(initialQuestion?.correctAnswerIndex ?: 0) }
    var explanation by remember { mutableStateOf(initialQuestion?.explanation ?: "") }
    var hint by remember { mutableStateOf(initialQuestion?.hint ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialQuestion == null) "Add New Question" else "Edit Question") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optA,
                    onValueChange = { optA = it },
                    label = { Text("Option A") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optB,
                    onValueChange = { optB = it },
                    label = { Text("Option B") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optC,
                    onValueChange = { optC = it },
                    label = { Text("Option C") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optD,
                    onValueChange = { optD = it },
                    label = { Text("Option D") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Correct Option Index", fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("A (0)", "B (1)", "C (2)", "D (3)").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = correctIdx == idx,
                            onClick = { correctIdx = idx },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Difficulty", fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Hint (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank() && optA.isNotBlank() && optB.isNotBlank()) {
                        val q = Question(
                            id = initialQuestion?.id ?: 0L,
                            categoryName = categoryName,
                            difficulty = difficulty,
                            questionText = questionText,
                            optionA = optA,
                            optionB = optB,
                            optionC = optC,
                            optionD = optD,
                            correctAnswerIndex = correctIdx,
                            explanation = explanation,
                            hint = hint
                        )
                        onSave(q)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ImportQuestionsDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var csvText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Questions from CSV") },
        text = {
            Column {
                Text("Paste CSV data (Category,Difficulty,Question,OptionA,OptionB,OptionC,OptionD,CorrectIdx,Explanation,Hint):", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = { Text("Python,Medium,\"What is GIL?\",a,b,c,d,2,Explanation,Hint") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onImport(csvText) }) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
