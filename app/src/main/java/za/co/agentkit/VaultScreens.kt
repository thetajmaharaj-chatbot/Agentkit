package za.co.agentkit

import android.Manifest
import android.graphics.pdf.PdfRenderer
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VaultInk = androidx.compose.ui.graphics.Color(0xFF102A43)
private val VaultBrand = androidx.compose.ui.graphics.Color(0xFF0A7A67)
private val VaultSoft = androidx.compose.ui.graphics.Color(0xFFE8F5F1)
private val VaultMuted = androidx.compose.ui.graphics.Color(0xFF64748B)

@Composable
fun DocumentVaultScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { VaultStore(context.applicationContext) }
    var documents by remember { mutableStateOf(store.documents()) }
    var selected by remember { mutableStateOf<VaultDocument?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { store.importDocument(uri) }
            documents = store.documents()
        }
    }

    if (selected != null) {
        DocumentPreviewScreen(selected!!, onBack = { selected = null }, onDelete = {
            store.deleteDocument(it)
            documents = store.documents()
            selected = null
        })
        return
    }

    VaultPage("Document vault", "Keep your working files inside AgentKit", onBack) {
        Button(
            onClick = { picker.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.UploadFile, null)
            Spacer(Modifier.width(8.dp))
            Text("Import document")
        }

        Text(
            "IN-APP PREVIEW",
            color = VaultMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        Text("PDF • DOCX • XLSX • JPG • PNG • WEBP • TXT • CSV", color = VaultMuted, fontSize = 13.sp)

        if (documents.isEmpty()) {
            VaultEmpty("No documents yet", "Import mandates, IDs, disclosure forms, spreadsheets, property photos or supporting files.")
        } else {
            documents.forEach { item ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { selected = item },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).background(VaultSoft, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconFor(item), null, tint = VaultBrand)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(item.name, fontWeight = FontWeight.SemiBold, color = VaultInk, maxLines = 2)
                            Text(DocumentUtils.typeLabel(item) + " • " + dateText(item.createdAt), color = VaultMuted, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = VaultMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentPreviewScreen(item: VaultDocument, onBack: () -> Unit, onDelete: (VaultDocument) -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    VaultPage(item.name, DocumentUtils.typeLabel(item), onBack) {
        when {
            item.mime == "application/pdf" || item.name.endsWith(".pdf", true) -> PdfPreview(item.path)
            item.mime.startsWith("image/") || item.name.endsWith(".jpg", true) || item.name.endsWith(".jpeg", true) || item.name.endsWith(".png", true) || item.name.endsWith(".webp", true) -> ImagePreview(item.path)
            else -> {
                val text = remember(item.path) { DocumentUtils.previewText(item) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text(
                        text.ifBlank { "A visual preview is not available for this file type yet. The original file is safely stored in AgentKit." },
                        modifier = Modifier.padding(18.dp),
                        color = VaultInk,
                        fontSize = 13.sp
                    )
                }
            }
        }
        OutlinedButton(
            onClick = { confirmDelete = true },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Icon(Icons.Default.DeleteOutline, null)
            Spacer(Modifier.width(8.dp))
            Text("Delete from vault")
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete document?") },
            text = { Text("This permanently removes the copy stored inside AgentKit.") },
            confirmButton = { TextButton(onClick = { onDelete(item) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun PdfPreview(path: String) {
    var pageIndex by remember { mutableIntStateOf(0) }
    val info = remember(path, pageIndex) {
        runCatching {
            val pfd = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val safeIndex = pageIndex.coerceIn(0, (renderer.pageCount - 1).coerceAtLeast(0))
            val page = renderer.openPage(safeIndex)
            val scale = (1200f / page.width).coerceAtMost(2f)
            val bitmap = android.graphics.Bitmap.createBitmap(
                (page.width * scale).toInt().coerceAtLeast(1),
                (page.height * scale).toInt().coerceAtLeast(1),
                android.graphics.Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val count = renderer.pageCount
            page.close()
            renderer.close()
            pfd.close()
            Pair(bitmap, count)
        }.getOrNull()
    }

    if (info == null) {
        VaultEmpty("PDF preview unavailable", "The file is stored safely but could not be rendered.")
        return
    }

    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)) {
        Image(
            bitmap = info.first.asImageBitmap(),
            contentDescription = "PDF page",
            modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp).padding(8.dp),
            contentScale = ContentScale.Fit
        )
    }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { if (pageIndex > 0) pageIndex-- }, enabled = pageIndex > 0) { Text("Previous") }
        Text("Page " + (pageIndex + 1) + " of " + info.second, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = VaultMuted)
        OutlinedButton(onClick = { if (pageIndex < info.second - 1) pageIndex++ }, enabled = pageIndex < info.second - 1) { Text("Next") }
    }
}

@Composable
private fun ImagePreview(path: String) {
    val bitmap = remember(path) { DocumentUtils.decodeScaledImage(path) }
    if (bitmap == null) {
        VaultEmpty("Image preview unavailable", "The image file is stored in the vault.")
    } else {
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Document image",
                modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun VoiceNotesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { VaultStore(context.applicationContext) }
    var notes by remember { mutableStateOf(store.voiceMemos()) }
    var recording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingId by remember { mutableStateOf<String?>(null) }

    fun beginRecording() {
        val file = store.newVoiceFile()
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(96000)
        r.setAudioSamplingRate(44100)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        currentFile = file
        recording = true
    }

    fun stopRecording(save: Boolean) {
        val file = currentFile
        runCatching { recorder?.stop() }
        recorder?.release()
        recorder = null
        currentFile = null
        recording = false
        if (file != null) {
            if (save && file.exists() && file.length() > 0) store.addVoice(file) else file.delete()
            notes = store.voiceMemos()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) runCatching { beginRecording() }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { recorder?.release() }
            runCatching { player?.release() }
        }
    }

    VaultPage("Voice notes", "Record private memos without leaving AgentKit", onBack) {
        Button(
            onClick = {
                if (recording) {
                    stopRecording(true)
                } else {
                    val granted = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (granted) runCatching { beginRecording() }
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = if (recording) MaterialTheme.colorScheme.error else VaultBrand),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(if (recording) Icons.Default.Stop else Icons.Default.Mic, null)
            Spacer(Modifier.width(8.dp))
            Text(if (recording) "Stop & save recording" else "Record voice note")
        }

        if (recording) {
            Text("Recording… tap Stop & save when finished.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
        }

        if (notes.isEmpty()) {
            VaultEmpty("No voice notes yet", "Record seller instructions, viewing impressions, reminders or property observations.")
        } else {
            notes.forEach { note ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (playingId == note.id) {
                                player?.stop()
                                player?.release()
                                player = null
                                playingId = null
                            } else {
                                player?.release()
                                val p = MediaPlayer()
                                p.setDataSource(note.path)
                                p.prepare()
                                p.setOnCompletionListener {
                                    it.release()
                                    player = null
                                    playingId = null
                                }
                                p.start()
                                player = p
                                playingId = note.id
                            }
                        }) {
                            Icon(if (playingId == note.id) Icons.Default.StopCircle else Icons.Default.PlayCircle, null, tint = VaultBrand)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(note.title, fontWeight = FontWeight.SemiBold, color = VaultInk)
                            Text(dateText(note.createdAt), color = VaultMuted, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            if (playingId == note.id) {
                                player?.release()
                                player = null
                                playingId = null
                            }
                            store.deleteVoice(note)
                            notes = store.voiceMemos()
                        }) {
                            Icon(Icons.Default.DeleteOutline, null, tint = VaultMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultPage(title: String, subtitle: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.padding(8.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Column {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VaultInk)
                Text(subtitle, fontSize = 12.sp, color = VaultMuted)
            }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), content = content)
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun VaultEmpty(title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(top = 18.dp).background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(18.dp)).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory2, null, tint = VaultBrand, modifier = Modifier.size(36.dp))
        Text(title, fontWeight = FontWeight.Bold, color = VaultInk, modifier = Modifier.padding(top = 8.dp))
        Text(subtitle, color = VaultMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

private fun iconFor(item: VaultDocument) = when (DocumentUtils.typeLabel(item)) {
    "PDF" -> Icons.Default.PictureAsPdf
    "DOCX", "DOC" -> Icons.Default.Description
    "XLSX", "XLS", "CSV" -> Icons.Default.TableChart
    "IMAGE" -> Icons.Default.Image
    else -> Icons.Default.InsertDriveFile
}

private fun dateText(time: Long): String =
    SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()).format(Date(time))
