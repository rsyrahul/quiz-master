package com.example.data.repository

import com.example.data.local.*
import com.example.data.models.*
import com.example.utils.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val questionDao = db.questionDao()
    private val quizResultDao = db.quizResultDao()
    private val achievementDao = db.achievementDao()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
    }

    suspend fun login(emailOrUsername: String, password: String): Result<User> {
        val user = if (emailOrUsername.contains("@")) {
            userDao.getUserByEmail(emailOrUsername)
        } else {
            userDao.getUserByUsername(emailOrUsername)
        } ?: return Result.failure(Exception("User not found"))

        val isValid = PasswordHasher.verifyPassword(password, user.passwordHash, user.salt)
        return if (isValid) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<User> {
        if (userDao.getUserByEmail(email) != null) {
            return Result.failure(Exception("Email is already registered"))
        }
        if (userDao.getUserByUsername(username) != null) {
            return Result.failure(Exception("Username is already taken"))
        }

        val (hash, salt) = PasswordHasher.hashPassword(password)
        val user = User(
            username = username,
            email = email,
            passwordHash = hash,
            salt = salt,
            isAdmin = false
        )
        val id = userDao.insertUser(user)
        val createdUser = user.copy(id = id)
        _currentUser.value = createdUser
        return Result.success(createdUser)
    }

    suspend fun forgotPasswordReset(email: String, newPassword: String): Result<Boolean> {
        val user = userDao.getUserByEmail(email) ?: return Result.failure(Exception("Email not found"))
        val (hash, salt) = PasswordHasher.hashPassword(newPassword)
        val updatedUser = user.copy(passwordHash = hash, salt = salt)
        userDao.insertUser(updatedUser)
        return Result.success(true)
    }

    fun logout() {
        _currentUser.value = null
    }

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun addCategory(name: String, iconName: String = "Category", description: String = "") {
        categoryDao.insertCategory(Category(name = name, iconName = iconName, description = description, isCustom = true))
    }

    // Questions
    val allQuestions: Flow<List<Question>> = questionDao.getAllQuestions()

    suspend fun getQuestionsForQuiz(category: String, difficulty: String): List<Question> {
        val questions = if (category == "All") {
            if (difficulty == "All") questionDao.getAllQuestionsList()
            else questionDao.getQuestionsByDifficulty(difficulty)
        } else {
            if (difficulty == "All") questionDao.getQuestionsByCategory(category)
            else questionDao.getQuestionsByCategoryAndDifficulty(category, difficulty)
        }
        return questions.shuffled()
    }

    suspend fun addQuestion(question: Question): Long {
        return questionDao.insertQuestion(question)
    }

    suspend fun updateQuestion(question: Question) {
        questionDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: Question) {
        questionDao.deleteQuestion(question)
    }

    suspend fun importQuestions(questions: List<Question>) {
        questionDao.insertQuestions(questions)
    }

    // Quiz Submission & Evaluation
    suspend fun saveQuizResult(
        categoryName: String,
        difficulty: String,
        totalQuestions: Int,
        correctCount: Int,
        wrongCount: Int,
        skippedCount: Int,
        timeTakenSeconds: Int,
        userAnswersJson: String
    ): QuizResult {
        val user = _currentUser.value ?: User(id = 0, username = "Guest", email = "guest@app.com", passwordHash = "", salt = "")
        
        // Calculate Score Percentage considering negative marking if enabled
        val isNegativeMarking = _settings.value.negativeMarkingEnabled
        val rawScore = if (isNegativeMarking) {
            (correctCount * 1.0f) - (wrongCount * 0.25f)
        } else {
            correctCount * 1.0f
        }
        val maxScore = totalQuestions * 1.0f
        val pct = if (maxScore > 0) ((rawScore / maxScore) * 100f).coerceIn(0f, 100f) else 0f

        val grade = when {
            pct >= 90f -> "A+"
            pct >= 80f -> "A"
            pct >= 70f -> "B"
            pct >= 60f -> "C"
            else -> "F"
        }

        val result = QuizResult(
            userId = user.id,
            userName = user.username,
            categoryName = categoryName,
            difficulty = difficulty,
            totalQuestions = totalQuestions,
            correctCount = correctCount,
            wrongCount = wrongCount,
            skippedCount = skippedCount,
            scorePercentage = pct,
            grade = grade,
            timeTakenSeconds = timeTakenSeconds,
            userAnswersJson = userAnswersJson
        )

        val resultId = quizResultDao.insertQuizResult(result)
        val savedResult = result.copy(id = resultId)

        // Check & Unlock Achievements asynchronously
        checkAndUnlockAchievements(user.id, savedResult)

        return savedResult
    }

    fun getUserResults(userId: Long): Flow<List<QuizResult>> {
        return quizResultDao.getResultsByUserId(userId)
    }

    val leaderboardResults: Flow<List<QuizResult>> = quizResultDao.getLeaderboardResults()

    suspend fun getUserStats(userId: Long): UserStats {
        val totalPlayed = quizResultDao.getQuizCountByUserId(userId)
        val highestScore = quizResultDao.getHighestScoreByUserId(userId) ?: 0f
        val avgScore = quizResultDao.getAverageScoreByUserId(userId) ?: 0f
        val latestResult = quizResultDao.getLatestResultByUserId(userId)
        val totalCorrect = quizResultDao.getTotalCorrectAnswersByUserId(userId) ?: 0

        return UserStats(
            totalPlayed = totalPlayed,
            highestScore = highestScore,
            avgScore = avgScore,
            totalCorrect = totalCorrect,
            recentQuiz = latestResult
        )
    }

    // Achievements
    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()

    private suspend fun checkAndUnlockAchievements(userId: Long, result: QuizResult) {
        val stats = getUserStats(userId)

        // 1. First Quiz
        if (stats.totalPlayed >= 1) {
            achievementDao.unlockAchievement(1)
        }
        // 2. 100% Score
        if (result.scorePercentage >= 99.9f) {
            achievementDao.unlockAchievement(2)
        }
        // 3. 10 Quizzes Completed
        if (stats.totalPlayed >= 10) {
            achievementDao.unlockAchievement(3)
        }
        // 4. 50 Correct Answers
        if (stats.totalCorrect >= 50) {
            achievementDao.unlockAchievement(4)
        }
        // 5. Quiz Champion
        if (stats.totalPlayed >= 5 && stats.avgScore >= 90f) {
            achievementDao.unlockAchievement(5)
        }
    }
}

data class UserStats(
    val totalPlayed: Int,
    val highestScore: Float,
    val avgScore: Float,
    val totalCorrect: Int,
    val recentQuiz: QuizResult?
)
