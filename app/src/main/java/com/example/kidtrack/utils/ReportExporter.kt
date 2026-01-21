package com.example.kidtrack.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.core.content.FileProvider
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.model.ReportStatistics
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    /**
     * Export report as PDF
     */
    fun exportReportAsPdf(
        context: Context,
        statistics: ReportStatistics,
        activities: List<Activity>
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            // Title
            paint.textSize = 24f
            paint.color = Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("KidTrack Activity Report", 50f, 50f, paint)

            // Date
            paint.textSize = 14f
            paint.isFakeBoldText = false
            paint.color = Color.GRAY
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, 80f, paint)

            // Statistics
            var yPos = 130f
            paint.textSize = 18f
            paint.color = Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("Statistics Summary", 50f, yPos, paint)

            yPos += 40f
            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Total Activities: ${statistics.totalActivities}", 70f, yPos, paint)
            yPos += 25f
            canvas.drawText("Activities This Week: ${statistics.thisWeekActivities}", 70f, yPos, paint)
            yPos += 25f
            canvas.drawText("Completion Rate: ${statistics.completionRate}%", 70f, yPos, paint)
            yPos += 25f
            canvas.drawText("Categories: ${statistics.categoryBreakdown.size}", 70f, yPos, paint)

            // Activities List
            yPos += 50f
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Recent Activities", 50f, yPos, paint)

            yPos += 40f
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            activities.take(15).forEach { activity ->
                if (yPos > 750) return@forEach // Prevent overflow
                canvas.drawText("${activity.category}: ${activity.description}", 70f, yPos, paint)
                yPos += 20f
                paint.color = Color.GRAY
                val dateString = DateTimeUtils.timestampToDateString(activity.dateTimestamp)
                val timeString = DateTimeUtils.minutesToTimeString(activity.timeMinutes)
                canvas.drawText("$dateString at $timeString", 90f, yPos, paint)
                yPos += 30f
                paint.color = Color.BLACK
            }

            pdfDocument.finishPage(page)

            // Save PDF
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "KidTrack_Report_$timestamp.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Export view as image
     */
    fun exportViewAsImage(context: Context, view: View): File? {
        return try {
            // Create bitmap from view
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "KidTrack_Report_$timestamp.png"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            bitmap.recycle()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Share file via Intent
     */
    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
    }
}
