package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "Category",
    val description: String = "",
    val isCustom: Boolean = false
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryName: String,
    val difficulty: String, // Easy, Medium, Hard
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int, // 0 for A, 1 for B, 2 for C, 3 for D
    val explanation: String = "",
    val hint: String = ""
)

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val categoryName: String,
    val difficulty: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val scorePercentage: Float,
    val grade: String,
    val timeTakenSeconds: Int,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val userAnswersJson: String = "" // JSON string storing questionId -> selectedIndex
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long? = null
)

data class AppSettings(
    val isDarkMode: Boolean = true,
    val fontSizeSp: Float = 16f,
    val soundEnabled: Boolean = true,
    val timerEnabled: Boolean = true,
    val timerSecondsPerQuestion: Int = 30,
    val negativeMarkingEnabled: Boolean = false
)
