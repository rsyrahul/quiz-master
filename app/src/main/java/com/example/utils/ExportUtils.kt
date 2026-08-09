package com.example.utils

import android.content.Context
import com.example.data.models.Question
import com.example.data.models.QuizResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun exportResultToCsv(context: Context, result: QuizResult): File? {
        val file = File(context.getExternalFilesDir(null) ?: context.cacheDir, "Quiz_Result_${result.id}.csv")
        return try {
            val writer = FileOutputStream(file).bufferedWriter()
            writer.write("User,Category,Difficulty,Score Percentage,Grade,Correct,Wrong,Skipped,Time Taken (s),Date\n")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = dateFormat.format(Date(result.dateTimestamp))
            writer.write(
                "\"${result.userName}\",\"${result.categoryName}\",\"${result.difficulty}\",${result.scorePercentage},\"${result.grade}\",${result.correctCount},${result.wrongCount},${result.skippedCount},${result.timeTakenSeconds},\"$dateStr\"\n"
            )
            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportQuestionsToCsv(context: Context, questions: List<Question>): File? {
        val file = File(context.getExternalFilesDir(null) ?: context.cacheDir, "Questions_Export.csv")
        return try {
            val writer = FileOutputStream(file).bufferedWriter()
            writer.write("Category,Difficulty,Question,OptionA,OptionB,OptionC,OptionD,CorrectIndex,Explanation,Hint\n")
            for (q in questions) {
                writer.write(
                    "\"${q.categoryName}\",\"${q.difficulty}\",\"${q.questionText.replace("\"", "\"\"")}\",\"${q.optionA.replace("\"", "\"\"")}\",\"${q.optionB.replace("\"", "\"\"")}\",\"${q.optionC.replace("\"", "\"\"")}\",\"${q.optionD.replace("\"", "\"\"")}\",${q.correctAnswerIndex},\"${q.explanation.replace("\"", "\"\"")}\",\"${q.hint.replace("\"", "\"\"")}\"\n"
                )
            }
            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseQuestionsFromCsv(csvContent: String): List<Question> {
        val list = mutableListOf<Question>()
        val lines = csvContent.lines()
        for ((index, line) in lines.withIndex()) {
            if (index == 0 || line.isBlank()) continue
            val parts = parseCsvLine(line)
            if (parts.size >= 8) {
                val category = parts[0]
                val diff = parts[1]
                val qText = parts[2]
                val optA = parts[3]
                val optB = parts[4]
                val optC = parts[5]
                val optD = parts[6]
                val correctIdx = parts[7].toIntOrNull() ?: 0
                val exp = if (parts.size > 8) parts[8] else ""
                val hint = if (parts.size > 9) parts[9] else ""

                list.add(
                    Question(
                        categoryName = category,
                        difficulty = diff,
                        questionText = qText,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        optionD = optD,
                        correctAnswerIndex = correctIdx,
                        explanation = exp,
                        hint = hint
                    )
                )
            }
        }
        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
