package za.co.agentkit

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.text.NumberFormat
import java.util.Locale

object PdfExporter {
    fun propertyProfile(context: Context, property: PropertyItem): File {
        val lines = listOf(
            "PROPERTY PROFILE",
            "",
            property.title,
            property.suburb,
            if (property.address.isNotBlank()) property.address else "",
            listOf(property.city, property.province, property.postalCode).filter { it.isNotBlank() }.joinToString(", "),
            if (property.latitude != 0.0 || property.longitude != 0.0) "GPS: %.5f, %.5f".format(property.latitude, property.longitude) else "",
            money(property.price),
            "",
            "Status: " + property.status,
            "Type: " + property.propertyType,
            "Bedrooms: " + property.bedrooms,
            "Bathrooms: " + property.bathrooms,
            "",
            "SELLER",
            property.sellerName.ifBlank { "Not added" },
            property.sellerPhone.ifBlank { "Not added" },
            "",
            "NOTES",
            property.notes.ifBlank { "No notes recorded." }
        )
        return create(context, "property_" + safe(property.title) + ".pdf", lines)
    }

    fun sellerReport(context: Context, property: PropertyItem, viewings: List<ViewingItem>): File {
        val completed = viewings.count { it.status == "Completed" }
        val lines = mutableListOf(
            "SELLER PROPERTY UPDATE",
            "",
            property.title,
            property.suburb,
            if (property.address.isNotBlank()) property.address else "",
            "Asking price: " + money(property.price),
            "Status: " + property.status,
            "",
            "ACTIVITY",
            "Viewings scheduled: " + viewings.size,
            "Viewings completed: " + completed
        )
        if (viewings.isNotEmpty()) {
            lines += ""
            lines += "VIEWING HISTORY"
            viewings.take(12).forEach { v ->
                lines += v.date + " " + v.time + " - " + v.contactName + " (" + v.status + ")"
            }
        }
        lines += ""
        lines += "AGENT NOTE"
        lines += property.notes.ifBlank { "No additional notes recorded." }
        return create(context, "seller_report_" + safe(property.title) + ".pdf", lines)
    }

    fun share(context: Context, file: File, subject: String) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    private fun create(context: Context, name: String, lines: List<String>): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, name)
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val normal = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var canvas = page.canvas
        var y = 58f

        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
            canvas = page.canvas
            y = 58f
        }

        fun drawWrapped(text: String, title: Boolean) {
            paint.typeface = if (title) bold else normal
            paint.textSize = if (title) 18f else 12f
            paint.color = android.graphics.Color.rgb(16, 42, 67)
            val maxWidth = 495f
            if (text.isBlank()) {
                y += 14f
                return
            }
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                val candidate = if (line.isBlank()) word else line + " " + word
                if (paint.measureText(candidate) > maxWidth && line.isNotBlank()) {
                    if (y > 790f) newPage()
                    canvas.drawText(line, 50f, y, paint)
                    y += if (title) 25f else 19f
                    line = word
                } else line = candidate
            }
            if (y > 790f) newPage()
            canvas.drawText(line, 50f, y, paint)
            y += if (title) 25f else 19f
        }

        lines.forEachIndexed { index, line ->
            val isTitle = index == 0 || line == "SELLER" || line == "NOTES" || line == "ACTIVITY" || line == "VIEWING HISTORY" || line == "AGENT NOTE"
            drawWrapped(line, isTitle)
        }
        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun safe(value: String): String = value.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40)

    private fun money(value: Double): String {
        val f = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        f.maximumFractionDigits = 0
        return f.format(value)
    }
}
