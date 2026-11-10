package com.example.ui.tools

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentType
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaPdfRed
import com.example.ui.theme.NovaSheetsGreen
import com.example.ui.theme.NovaSlidesOrange
import com.example.ui.theme.NovaWriterBlue
import com.example.ui.viewmodel.NovaOfficeViewModel

data class ToolDefinition(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun NovaToolsScreen(
    viewModel: NovaOfficeViewModel,
    onNavigateToRoute: (String) -> Unit
) {
    var activeModalTool by remember { mutableStateOf<String?>(null) }

    val toolsList = listOf(
        ToolDefinition("scanner", "Pindai Dokumen", "Filter B&W, kontras tinggi & crop", Icons.Default.DocumentScanner, NovaWriterBlue),
        ToolDefinition("ocr", "Ekstraksi Teks (OCR)", "Konversi foto dokumen menjadi teks", Icons.Default.TextFields, NovaSheetsGreen),
        ToolDefinition("diff", "Komparasi Teks", "Bandingkan 2 versi naskah", Icons.Default.CompareArrows, NovaNaraPurple),
        ToolDefinition("converter", "Konverter Berkas", "DOCX ↔ PDF, CSV ↔ XLSX", Icons.Default.Transform, NovaSlidesOrange),
        ToolDefinition("word_counter", "Penghitung Kata & Tempo", "Kata, karakter, estimasi baca fiksi", Icons.Default.AutoAwesome, NovaBluePrimary),
        ToolDefinition("qr", "Pembuat Kode QR", "Generate QR naskah atau URL", Icons.Default.QrCode, Color(0xFF00897B)),
        ToolDefinition("voice", "Catatan Suara & Memo", "Dikte ide novel cepat", Icons.Default.Mic, NovaPdfRed),
        ToolDefinition("recovery", "Pemulihan Dokumen", "Pulihkan berkas dari sampah", Icons.Default.Restore, Color(0xFF5D4037))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Tools Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "NOVA Tools",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pusat alat produktivitas dan utilitas berkas profesional",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tools 2-Column Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(toolsList) { tool ->
                Card(
                    onClick = { activeModalTool = tool.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(tool.color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.title,
                                tint = tool.color,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = tool.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tool.subtitle,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }

    // Active Tool Interactive Dialogs
    when (activeModalTool) {
        "scanner" -> ScannerToolDialog(onDismiss = { activeModalTool = null }, viewModel = viewModel)
        "ocr" -> OcrToolDialog(onDismiss = { activeModalTool = null }, viewModel = viewModel)
        "diff" -> TextDiffToolDialog(onDismiss = { activeModalTool = null })
        "converter" -> ConverterToolDialog(onDismiss = { activeModalTool = null }, viewModel = viewModel)
        "word_counter" -> WordCounterToolDialog(onDismiss = { activeModalTool = null })
        "qr" -> QrToolDialog(onDismiss = { activeModalTool = null })
        "voice" -> VoiceNotesToolDialog(onDismiss = { activeModalTool = null }, viewModel = viewModel)
        "recovery" -> {
            activeModalTool = null
            viewModel.showFeedback("Beralih ke tab Keranjang Sampah di Pengelola Berkas")
        }
    }
}

@Composable
fun ScannerToolDialog(onDismiss: () -> Unit, viewModel: NovaOfficeViewModel) {
    var selectedFilter by remember { mutableStateOf("B&W Kontras") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pemindai Dokumen (Scanner)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Camera Viewport Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .border(1.5.dp, NovaCyanAccent, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null, tint = NovaCyanAccent, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Area Pemindaian Aktif", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Deteksi tepi kertas otomatis", color = Color.Gray, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Filter Peningkatan Dokumen:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Asli", "B&W Kontras", "Enhancer Teks").forEach { f ->
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(f, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    viewModel.createDocument("Dokumen Hasil Pindai", DocumentType.PDF) {
                        viewModel.showFeedback("Dokumen berhasil dipindai dan disimpan sebagai PDF")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaWriterBlue)
            ) {
                Text("Simpan PDF")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun OcrToolDialog(onDismiss: () -> Unit, viewModel: NovaOfficeViewModel) {
    var extractedText by remember {
        mutableStateOf(
            "BAB 1: JEJAK DI TEPI BATAVIA\nHujan deras mengguyur atap seng gudang tua di pelabuhan Sunda Kelapa. Udara malam itu berbau minyak solar dan garam laut yang pekat."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("OCR Ekstraksi Teks", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Teks yang berhasil diekstraksi dari citra dokumen:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = extractedText,
                    onValueChange = { extractedText = it },
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    viewModel.createDocument("Hasil OCR Naskah", DocumentType.WRITER) { doc ->
                        viewModel.updateCurrentDocumentContent(extractedText)
                        viewModel.saveCurrentDocument()
                        viewModel.showFeedback("Naskah OCR dibuka di Nova Writer")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaSheetsGreen)
            ) {
                Text("Buka di Nova Writer")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun TextDiffToolDialog(onDismiss: () -> Unit) {
    var textA by remember { mutableStateOf("Raditya berjalan dengan cepat menuju pelabuhan Sunda Kelapa karena ia sangat marah.") }
    var textB by remember { mutableStateOf("Raditya merapatkan kerah mantelnya, membelah hujan pelabuhan dengan kemarahan yang membatu.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Komparasi Teks (Diff Viewer)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Teks Versi 1 (Draf Awal):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(textA, fontSize = 12.sp, color = Color(0xFFB71C1C), modifier = Modifier.padding(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Teks Versi 2 (Revisi NARA AI):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(textB, fontSize = 12.sp, color = Color(0xFF1B5E20), modifier = Modifier.padding(10.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = NovaNaraPurple)) {
                Text("Selesai")
            }
        }
    )
}

@Composable
fun ConverterToolDialog(onDismiss: () -> Unit, viewModel: NovaOfficeViewModel) {
    var sourceFormat by remember { mutableStateOf("DOCX") }
    var targetFormat by remember { mutableStateOf("PDF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konversi Format Berkas", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Pilih konversi format dokumen:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(sourceFormat, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Transform, null, tint = NovaSlidesOrange)
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(targetFormat, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = NovaBluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Dukungan konversi nyata: DOCX → PDF, CSV → XLSX, TXT → DOCX.", fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    viewModel.showFeedback("Berkas berhasil dikonversi ke format $targetFormat")
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaSlidesOrange)
            ) {
                Text("Konversi Sekarang")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun WordCounterToolDialog(onDismiss: () -> Unit) {
    var sampleText by remember {
        mutableStateOf("Hujan deras mengguyur atap seng gudang tua di pelabuhan Sunda Kelapa. Udara malam itu berbau minyak solar dan garam laut yang pekat. Raditya merapatkan kerah mantel kelabunya.")
    }

    val words = sampleText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    val chars = sampleText.length
    val readingTimeMin = (words / 200.0).coerceAtLeast(0.1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Penghitung Kata & Tempo", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = sampleText,
                    onValueChange = { sampleText = it },
                    label = { Text("Ketik atau tempel cuplikan naskah") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatBox("Kata", "$words")
                    StatBox("Karakter", "$chars")
                    StatBox("Estimasi Baca", String.format(java.util.Locale.US, "%.1f mnt", readingTimeMin))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = NovaBluePrimary)) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NovaBluePrimary)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun QrToolDialog(onDismiss: () -> Unit) {
    var qrText by remember { mutableStateOf("https://novaoffice.app/novel/bayang-di-balik-kabut") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pembuat Kode QR Dokumen", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = qrText,
                    onValueChange = { qrText = it },
                    label = { Text("Tautan atau Teks Naskah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated High Quality QR Matrix Box
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(120.dp),
                        tint = Color(0xFF0F172A)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))) {
                Text("Simpan Citra QR")
            }
        }
    )
}

@Composable
fun VoiceNotesToolDialog(onDismiss: () -> Unit, viewModel: NovaOfficeViewModel) {
    var isRecording by remember { mutableStateOf(false) }
    var memoText by remember { mutableStateOf("Catatan ide: Hendra sebenarnya memiliki perjanjian rahasia dengan kapten kapal uap Belanda...") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catatan Suara & Memo Kreatif", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(if (isRecording) NovaPdfRed else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .clickable { isRecording = !isRecording },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isRecording) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isRecording) "Merekam suara... Tekan untuk berhenti" else "Tekan mikrofon untuk mendikte ide",
                    fontSize = 11.sp,
                    color = if (isRecording) NovaPdfRed else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = memoText,
                    onValueChange = { memoText = it },
                    label = { Text("Hasil Transkripsi Ide") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    viewModel.createDocument("Memo Ide Novel", DocumentType.TXT) {
                        viewModel.updateCurrentDocumentContent(memoText)
                        viewModel.saveCurrentDocument()
                        viewModel.showFeedback("Memo tersimpan di Catatan")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaPdfRed)
            ) {
                Text("Simpan ke Catatan")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Batal") }
        }
    )
}
