package com.example.ui.writer

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditorialMode
import com.example.ui.components.DiffReviewDialog
import com.example.ui.components.FloatingNaraButton
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaNavyDark
import com.example.ui.theme.NovaWriterBlue
import com.example.ui.viewmodel.NovaOfficeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaWriterScreen(
    docId: String,
    viewModel: NovaOfficeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentDoc by viewModel.currentDocument.collectAsState()
    val isAutosaving by viewModel.isAutosaving.collectAsState()
    val lastSaveTime by viewModel.lastSaveTime.collectAsState()
    val pendingDiff by viewModel.pendingDiffProposal.collectAsState()
    val isNaraGenerating by viewModel.isNaraGenerating.collectAsState()

    LaunchedEffect(docId) {
        viewModel.loadDocument(docId)
    }

    BackHandler {
        viewModel.saveCurrentDocument()
        onBack()
    }

    var selectedToolbarTab by remember { mutableStateOf(0) } // 0: Format, 1: Layout, 2: Insert, 3: NARA AI
    var showFindReplace by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var showOutlineDrawer by remember { mutableStateOf(false) }
    var showAiInstructionModal by remember { mutableStateOf(false) }
    var aiInstruction by remember { mutableStateOf("") }

    // Typography & Formatting states
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isUnderline by remember { mutableStateOf(false) }
    var textAlign by remember { mutableStateOf(TextAlign.Start) }
    var currentFontFamily by remember { mutableStateOf(FontFamily.Default) }
    var currentFontSize by remember { mutableStateOf(16.sp) }
    var fontColor by remember { mutableStateOf(Color.Unspecified) }
    var pageMarginDp by remember { mutableStateOf(20.dp) }
    var pageSizeLabel by remember { mutableStateOf("A4 (210 x 297 mm)") }
    var isLandscape by remember { mutableStateOf(false) }

    val doc = currentDoc

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9EEF4)) // Classic subtle office desk gray
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Document Header Bar
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
                IconButton(
                    onClick = {
                        viewModel.saveCurrentDocument()
                        onBack()
                    },
                    modifier = Modifier.testTag("writer_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }

                // Document Title Input
                Column(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = doc?.title ?: "Memuat dokumen...",
                        onValueChange = { viewModel.updateCurrentDocumentTitle(it) },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("writer_title_input")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAutosaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = NovaWriterBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Menyimpan...", fontSize = 10.sp, color = NovaWriterBlue)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            Text(
                                text = "Tersimpan otomatis • ${sdf.format(Date(lastSaveTime))}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = " • ${doc?.wordCount ?: 0} kata",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Header Actions: Outline, Find, Save, Share
                IconButton(
                    onClick = { showOutlineDrawer = true },
                    modifier = Modifier.testTag("writer_outline_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Struktur & Outline",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showFindReplace = !showFindReplace }) {
                    Icon(Icons.Default.Search, contentDescription = "Cari & Ganti", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(
                    onClick = { viewModel.saveCurrentDocument() },
                    modifier = Modifier.testTag("writer_save_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Simpan", tint = NovaWriterBlue)
                }

                IconButton(onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TITLE, doc?.title ?: "Dokumen")
                        putExtra(Intent.EXTRA_TEXT, doc?.content ?: "")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Ekspor naskah via"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Optional Find and Replace Bar
        if (showFindReplace) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = findQuery,
                            onValueChange = { findQuery = it },
                            placeholder = { Text("Cari kata...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Ganti dengan...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = {
                            if (findQuery.isNotBlank() && doc != null) {
                                val replaced = doc.content.replace(findQuery, replaceQuery)
                                viewModel.updateCurrentDocumentContent(replaced)
                                viewModel.showFeedback("Berhasil mengganti kata dalam naskah")
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Terapkan", tint = NovaBluePrimary)
                        }
                        IconButton(onClick = { showFindReplace = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }
            }
        }

        // Center: Word Processing Virtual Paper View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = if (isLandscape) 32.dp else 12.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pageMarginDp)
                ) {
                    // Editable Document Content Area
                    BasicTextField(
                        value = doc?.content ?: "",
                        onValueChange = { viewModel.updateCurrentDocumentContent(it) },
                        textStyle = TextStyle(
                            fontFamily = currentFontFamily,
                            fontSize = currentFontSize,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None,
                            textAlign = textAlign,
                            color = if (fontColor != Color.Unspecified) fontColor else Color(0xFF1E293B),
                            lineHeight = 24.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("writer_text_editor")
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // Floating NARA AI Button right inside editor
            FloatingNaraButton(
                onClick = { viewModel.setNaraSheetOpen(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 12.dp),
                label = "Tanya NARA"
            )
        }

        // Bottom Formatting & Tools Deck
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Toolbar Tabs (Format, Layout, Insert, NARA AI)
                TabRow(
                    selectedTabIndex = selectedToolbarTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = NovaWriterBlue
                ) {
                    Tab(
                        selected = selectedToolbarTab == 0,
                        onClick = { selectedToolbarTab = 0 },
                        text = { Text("Format", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedToolbarTab == 1,
                        onClick = { selectedToolbarTab = 1 },
                        text = { Text("Tata Letak", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedToolbarTab == 2,
                        onClick = { selectedToolbarTab = 2 },
                        text = { Text("Sisipkan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedToolbarTab == 3,
                        onClick = { selectedToolbarTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = NovaNaraPurple
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "NARA AI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NovaNaraPurple
                                )
                            }
                        }
                    )
                }

                // Toolbar Action Buttons Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (selectedToolbarTab) {
                        0 -> { // Format
                            IconButton(onClick = { isBold = !isBold }) {
                                Icon(Icons.Default.FormatBold, contentDescription = "Tebal", tint = if (isBold) NovaWriterBlue else MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { isItalic = !isItalic }) {
                                Icon(Icons.Default.FormatItalic, contentDescription = "Miring", tint = if (isItalic) NovaWriterBlue else MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { isUnderline = !isUnderline }) {
                                Icon(Icons.Default.FormatUnderlined, contentDescription = "Garis Bawah", tint = if (isUnderline) NovaWriterBlue else MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { textAlign = TextAlign.Start }) {
                                Icon(Icons.Default.FormatAlignLeft, contentDescription = "Rata Kiri", tint = if (textAlign == TextAlign.Start) NovaWriterBlue else MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { textAlign = TextAlign.Center }) {
                                Icon(Icons.Default.FormatAlignCenter, contentDescription = "Rata Tengah", tint = if (textAlign == TextAlign.Center) NovaWriterBlue else MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { textAlign = TextAlign.Justify }) {
                                Icon(Icons.Default.FormatAlignJustify, contentDescription = "Rata Kanan Kiri", tint = if (textAlign == TextAlign.Justify) NovaWriterBlue else MaterialTheme.colorScheme.onSurface)
                            }
                            // Font Size Adjuster
                            IconButton(onClick = {
                                val current = currentFontSize.value
                                if (current > 12) currentFontSize = (current - 2).sp
                            }) {
                                Text("A-", fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = {
                                val current = currentFontSize.value
                                if (current < 28) currentFontSize = (current + 2).sp
                            }) {
                                Text("A+", fontWeight = FontWeight.Bold)
                            }
                        }
                        1 -> { // Layout
                            FilterChip(
                                selected = pageSizeLabel.startsWith("A4"),
                                onClick = { pageSizeLabel = "A4 (210 x 297 mm)" },
                                label = { Text("A4") }
                            )
                            FilterChip(
                                selected = pageSizeLabel.startsWith("Letter"),
                                onClick = { pageSizeLabel = "Letter (216 x 279 mm)" },
                                label = { Text("Letter") }
                            )
                            FilterChip(
                                selected = isLandscape,
                                onClick = { isLandscape = !isLandscape },
                                label = { Text(if (isLandscape) "Lansekap" else "Potret") }
                            )
                            FilterChip(
                                selected = pageMarginDp == 12.dp,
                                onClick = { pageMarginDp = if (pageMarginDp == 12.dp) 24.dp else 12.dp },
                                label = { Text("Margin Sempit/Lebar") }
                            )
                        }
                        2 -> { // Insert
                            IconButton(onClick = {
                                val tableTemplate = "\n| Kolom 1 | Kolom 2 | Kolom 3 |\n|---|---|---|\n| Data A | Data B | Data C |\n"
                                doc?.let { viewModel.updateCurrentDocumentContent(it.content + tableTemplate) }
                                viewModel.showFeedback("Tabel disisipkan ke naskah")
                            }) {
                                Icon(Icons.Default.TableChart, contentDescription = "Tabel", tint = NovaWriterBlue)
                            }
                            IconButton(onClick = {
                                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                val dateStr = "\n[Tanggal: ${sdf.format(Date())}]\n"
                                doc?.let { viewModel.updateCurrentDocumentContent(it.content + dateStr) }
                            }) {
                                Text("📅", fontSize = 16.sp)
                            }
                            IconButton(onClick = {
                                val divider = "\n\n***\n\n"
                                doc?.let { viewModel.updateCurrentDocumentContent(it.content + divider) }
                            }) {
                                Text("——", fontWeight = FontWeight.Bold)
                            }
                        }
                        3 -> { // NARA AI Editorial Novel Actions
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val selection = doc?.content?.takeLast(500) ?: ""
                                    viewModel.requestEditSelection(
                                        selectedSnippet = selection,
                                        instruction = "Poles diksi, kedalaman sensorik, dan Show Don't Tell"
                                    )
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(12.dp), tint = NovaNaraPurple)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Poles Prosa")
                                    }
                                }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val selection = doc?.content?.takeLast(500) ?: ""
                                    viewModel.requestEditSelection(
                                        selectedSnippet = selection,
                                        instruction = "Tajamkan dialog karakter, kurangi tag berlebihan, dan perkuat subteks"
                                    )
                                },
                                label = { Text("Tajamkan Dialog") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val selection = doc?.content?.takeLast(500) ?: ""
                                    viewModel.requestEditSelection(
                                        selectedSnippet = selection,
                                        instruction = "Periksa tempo dan ketegangan adegan (pacing analysis)"
                                    )
                                },
                                label = { Text("Periksa Pacing") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val selection = doc?.content?.takeLast(500) ?: ""
                                    viewModel.requestEditSelection(
                                        selectedSnippet = selection,
                                        instruction = "Koreksi ejaan, tanda baca dialog, dan tipo sesuai kaidah sastra"
                                    )
                                },
                                label = { Text("Proofreading") }
                            )
                        }
                    }
                }
            }
        }
    }

    // Loading dialog during AI generation
    if (isNaraGenerating) {
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NovaNaraPurple
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("NARA AI Menganalisis Naskah...", fontWeight = FontWeight.Bold)
                            Text("Menerapkan standar editorial novel profesional", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Diff Review Dialog
    pendingDiff?.let { diff ->
        DiffReviewDialog(
            diffResult = diff,
            onApply = { viewModel.applyPendingDiff() },
            onReject = { viewModel.dismissPendingDiff() }
        )
    }

    // Outline & Manuscript Stats Drawer
    if (showOutlineDrawer) {
        ModalBottomSheet(
            onDismissRequest = { showOutlineDrawer = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Struktur & Statistik Naskah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = doc?.title ?: "Dokumen",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showOutlineDrawer = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                val words = doc?.wordCount ?: 0
                val chars = doc?.content?.length ?: 0
                val readingMinutes = (words / 200).coerceAtLeast(1)
                val estimatedPages = (words / 300).coerceAtLeast(1)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Kata", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$words", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NovaWriterBlue)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Halaman", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("~$estimatedPages", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Waktu Baca", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("~$readingMinutes mnt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Karakter", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$chars", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Daftar Bab & Struktur Bagian", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val detectedHeadings = remember(doc?.content) {
                    val lines = doc?.content?.lines().orEmpty()
                    lines.map { it.trim() }.filter { trimmed ->
                        trimmed.startsWith("BAB", ignoreCase = true) ||
                        trimmed.startsWith("Chapter", ignoreCase = true) ||
                        trimmed.startsWith("Bagian", ignoreCase = true) ||
                        trimmed.startsWith("Prolog", ignoreCase = true) ||
                        trimmed.startsWith("Epilog", ignoreCase = true) ||
                        (trimmed.length in 4..35 && trimmed.all { it.isUpperCase() || it.isWhitespace() || it.isDigit() })
                    }
                }

                if (detectedHeadings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Naskah mengalir berkesinambungan. Tambahkan penanda seperti \"BAB 1\" atau \"BAB 2\" agar terpetakan otomatis.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        detectedHeadings.forEachIndexed { idx, heading ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showOutlineDrawer = false
                                        viewModel.showFeedback("Mengarahkan fokus ke $heading")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = NovaWriterBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = heading,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text("Opsi Ekspor & Berbagi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            showOutlineDrawer = false
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, "${doc?.title ?: "Dokumen"}.docx")
                                putExtra(Intent.EXTRA_TEXT, doc?.content ?: "")
                                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            }
                            context.startActivity(Intent.createChooser(intent, "Ekspor sebagai .docx via"))
                            viewModel.showFeedback("Naskah siap diekspor dalam format DOCX")
                        },
                        label = { Text("Ekspor Word (.docx)") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            showOutlineDrawer = false
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, "${doc?.title ?: "Dokumen"}.pdf")
                                putExtra(Intent.EXTRA_TEXT, doc?.content ?: "")
                                type = "application/pdf"
                            }
                            context.startActivity(Intent.createChooser(intent, "Ekspor sebagai .pdf via"))
                            viewModel.showFeedback("Naskah siap diekspor dalam format PDF")
                        },
                        label = { Text("Ekspor PDF (.pdf)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
