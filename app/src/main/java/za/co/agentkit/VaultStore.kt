package za.co.agentkit

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VaultDocument(
    val id: String,
    val name: String,
    val mime: String,
    val path: String,
    val createdAt: Long
)

data class VoiceMemo(
    val id: String,
    val title: String,
    val path: String,
    val createdAt: Long
)

class VaultStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("agentkit_vault", Context.MODE_PRIVATE)
    private val documentDir = File(context.filesDir, "vault/documents").apply { mkdirs() }
    private val voiceDir = File(context.filesDir, "vault/voice").apply { mkdirs() }

    fun documents(): List<VaultDocument> = runCatching {
        val a = JSONArray(prefs.getString("documents", "[]") ?: "[]")
        List(a.length()) { i ->
            val o = a.getJSONObject(i)
            VaultDocument(
                id = o.optString("id"),
                name = o.optString("name"),
                mime = o.optString("mime"),
                path = o.optString("path"),
                createdAt = o.optLong("createdAt")
            )
        }.filter { File(it.path).exists() }
    }.getOrDefault(emptyList())

    fun importDocument(uri: Uri): VaultDocument {
        val resolver = context.contentResolver
        var displayName = "document"
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) displayName = c.getString(0) ?: displayName
        }
        val mime = resolver.getType(uri) ?: DocumentUtils.mimeFromName(displayName)
        val safe = displayName.replace(Regex("[^A-Za-z0-9._ -]"), "_")
        val id = System.currentTimeMillis().toString()
        val target = File(documentDir, id + "_" + safe)
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open file" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
        val item = VaultDocument(id, displayName, mime, target.absolutePath, System.currentTimeMillis())
        saveDocuments(listOf(item) + documents())
        return item
    }

    fun deleteDocument(item: VaultDocument) {
        File(item.path).delete()
        saveDocuments(documents().filterNot { it.id == item.id })
    }

    fun voiceMemos(): List<VoiceMemo> = runCatching {
        val a = JSONArray(prefs.getString("voice", "[]") ?: "[]")
        List(a.length()) { i ->
            val o = a.getJSONObject(i)
            VoiceMemo(
                id = o.optString("id"),
                title = o.optString("title"),
                path = o.optString("path"),
                createdAt = o.optLong("createdAt")
            )
        }.filter { File(it.path).exists() }
    }.getOrDefault(emptyList())

    fun newVoiceFile(): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(voiceDir, "voice_" + stamp + ".m4a")
    }

    fun addVoice(file: File): VoiceMemo {
        val title = "Voice note " + SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()).format(Date())
        val item = VoiceMemo(System.currentTimeMillis().toString(), title, file.absolutePath, System.currentTimeMillis())
        saveVoice(listOf(item) + voiceMemos())
        return item
    }

    fun deleteVoice(item: VoiceMemo) {
        File(item.path).delete()
        saveVoice(voiceMemos().filterNot { it.id == item.id })
    }

    private fun saveDocuments(items: List<VaultDocument>) {
        val a = JSONArray()
        items.forEach {
            a.put(JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("mime", it.mime)
                .put("path", it.path)
                .put("createdAt", it.createdAt))
        }
        prefs.edit().putString("documents", a.toString()).apply()
    }

    private fun saveVoice(items: List<VoiceMemo>) {
        val a = JSONArray()
        items.forEach {
            a.put(JSONObject()
                .put("id", it.id)
                .put("title", it.title)
                .put("path", it.path)
                .put("createdAt", it.createdAt))
        }
        prefs.edit().putString("voice", a.toString()).apply()
    }
}
