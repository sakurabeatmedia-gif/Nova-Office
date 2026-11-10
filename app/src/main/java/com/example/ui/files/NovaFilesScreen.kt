package com.example.ui.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.DocumentActionSheet
import com.example.ui.components.RenameDocumentDialog
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaPdfRed
import com.example.ui.theme.NovaSheetsGreen
import com.example.ui.theme.NovaSlidesOrange
import com.example.ui.theme.NovaWriterBlue
import com.example.ui.viewmodel.NovaOfficeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaFilesScreen(
    viewModel: NovaOfficeViewModel,
    onNavigateToDocument: (OfficeDocument) -> Unit
) {
    val context = LocalContext.current
    val activeDocs by viewModel.documentRepository.activeDocuments.collectAsState(initial = emptyList())
    val trashDocs by viewModel.trashDocuments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Semua Berkas, 1: Keranjang Sampah
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var actionDoc by remember { mutableStateOf<OfficeDocument?>(null) }
    var renameDoc by remember { mutableStateOf<OfficeDocument?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var customFolders by remember { mutableStateOf(listOf("Naskah Novel", "Finance", "Presentasi", "Legal", "Catatan")) }
    var sortOrder by remember { mutableStateOf("Terbaru") } // Terbaru, Nama, Ukuran
    var showSortMenu by remember { mutableStateOf(false) }

    // SAF File Picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: "Berkas diimpor"
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Dokumen_Impor.docx"
            viewModel.importDocument(fileName, content) { doc ->
                onNavigateToDocument(doc)
            }
        }
    }

    val displayDocs = if (activeTab == 0) {
        activeDocs.filter { doc ->
            val matchesFolder = selectedFolder == null || doc.folderName == selectedFolder
            val matchesSearch = searchQuery.isBlank() || doc.title.contains(searchQuery, ignoreCase = true)
            matchesFolder && matchesSearch
        }.let { list ->
            when (sortOrder) {
                "Nama" -> list.sortedBy { it.title.lowercase() }
                "Ukuran" -> list.sortedByDescending { it.sizeBytes }
                else -> list.sortedByDescending { it.lastModified }
            }
        }
    } else {
        trashDocs
    }

    val totalBytes = activeDocs.sumOf { it.sizeBytes }
    val totalMb = totalBytes.toDouble() / (1024 * 1024)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Files Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pengelola Berkas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Penyimpanan Internal & Storage Access Framework",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(onClick = { showCreateFolderDialog = true }) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "Folder Baru", tint = NovaBluePrimary)
                        }

                        IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.FileOpen, contentDescription = "Impor Berkas", tint = NovaBluePrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Storage Usage Meter Bar
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, null, modifier = Modifier.size(14.dp), tint = NovaBluePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Penyimpanan Dokumen", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                String.format(java.util.Locale.US, "%.2f MB terpakai", totalMb),
                                fontSize = 11.sp,
                                color = NovaBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (totalMb / 100.0).toFloat().coerceIn(0.05f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = NovaBluePrimary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Berkas Aktif vs Keranjang Sampah
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("Semua Berkas (${activeDocs.size})") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("Keranjang Sampah (${trashDocs.size})") },
                        leadingIcon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
        }

        // Folder & Filter Controls (If on active tab)
        if (activeTab == 0) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFolder == null,
                        onClick = { selectedFolder = null },
                        label = { Text("Semua Folder") }
                    )
                }
                items(customFolders) { folder ->
                    FilterChip(
                        selected = selectedFolder == folder,
                        onClick = { selectedFolder = folder },
                        label = { Text(folder) },
                        leadingIcon = { Icon(Icons.Default.Folder, null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            // Sort & Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedFolder != null) "Folder: $selectedFolder" else "Daftar Berkas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box {
                    Row(
                        modifier = Modifier.clickable { showSortMenu = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sort, null, modifier = Modifier.size(14.dp), tint = NovaBluePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Urut: $sortOrder", fontSize = 11.sp, color = NovaBluePrimary, fontWeight = FontWeight.Medium)
                    }

                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        listOf("Terbaru", "Nama", "Ukuran").forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order) },
                                onClick = {
                                    sortOrder = order
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Trash Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Item yang dihapus akan tersimpan di sini", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (trashDocs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showEmptyTrashConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kosongkan Sampah", fontSize = 11.sp)
                    }
                }
            }
        }

        // Documents List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            if (displayDocs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (activeTab == 0) Icons.Default.Folder else Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (activeTab == 0) "Tidak ada berkas di folder ini" else "Keranjang sampah kosong",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(displayDocs, key = { it.id }) { doc ->
                    Card(
                        onClick = {
                            if (activeTab == 0) {
                                onNavigateToDocument(doc)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(doc.type.color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (doc.type) {
                                        DocumentType.WRITER -> Icons.Default.Description
                                        DocumentType.SHEETS -> Icons.Default.TableChart
                                        DocumentType.SLIDES -> Icons.Default.Slideshow
                                        DocumentType.PDF -> Icons.Default.PictureAsPdf
                                        DocumentType.TXT -> Icons.AutoMirrored.Filled.InsertDriveFile
                                    },
                                    contentDescription = null,
                                    tint = doc.type.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${doc.folderName} • ${doc.formattedSize} • ${doc.formattedDate}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (activeTab == 0) {
                                IconButton(onClick = { actionDoc = doc }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opsi", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                // Restore and Permanent Delete Buttons
                                Row {
                                    IconButton(onClick = { viewModel.restoreFromTrash(doc.id) }) {
                                        Icon(Icons.Default.Restore, contentDescription = "Pulihkan", tint = NovaSheetsGreen)
                                    }
                                    IconButton(onClick = { viewModel.deletePermanently(doc.id) }) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = "Hapus Permanen", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Buat Folder Baru", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Nama Folder") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            customFolders = customFolders + newFolderName.trim()
                            newFolderName = ""
                            showCreateFolderDialog = false
                            viewModel.showFeedback("Folder berhasil dibuat")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaBluePrimary)
                ) {
                    Text("Buat")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Empty Trash Confirmation
    if (showEmptyTrashConfirm) {
        ConfirmDialog(
            title = "Kosongkan Keranjang Sampah?",
            message = "Semua dokumen di keranjang sampah akan dihapus secara permanen dan tidak dapat dipulihkan.",
            confirmText = "Kosongkan",
            isDestructive = true,
            onDismiss = { showEmptyTrashConfirm = false },
            onConfirm = {
                viewModel.emptyTrash()
                showEmptyTrashConfirm = false
            }
        )
    }

    // Document Action Sheet
    actionDoc?.let { doc ->
        DocumentActionSheet(
            document = doc,
            onDismiss = { actionDoc = null },
            onOpen = { onNavigateToDocument(it) },
            onRename = { renameDoc = it },
            onDuplicate = { viewModel.duplicateDocument(it.id) },
            onToggleFavorite = { viewModel.toggleFavorite(it.id) },
            onDelete = { viewModel.moveToTrash(it.id) }
        )
    }

    // Rename Dialog
    renameDoc?.let { doc ->
        RenameDocumentDialog(
            currentTitle = doc.title,
            onDismiss = { renameDoc = null },
            onConfirm = { newTitle ->
                renameDoc = null
                viewModel.renameDocument(doc.id, newTitle)
            }
        )
    }
}
