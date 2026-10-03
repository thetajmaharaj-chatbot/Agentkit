package za.co.agentkit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.util.zip.ZipFile

object DocumentUtils {
    fun mimeFromName(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "pdf" -> "application/pdf"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "xls" -> "application/vnd.ms-excel"
        "doc" -> "application/msword"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "txt" -> "text/plain"
        "csv" -> "text/csv"
        else -> "application/octet-stream"
    }

    fun typeLabel(item: VaultDocument): String = when {
        item.mime == "application/pdf" || item.name.endsWith(".pdf", true) -> "PDF"
        item.name.endsWith(".docx", true) -> "DOCX"
        item.name.endsWith(".xlsx", true) -> "XLSX"
        item.name.endsWith(".xls", true) -> "XLS"
        item.name.endsWith(".doc", true) -> "DOC"
        item.mime.startsWith("image/") -> "IMAGE"
        item.name.endsWith(".csv", true) -> "CSV"
        item.mime.startsWith("text/") -> "TEXT"
        else -> "FILE"
    }

    fun previewText(item: VaultDocument): String = runCatching {
        when {
            item.name.endsWith(".docx", true) -> readDocx(File(item.path))
            item.name.endsWith(".xlsx", true) -> readXlsx(File(item.path))
            item.name.endsWith(".csv", true) || item.mime.startsWith("text/") -> File(item.path).readText()
            item.name.endsWith(".xls", true) -> "Legacy .xls file stored in AgentKit. Full in-app binary XLS rendering is planned; .xlsx files preview now."
            item.name.endsWith(".doc", true) -> "Legacy .doc file stored in AgentKit. Full in-app binary DOC rendering is planned; .docx files preview now."
            else -> ""
        }
    }.getOrElse { "Preview could not be generated: " + (it.message ?: "unknown error") }

    fun decodeScaledImage(path: String, maxSize: Int = 1600): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private fun readDocx(file: File): String {
        ZipFile(file).use { zip ->
            val entry = zip.getEntry("word/document.xml") ?: return "DOCX content not found."
            val xml = zip.getInputStream(entry).bufferedReader().use { it.readText() }
            return xml
                .replace("</w:p>", "\n")
                .replace("</w:tr>", "\n")
                .replace("</w:tc>", "\t")
                .replace(Regex("<[^>]+>"), "")
                .let(::decodeXml)
                .replace(Regex("\n{3,}"), "\n\n")
                .trim()
        }
    }

    private fun readXlsx(file: File): String {
        ZipFile(file).use { zip ->
            val shared = mutableListOf<String>()
            zip.getEntry("xl/sharedStrings.xml")?.let { entry ->
                val xml = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                Regex("<t[^>]*>(.*?)</t>", setOf(RegexOption.DOT_MATCHES_ALL))
                    .findAll(xml).forEach { shared += decodeXml(it.groupValues[1]) }
            }
            val sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml")
                ?: zip.entries().asSequence().firstOrNull { it.name.startsWith("xl/worksheets/sheet") && it.name.endsWith(".xml") }
                ?: return "Workbook contains no readable worksheet."
            val xml = zip.getInputStream(sheetEntry).bufferedReader().use { it.readText() }
            val rows = Regex("<row[^>]*>(.*?)</row>", setOf(RegexOption.DOT_MATCHES_ALL)).findAll(xml)
            val out = StringBuilder()
            rows.forEach { row ->
                val cells = Regex("<c([^>]*)>(.*?)</c>", setOf(RegexOption.DOT_MATCHES_ALL)).findAll(row.groupValues[1])
                val values = cells.map { cell ->
                    val attrs = cell.groupValues[1]
                    val body = cell.groupValues[2]
                    val inline = Regex("<t[^>]*>(.*?)</t>", setOf(RegexOption.DOT_MATCHES_ALL)).find(body)?.groupValues?.get(1)
                    val raw = Regex("<v>(.*?)</v>", setOf(RegexOption.DOT_MATCHES_ALL)).find(body)?.groupValues?.get(1)
                    when {
                        inline != null -> decodeXml(inline)
                        attrs.contains("t=\"s\"") && raw != null -> shared.getOrNull(raw.toIntOrNull() ?: -1) ?: raw
                        raw != null -> raw
                        else -> ""
                    }
                }.toList()
                out.append(values.joinToString("    ")).append('\n')
            }
            return out.toString().trim().ifBlank { "Worksheet is empty." }
        }
    }

    private fun decodeXml(text: String): String = text
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
}
