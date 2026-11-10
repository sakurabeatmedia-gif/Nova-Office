package com.example.ui.pdf

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FloatingNaraButton
import com.example.ui.theme.NovaPdfRed
import com.example.ui.viewmodel.NovaOfficeViewModel

enum class PdfAnnotateMode {
    NONE, HIGHLIGHT, UNDERLINE, DRAW_SIGNATURE, STICKY_NOTE
}

data class DrawPoint(val offset: Offset, val color: Color, val strokeWidth: Float)

@Composable
fun NovaPdfScreen(
    docId: String,
    viewModel: NovaOfficeViewModel,
    onBack: () -> Unit
) {
    val currentDoc by viewModel.currentDocument.collectAsState()

    LaunchedEffect(docId) {
        viewModel.loadDocument(docId)
    }

    BackHandler {
        viewModel.saveCurrentDocument()
        onBack()
    }

    var annotateMode by remember { mutableStateOf(PdfAnnotateMode.NONE) }
    var activePage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(3) }
    var pageRotation by remember { mutableStateOf(0f) }
    var isBookmarked by remember { mutableStateOf(false) }
    var stickyNoteText by remember { mutableStateOf("") }
    var showStickyNoteDialog by remember { mutableStateOf(false) }
    var stickyNotesList by remember { mutableStateOf(listOf("Penting: Verifikasi royalti pasal 3")) }

    // Freehand drawing paths for signature and annotations
    val drawnLines = remember { mutableStateListOf<List<Offset>>() }
    var currentLine by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var brushColor by remember { mutableStateOf(Color(0xFF0056B3)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3E50)) // Deep document viewer backdrop
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // PDF Top Navigation
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentDoc?.title ?: "Dokumen PDF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Halaman $activePage dari $totalPages • Nova PDF",
                        fontSize = 11.sp,
                        color = NovaPdfRed
                    )
                }

                IconButton(onClick = { isBookmarked = !isBookmarked }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Tandai",
                        tint = if (isBookmarked) NovaPdfRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = {
                    pageRotation = (pageRotation + 90f) % 360f
                    viewModel.showFeedback("Halaman diputar ${pageRotation.toInt()}°")
                }) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Putar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = {
                    viewModel.saveCurrentDocument()
                    viewModel.showFeedback("Anotasi PDF disimpan")
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Simpan", tint = NovaPdfRed)
                }
            }
        }

        // Annotation Mode Selector Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = annotateMode == PdfAnnotateMode.HIGHLIGHT,
                    onClick = {
                        annotateMode = if (annotateMode == PdfAnnotateMode.HIGHLIGHT) PdfAnnotateMode.NONE else PdfAnnotateMode.HIGHLIGHT
                    },
                    label = { Text("Stabilo", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Highlight, null, modifier = Modifier.size(14.dp)) }
                )

                FilterChip(
                    selected = annotateMode == PdfAnnotateMode.UNDERLINE,
                    onClick = {
                        annotateMode = if (annotateMode == PdfAnnotateMode.UNDERLINE) PdfAnnotateMode.NONE else PdfAnnotateMode.UNDERLINE
                    },
                    label = { Text("Garis Bawah", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.FormatUnderlined, null, modifier = Modifier.size(14.dp)) }
                )

                FilterChip(
                    selected = annotateMode == PdfAnnotateMode.DRAW_SIGNATURE,
                    onClick = {
                        annotateMode = if (annotateMode == PdfAnnotateMode.DRAW_SIGNATURE) PdfAnnotateMode.NONE else PdfAnnotateMode.DRAW_SIGNATURE
                    },
                    label = { Text("Tanda Tangan", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Draw, null, modifier = Modifier.size(14.dp)) }
                )

                FilterChip(
                    selected = false,
                    onClick = { showStickyNoteDialog = true },
                    label = { Text("+ Catatan", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Notes, null, modifier = Modifier.size(14.dp)) }
                )

                if (drawnLines.isNotEmpty()) {
                    IconButton(onClick = { drawnLines.clear() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus Coretan", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // PDF Main Canvas (A4 Simulated Page)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(pageRotation)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    // Render PDF Text Content
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = currentDoc?.content ?: "Memuat dokumen PDF...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1E293B),
                            lineHeight = 22.sp,
                            modifier = Modifier.background(
                                if (annotateMode == PdfAnnotateMode.HIGHLIGHT) Color(0x33FFEB3B) else Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Render Attached Sticky Notes
                        stickyNotesList.forEach { note ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Notes, null, tint = Color(0xFFF57F17), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(note, fontSize = 11.sp, color = Color(0xFF5D4037), fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    // Freehand Signature / Drawing Layer on top of PDF Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(annotateMode) {
                                if (annotateMode == PdfAnnotateMode.DRAW_SIGNATURE) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentLine = listOf(offset)
                                        },
                                        onDrag = { change, _ ->
                                            currentLine = currentLine + change.position
                                        },
                                        onDragEnd = {
                                            if (currentLine.isNotEmpty()) {
                                                drawnLines.add(currentLine)
                                                currentLine = emptyList()
                                            }
                                        }
                                    )
                                }
                            }
                    ) {
                        // Draw previous finished strokes
                        drawnLines.forEach { line ->
                            if (line.size > 1) {
                                val path = Path().apply {
                                    moveTo(line.first().x, line.first().y)
                                    for (i in 1 until line.size) {
                                        lineTo(line[i].x, line[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = brushColor,
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        // Draw currently dragging stroke
                        if (currentLine.size > 1) {
                            val path = Path().apply {
                                moveTo(currentLine.first().x, currentLine.first().y)
                                for (i in 1 until currentLine.size) {
                                    lineTo(currentLine[i].x, currentLine[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = brushColor,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
            }

            // Floating NARA Assistant for PDF Analysis
            FloatingNaraButton(
                onClick = { viewModel.setNaraSheetOpen(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 48.dp),
                label = "NARA PDF"
            )
        }

        // Bottom Page Navigator Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (activePage > 1) activePage-- },
                    enabled = activePage > 1
                ) {
                    Text("◀", fontSize = 14.sp)
                }

                Text(
                    text = "Halaman $activePage / $totalPages",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                IconButton(
                    onClick = { if (activePage < totalPages) activePage++ },
                    enabled = activePage < totalPages
                ) {
                    Text("▶", fontSize = 14.sp)
                }
            }
        }
    }

    // Sticky Note Add Dialog
    if (showStickyNoteDialog) {
        AlertDialog(
            onDismissRequest = { showStickyNoteDialog = false },
            title = { Text("Tambah Catatan Tempel", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = stickyNoteText,
                    onValueChange = { stickyNoteText = it },
                    placeholder = { Text("Tulis catatan atau memo...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (stickyNoteText.isNotBlank()) {
                            stickyNotesList = stickyNotesList + stickyNoteText
                            stickyNoteText = ""
                            showStickyNoteDialog = false
                            viewModel.showFeedback("Catatan tempel disematkan pada PDF")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaPdfRed)
                ) {
                    Text("Sematkan")
                }
            },
            dismissButton = {
                Button(onClick = { showStickyNoteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
