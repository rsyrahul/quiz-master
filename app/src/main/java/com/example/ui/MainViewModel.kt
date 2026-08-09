package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.models.*
import com.example.data.repository.QuizRepository
import com.example.data.repository.UserStats
import com.example.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuizRepository(AppDatabase.getDatabase(application))
    val soundManager = SoundManager()

    val currentUser: StateFlow<User?> = repository.currentUser
    val appSettings: StateFlow<AppSettings> = repository.settings
    val allCategories: StateFlow<List<Category>> = repository.allCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allQuestions: StateFlow<List<Question>> = repository.allQuestions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val achievements: StateFlow<List<Achievement>> = repository.allAchievements.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val leaderboard: StateFlow<List<QuizResult>> = repository.leaderboardResults.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _userStats = MutableStateFlow(UserStats(0, 0f, 0f, 0, null))
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _userResults = MutableStateFlow<List<QuizResult>>(emptyList())
    val userResults: StateFlow<List<QuizResult>> = _userResults.asStateFlow()

    // Active Quiz Session State
    private val _quizSession = MutableStateFlow<QuizSessionState?>(null)
    val quizSession: StateFlow<QuizSessionState?> = _quizSession.asStateFlow()

    // Recent Submitted Result State
    private val _latestSubmittedResult = MutableStateFlow<QuizResult?>(null)
    val latestSubmittedResult: StateFlow<QuizResult?> = _latestSubmittedResult.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    loadUserStatsAndResults(user.id)
                } else {
                    _userStats.value = UserStats(0, 0f, 0f, 0, null)
                    _userResults.value = emptyList()
                }
            }
        }
    }

    fun loadUserStatsAndResults(userId: Long) {
        viewModelScope.launch {
            _userStats.value = repository.getUserStats(userId)
            repository.getUserResults(userId).collect { results ->
                _userResults.value = results
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        repository.updateSettings(newSettings)
    }

    fun login(emailOrUser: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(emailOrUser, pass)
            if (result.isSuccess) {
                if (appSettings.value.soundEnabled) soundManager.playClickSound()
                onResult(true, "Login successful")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(username: String, email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.register(username, email, pass)
            if (result.isSuccess) {
                if (appSettings.value.soundEnabled) soundManager.playClickSound()
                onResult(true, "Registration successful")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun forgotPassword(email: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.forgotPasswordReset(email, newPass)
            if (result.isSuccess) {
                onResult(true, "Password updated successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to reset password")
            }
        }
    }

    fun logout() {
        repository.logout()
        _quizSession.value = null
        _latestSubmittedResult.value = null
    }

    // Active Quiz Logic
    fun startQuiz(categoryName: String, difficulty: String) {
        viewModelScope.launch {
            val questions = repository.getQuestionsForQuiz(categoryName, difficulty)
            if (questions.isEmpty()) {
                return@launch
            }

            val session = QuizSessionState(
                categoryName = categoryName,
                difficulty = difficulty,
                questions = questions,
                currentQuestionIndex = 0,
                selectedAnswers = mutableMapOf(),
                bookmarkedQuestions = mutableSetOf(),
                remainingSeconds = appSettings.value.timerSecondsPerQuestion,
                totalTimeTakenSeconds = 0
            )

            _quizSession.value = session
            if (appSettings.value.timerEnabled) {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _quizSession.value ?: break
                if (current.isCompleted) break

                val newRemaining = current.remainingSeconds - 1
                val newTotalTime = current.totalTimeTakenSeconds + 1

                if (newRemaining <= 5 && newRemaining > 0 && appSettings.value.soundEnabled) {
                    soundManager.playTimerWarningSound()
                }

                if (newRemaining <= 0) {
                    // Time up for current question -> skip or go next
                    if (current.currentQuestionIndex < current.questions.size - 1) {
                        _quizSession.value = current.copy(
                            currentQuestionIndex = current.currentQuestionIndex + 1,
                            remainingSeconds = appSettings.value.timerSecondsPerQuestion,
                            totalTimeTakenSeconds = newTotalTime
                        )
                    } else {
                        // Reached end -> Auto Submit
                        _quizSession.value = current.copy(
                            totalTimeTakenSeconds = newTotalTime
                        )
                        submitQuiz()
                        break
                    }
                } else {
                    _quizSession.value = current.copy(
                        remainingSeconds = newRemaining,
                        totalTimeTakenSeconds = newTotalTime
                    )
                }
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        val current = _quizSession.value ?: return
        val map = current.selectedAnswers.toMutableMap()
        map[current.currentQuestionIndex] = optionIndex
        _quizSession.value = current.copy(selectedAnswers = map)
        if (appSettings.value.soundEnabled) soundManager.playClickSound()
    }

    fun toggleBookmarkCurrentQuestion() {
        val current = _quizSession.value ?: return
        val set = current.bookmarkedQuestions.toMutableSet()
        val idx = current.currentQuestionIndex
        if (set.contains(idx)) {
            set.remove(idx)
        } else {
            set.add(idx)
        }
        _quizSession.value = current.copy(bookmarkedQuestions = set)
    }

    fun nextQuestion() {
        val current = _quizSession.value ?: return
        if (current.currentQuestionIndex < current.questions.size - 1) {
            _quizSession.value = current.copy(
                currentQuestionIndex = current.currentQuestionIndex + 1,
                remainingSeconds = appSettings.value.timerSecondsPerQuestion
            )
            if (appSettings.value.soundEnabled) soundManager.playClickSound()
        }
    }

    fun previousQuestion() {
        val current = _quizSession.value ?: return
        if (current.currentQuestionIndex > 0) {
            _quizSession.value = current.copy(
                currentQuestionIndex = current.currentQuestionIndex - 1,
                remainingSeconds = appSettings.value.timerSecondsPerQuestion
            )
            if (appSettings.value.soundEnabled) soundManager.playClickSound()
        }
    }

    fun jumpToQuestion(index: Int) {
        val current = _quizSession.value ?: return
        if (index in 0 until current.questions.size) {
            _quizSession.value = current.copy(
                currentQuestionIndex = index,
                remainingSeconds = appSettings.value.timerSecondsPerQuestion
            )
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val current = _quizSession.value ?: return

        var correctCount = 0
        var wrongCount = 0
        var skippedCount = 0

        for ((idx, question) in current.questions.withIndex()) {
            val selected = current.selectedAnswers[idx]
            if (selected == null) {
                skippedCount++
            } else if (selected == question.correctAnswerIndex) {
                correctCount++
            } else {
                wrongCount++
            }
        }

        if (appSettings.value.soundEnabled) {
            if (correctCount >= (current.questions.size / 2)) {
                soundManager.playCompletionSound()
            } else {
                soundManager.playWrongSound()
            }
        }

        // Build User Answers JSON string
        val jsonAnswers = current.selectedAnswers.entries.joinToString(prefix = "{", postfix = "}") {
            "\"${current.questions[it.key].id}\":${it.value}"
        }

        viewModelScope.launch {
            val savedResult = repository.saveQuizResult(
                categoryName = current.categoryName,
                difficulty = current.difficulty,
                totalQuestions = current.questions.size,
                correctCount = correctCount,
                wrongCount = wrongCount,
                skippedCount = skippedCount,
                timeTakenSeconds = current.totalTimeTakenSeconds,
                userAnswersJson = jsonAnswers
            )

            _latestSubmittedResult.value = savedResult
            _quizSession.value = current.copy(isCompleted = true)
            currentUser.value?.let { loadUserStatsAndResults(it.id) }
        }
    }

    // Admin Operations
    fun addQuestion(question: Question, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addQuestion(question)
            onComplete()
        }
    }

    fun updateQuestion(question: Question, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateQuestion(question)
            onComplete()
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }

    fun importQuestions(questions: List<Question>) {
        viewModelScope.launch {
            repository.importQuestions(questions)
        }
    }

    fun addCategory(name: String, description: String) {
        viewModelScope.launch {
            repository.addCategory(name = name, description = description)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

data class QuizSessionState(
    val categoryName: String,
    val difficulty: String,
    val questions: List<Question>,
    val currentQuestionIndex: Int,
    val selectedAnswers: Map<Int, Int>, // index -> chosenOptionIndex
    val bookmarkedQuestions: Set<Int>,
    val remainingSeconds: Int,
    val totalTimeTakenSeconds: Int,
    val isCompleted: Boolean = false
)
