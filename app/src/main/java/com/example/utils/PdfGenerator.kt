package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.models.QuizResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateCertificatePdf(context: Context, result: QuizResult): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 Landscape
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val width = 842f
        val height = 595f

        // Background Fill
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Outer Border Frame
        val borderPaint = Paint().apply {
            color = Color.parseColor("#1E1B4B") // Dark Indigo
            style = Paint.Style.STROKE
            strokeWidth = 12f
        }
        canvas.drawRect(RectF(20f, 20f, width - 20f, height - 20f), borderPaint)

        // Inner Gold Border
        val goldBorderPaint = Paint().apply {
            color = Color.parseColor("#D97706") // Gold Accent
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(RectF(32f, 32f, width - 32f, height - 32f), goldBorderPaint)

        // Header Title
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1B4B")
            textSize = 36f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("CERTIFICATE OF ACHIEVEMENT", width / 2, 100f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("THIS CERTIFICATE IS PROUDLY PRESENTED TO", width / 2, 150f, subTitlePaint)

        // Recipient Name
        val namePaint = Paint().apply {
            color = Color.parseColor("#D97706")
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(result.userName.uppercase(Locale.getDefault()), width / 2, 220f, namePaint)

        // Line under name
        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 2f
        }
        canvas.drawLine(width / 2 - 200, 235f, width / 2 + 200, 235f, linePaint)

        // Description text
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "for successfully completing the ${result.categoryName} Quiz (${result.difficulty} Level)",
            width / 2,
            280f,
            bodyPaint
        )

        // Score Badge Box
        val scoreBoxPaint = Paint().apply {
            color = Color.parseColor("#EEF2FF")
            style = Paint.Style.FILL
        }
        val scoreBox = RectF(width / 2 - 150, 310f, width / 2 + 150, 390f)
        canvas.drawRoundRect(scoreBox, 16f, 16f, scoreBoxPaint)

        val scoreTextPaint = Paint().apply {
            color = Color.parseColor("#4338CA")
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val pctString = String.format(Locale.US, "Score: %.1f%%  |  Grade: %s", result.scorePercentage, result.grade)
        canvas.drawText(pctString, width / 2, 360f, scoreTextPaint)

        // Details Footer
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date(result.dateTimestamp))

        val footerTextPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 14f
        }
        canvas.drawText("Date: $dateStr", 70f, 480f, footerTextPaint)
        canvas.drawText("Certificate ID: QMP-${result.id}-${result.dateTimestamp / 1000}", 70f, 510f, footerTextPaint)

        // Signature Line
        val sigLinePaint = Paint().apply {
            color = Color.parseColor("#1E1B4B")
            strokeWidth = 2f
        }
        canvas.drawLine(width - 250f, 480f, width - 70f, 480f, sigLinePaint)

        val sigTextPaint = Paint().apply {
            color = Color.parseColor("#1E1B4B")
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Quiz Master Pro Board", width - 160f, 505f, sigTextPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache or external files directory
        val file = File(context.getExternalFilesDir(null) ?: context.cacheDir, "Certificate_QMP_${result.id}.pdf")
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
