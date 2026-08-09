package com.example.data.local

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Delete
    suspend fun deleteCategory(category: Category)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE categoryName = :category AND difficulty = :difficulty")
    suspend fun getQuestionsByCategoryAndDifficulty(category: String, difficulty: String): List<Question>

    @Query("SELECT * FROM questions WHERE categoryName = :category")
    suspend fun getQuestionsByCategory(category: String): List<Question>

    @Query("SELECT * FROM questions WHERE difficulty = :difficulty")
    suspend fun getQuestionsByDifficulty(difficulty: String): List<Question>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestionsList(): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int
}

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY dateTimestamp DESC")
    fun getResultsByUserId(userId: Long): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results ORDER BY scorePercentage DESC, timeTakenSeconds ASC LIMIT 50")
    fun getLeaderboardResults(): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY dateTimestamp DESC LIMIT 1")
    suspend fun getLatestResultByUserId(userId: Long): QuizResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(quizResult: QuizResult): Long

    @Query("SELECT COUNT(*) FROM quiz_results WHERE userId = :userId")
    suspend fun getQuizCountByUserId(userId: Long): Int

    @Query("SELECT MAX(scorePercentage) FROM quiz_results WHERE userId = :userId")
    suspend fun getHighestScoreByUserId(userId: Long): Float?

    @Query("SELECT AVG(scorePercentage) FROM quiz_results WHERE userId = :userId")
    suspend fun getAverageScoreByUserId(userId: Long): Float?

    @Query("SELECT SUM(correctCount) FROM quiz_results WHERE userId = :userId")
    suspend fun getTotalCorrectAnswersByUserId(userId: Long): Int?
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedTimestamp = :timestamp WHERE id = :id")
    suspend fun unlockAchievement(id: Long, timestamp: Long = System.currentTimeMillis())
}
