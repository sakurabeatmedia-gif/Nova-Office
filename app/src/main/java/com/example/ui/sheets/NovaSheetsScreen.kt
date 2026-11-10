package com.example.ui.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FloatingNaraButton
import com.example.ui.theme.NovaSheetsGreen
import com.example.ui.viewmodel.NovaOfficeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaSheetsScreen(
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

    // Spreadsheet Grid Internal State
    var numCols by remember { mutableStateOf(6) } // A to F
    var numRows by remember { mutableStateOf(20) }
    var selectedRow by remember { mutableStateOf(0) }
    var selectedCol by remember { mutableStateOf(0) }
    var activeTab by remember { mutableStateOf(0) } // Sheet 1, Sheet 2
    var showChartDialog by remember { mutableStateOf(false) }

    // Cell data map: Pair(row, col) to raw string value/formula
    var gridData by remember { mutableStateOf<Map<Pair<Int, Int>, String>>(emptyMap()) }

    // Parse initial CSV content from document
    LaunchedEffect(currentDoc?.content) {
        val content = currentDoc?.content ?: return@LaunchedEffect
        val map = mutableMapOf<Pair<Int, Int>, String>()
        val lines = content.lines()
        var maxCols = 6
        lines.forEachIndexed { r, line ->
            if (line.isNotBlank()) {
                val cells = line.split(",")
                if (cells.size > maxCols) maxCols = cells.size
                cells.forEachIndexed { c, value ->
                    map[Pair(r, c)] = value.trim()
                }
            }
        }
        numCols = maxCols.coerceAtLeast(6)
        numRows = (lines.size + 10).coerceAtLeast(20)
        gridData = map
    }

    // Helper to evaluate formulas
    fun evaluateCell(raw: String): String {
        if (!raw.startsWith("=")) return raw
        val formula = raw.substring(1).trim().uppercase(Locale.US)

        return try {
            if (formula.startsWith("SUM(") && formula.endsWith(")")) {
                val range = formula.substring(4, formula.length - 1)
                val parts = range.split(":")
                if (parts.size == 2) {
                    val startCol = parts[0][0] - 'A'
                    val startRow = parts[0].substring(1).toInt() - 1
                    val endCol = parts[1][0] - 'A'
                    val endRow = parts[1].substring(1).toInt() - 1

                    var sum = 0.0
                    for (r in startRow..endRow) {
                        for (c in startCol..endCol) {
                            val v = gridData[Pair(r, c)] ?: "0"
                            sum += v.toDoubleOrNull() ?: 0.0
                        }
                    }
                    if (sum % 1.0 == 0.0) sum.toLong().toString() else String.format(Locale.US, "%.2f", sum)
                } else "0"
            } else if (formula.startsWith("AVERAGE(") && formula.endsWith(")")) {
                val range = formula.substring(8, formula.length - 1)
                val parts = range.split(":")
                var sum = 0.0
                var count = 0
                if (parts.size == 2) {
                    val startCol = parts[0][0] - 'A'
                    val startRow = parts[0].substring(1).toInt() - 1
                    val endCol = parts[1][0] - 'A'
                    val endRow = parts[1].substring(1).toInt() - 1

                    for (r in startRow..endRow) {
                        for (c in startCol..endCol) {
                            val v = gridData[Pair(r, c)] ?: ""
                            val num = v.toDoubleOrNull()
                            if (num != null) {
                                sum += num
                                count++
                            }
                        }
                    }
                }
                if (count > 0) String.format(Locale.US, "%.1f", sum / count) else "0"
            } else {
                raw
            }
        } catch (e: Exception) {
            "#ERROR!"
        }
    }

    // Save grid data back to doc content CSV
    fun syncGridToDoc() {
        val sb = StringBuilder()
        for (r in 0 until numRows) {
            val rowValues = (0 until numCols).map { c -> gridData[Pair(r, c)] ?: "" }
            if (rowValues.any { it.isNotBlank() }) {
                sb.append(rowValues.joinToString(","))
                sb.append("\n")
            }
        }
        viewModel.updateCurrentDocumentContent(sb.toString().trimEnd())
    }

    val selectedCoord = "${('A' + selectedCol)}${selectedRow + 1}"
    val currentRawValue = gridData[Pair(selectedRow, selectedCol)] ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Sheets Top Header
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
                IconButton(onClick = {
                    syncGridToDoc()
                    viewModel.saveCurrentDocument()
                    onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentDoc?.title ?: "Spreadsheet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Nova Sheets • Autosave aktif",
                        fontSize = 11.sp,
                        color = NovaSheetsGreen
                    )
                }

                IconButton(onClick = { showChartDialog = true }) {
                    Icon(Icons.Default.BarChart, contentDescription = "Bagan", tint = NovaSheetsGreen)
                }

                IconButton(onClick = {
                    syncGridToDoc()
                    viewModel.saveCurrentDocument()
                    viewModel.showFeedback("Spreadsheet disimpan")
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Simpan", tint = NovaSheetsGreen)
                }
            }
        }

        // Live Formula Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coordinate badge
                Box(
                    modifier = Modifier
                        .background(NovaSheetsGreen, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedCoord,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Editable formula input
                BasicTextField(
                    value = currentRawValue,
                    onValueChange = { newValue ->
                        val updated = gridData.toMutableMap()
                        updated[Pair(selectedRow, selectedCol)] = newValue
                        gridData = updated
                        syncGridToDoc()
                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheets_formula_bar"),
                    singleLine = true
                )
            }
        }

        // Quick Function Formula Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("SUM", "AVERAGE", "COUNT", "MIN", "MAX").forEach { func ->
                FilterChip(
                    selected = false,
                    onClick = {
                        val current = gridData[Pair(selectedRow, selectedCol)] ?: ""
                        val newFormula = if (current.isBlank()) "=$func(A1:A5)" else current
                        val updated = gridData.toMutableMap()
                        updated[Pair(selectedRow, selectedCol)] = newFormula
                        gridData = updated
                        syncGridToDoc()
                    },
                    label = { Text("=$func", fontSize = 11.sp) }
                )
            }

            FilterChip(
                selected = false,
                onClick = {
                    numRows += 5
                    viewModel.showFeedback("5 Baris ditambahkan")
                },
                label = { Text("+ Baris", fontSize = 11.sp) }
            )

            FilterChip(
                selected = false,
                onClick = {
                    numCols = (numCols + 1).coerceAtMost(10)
                    viewModel.showFeedback("Kolom ditambahkan")
                },
                label = { Text("+ Kolom", fontSize = 11.sp) }
            )
        }

        HorizontalDivider()

        // Main Spreadsheet Grid
        val horizontalScrollState = rememberScrollState()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState)) {
                // Column Headers (A, B, C, D...)
                Row(modifier = Modifier.background(Color(0xFFE2E8F0))) {
                    // Empty top-left cell
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 30.dp)
                            .border(0.5.dp, Color(0xFFCBD5E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("#", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    for (c in 0 until numCols) {
                        val colLetter = ('A' + c).toString()
                        val isColSelected = selectedCol == c
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 30.dp)
                                .background(if (isColSelected) NovaSheetsGreen.copy(alpha = 0.2f) else Color.Transparent)
                                .border(0.5.dp, Color(0xFFCBD5E1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = colLetter,
                                fontSize = 12.sp,
                                fontWeight = if (isColSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isColSelected) NovaSheetsGreen else Color.DarkGray
                            )
                        }
                    }
                }

                // Grid Rows
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(numRows) { r ->
                        val isRowSelected = selectedRow == r

                        Row {
                            // Row Header (1, 2, 3...)
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .background(if (isRowSelected) NovaSheetsGreen.copy(alpha = 0.2f) else Color(0xFFF1F5F9))
                                    .border(0.5.dp, Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${r + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isRowSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isRowSelected) NovaSheetsGreen else Color.Gray
                                )
                            }

                            // Cells in row
                            for (c in 0 until numCols) {
                                val isCellSelected = selectedRow == r && selectedCol == c
                                val rawVal = gridData[Pair(r, c)] ?: ""
                                val displayVal = evaluateCell(rawVal)

                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 36.dp)
                                        .background(
                                            when {
                                                isCellSelected -> Color(0xFFD1E7DD)
                                                r == 0 -> Color(0xFFF8FAFC)
                                                else -> Color.White
                                            }
                                        )
                                        .border(
                                            if (isCellSelected) 1.5.dp else 0.5.dp,
                                            if (isCellSelected) NovaSheetsGreen else Color(0xFFE2E8F0)
                                        )
                                        .clickable {
                                            selectedRow = r
                                            selectedCol = c
                                        }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = displayVal,
                                        fontSize = 12.sp,
                                        fontWeight = if (r == 0) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        color = if (rawVal.startsWith("=")) NovaSheetsGreen else Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating NARA Assistant for Spreadsheet analysis
            FloatingNaraButton(
                onClick = { viewModel.setNaraSheetOpen(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 48.dp),
                label = "NARA Sheets"
            )
        }

        // Bottom Worksheets Tabs (Sheet 1, Sheet 2)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Lembar 1 (Anggaran)", "Lembar 2 (Proyeksi)").forEachIndexed { index, name ->
                    FilterChip(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        label = { Text(name, fontSize = 11.sp) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.showFeedback("Lembar 3 ditambahkan") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Lembar", modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // Chart Preview Dialog
    if (showChartDialog) {
        AlertDialog(
            onDismissRequest = { showChartDialog = false },
            title = { Text("Visualisasi Data Bagan", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Bagan Batang Proyeksi Anggaran & Target", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Clean Bar Chart visualization
                    val sampleBars = listOf(
                        "Bab 1-5" to 45f,
                        "Sampul" to 60f,
                        "Cetak B1" to 95f,
                        "Promosi" to 70f,
                        "Distribusi" to 80f
                    )

                    sampleBars.forEach { (label, value) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("${value.toInt()}%", fontSize = 11.sp, color = NovaSheetsGreen)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(5.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(value / 100f)
                                        .fillMaxHeight()
                                        .background(NovaSheetsGreen, RoundedCornerShape(5.dp))
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChartDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaSheetsGreen)
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}
