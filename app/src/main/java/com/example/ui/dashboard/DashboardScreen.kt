package com.example.ui.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument
import com.example.ui.components.CreateDocumentDialog
import com.example.ui.components.DocumentActionSheet
import com.example.ui.components.RenameDocumentDialog
import com.example.ui.navigation.Screen
import com.example.ui.theme.NovaBlueDeep
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaNavyDark
import com.example.ui.theme.NovaPdfRed
import com.example.ui.theme.NovaSheetsGreen
import com.example.ui.theme.NovaSlidesOrange
import com.example.ui.theme.NovaWriterBlue
import com.example.ui.viewmodel.NovaOfficeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NovaOfficeViewModel,
    onNavigateToDocument: (OfficeDocument) -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredDocs by viewModel.filteredDocuments.collectAsState()
    val feedbackMsg by viewModel.userFeedbackMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var createType by remember { mutableStateOf(DocumentType.WRITER) }
    var actionDoc by remember { mutableStateOf<OfficeDocument?>(null) }
    var renameDoc by remember { mutableStateOf<OfficeDocument?>(null) }
    var showNotificationSheet by remember { mutableStateOf(false) }

    // SAF File Picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val content = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: "Berkas berhasil diimpor"
            } catch (e: Exception) {
                "Gagal membaca berkas: ${e.message}"
            }
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Dokumen_Impor.docx"
            viewModel.importDocument(fileName, content) { doc ->
                onNavigateToDocument(doc)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Top Executive Header
            item {
                DashboardHeader(
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onNotificationsClick = { showNotificationSheet = true },
                    onSettingsClick = { onNavigateToRoute(Screen.Settings.route) }
                )
            }

            // Quick Access Actions Grid
            item {
                Text(
                    text = "Akses Cepat",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickAccessCard(
                            title = "New Document",
                            subtitle = "Nova Writer",
                            icon = Icons.Default.Description,
                            color = NovaWriterBlue,
                            onClick = {
                                createType = DocumentType.WRITER
                                showCreateDialog = true
                            },
                            testTag = "qa_new_document"
                        )
                    }
                    item {
                        QuickAccessCard(
                            title = "New Sheet",
                            subtitle = "Nova Sheets",
                            icon = Icons.Default.TableChart,
                            color = NovaSheetsGreen,
                            onClick = {
                                createType = DocumentType.SHEETS
                                showCreateDialog = true
                            },
                            testTag = "qa_new_sheet"
                        )
                    }
                    item {
                        QuickAccessCard(
                            title = "New Slides",
                            subtitle = "Nova Slides",
                            icon = Icons.Default.Slideshow,
                            color = NovaSlidesOrange,
                            onClick = {
                                createType = DocumentType.SLIDES
                                showCreateDialog = true
                            },
                            testTag = "qa_new_slides"
                        )
                    }
                    item {
                        QuickAccessCard(
                            title = "Open PDF",
                            subtitle = "Nova PDF",
                            icon = Icons.Default.PictureAsPdf,
                            color = NovaPdfRed,
                            onClick = {
                                importLauncher.launch(arrayOf("application/pdf", "*/*"))
                            },
                            testTag = "qa_open_pdf"
                        )
                    }
                    item {
                        QuickAccessCard(
                            title = "Scan Doc",
                            subtitle = "OCR & Filter",
                            icon = Icons.Default.QrCodeScanner,
                            color = NovaNaraPurple,
                            onClick = {
                                onNavigateToRoute(Screen.ScannerTool.route)
                            },
                            testTag = "qa_scan_doc"
                        )
                    }
                    item {
                        QuickAccessCard(
                            title = "Import File",
                            subtitle = "SAF Storage",
                            icon = Icons.Default.FileOpen,
                            color = NovaBluePrimary,
                            onClick = {
                                importLauncher.launch(arrayOf("*/*"))
                            },
                            testTag = "qa_import_file"
                        )
                    }
                }
            }

            // Category Filter Tabs
            item {
                Spacer(modifier = Modifier.height(16.dp))

                val categories = listOf("All", "Documents", "Spreadsheets", "Presentations", "PDFs", "Text Files", "Favorites")
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedCategory(category) },
                            text = {
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NovaBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // Recent Documents Section Header
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dokumen Terkini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredDocs.size} berkas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Documents List
            if (filteredDocs.isEmpty()) {
                item {
                    EmptyDocumentsPlaceholder(searchQuery = searchQuery)
                }
            } else {
                items(filteredDocs, key = { it.id }) { doc ->
                    DocumentListItem(
                        document = doc,
                        onClick = { onNavigateToDocument(doc) },
                        onMoreClick = { actionDoc = doc },
                        onFavoriteClick = { viewModel.toggleFavorite(doc.id) }
                    )
                }
            }
        }

        // Floating Action Button for Quick Creation
        FloatingActionButton(
            onClick = {
                createType = DocumentType.WRITER
                showCreateDialog = true
            },
            containerColor = NovaBluePrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 80.dp)
                .testTag("fab_create_document")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Buat Dokumen")
        }
    }

    // Create Document Dialog
    if (showCreateDialog) {
        CreateDocumentDialog(
            initialType = createType,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, type ->
                showCreateDialog = false
                viewModel.createDocument(title, type) { newDoc ->
                    onNavigateToDocument(newDoc)
                }
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

    // Notification Sheet
    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Pusat Notifikasi & Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                NotificationItem(
                    title = "NARA AI Siap Digunakan",
                    detail = "Model editorial fiksi NARA aktif mendampingi naskah novel Anda.",
                    time = "Baru saja",
                    color = NovaNaraPurple
                )
                NotificationItem(
                    title = "Penyimpanan Lokal Tersinkron",
                    detail = "Semua dokumen tersimpan otomatis dengan Room Database.",
                    time = "10 mnt lalu",
                    color = NovaSheetsGreen
                )
                NotificationItem(
                    title = "Draf Novel Terkini",
                    detail = "Bab 1: Jejak di Tepi Batavia siap dilanjutkan.",
                    time = "12 mnt lalu",
                    color = NovaWriterBlue
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DashboardHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Top Row: Brand Logo, Notifications, Settings, User Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(listOf(NovaNavyDark, NovaBlueDeep)),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            color = NovaCyanAccent,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NOVA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OFFICE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Light,
                                color = NovaBluePrimary,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Your Complete Office & Creative Workspace",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NovaBlueDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SB",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari dokumen, naskah novel, spreadsheet...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_bar"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaBluePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(130.dp)
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DocumentListItem(
    document: OfficeDocument,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("doc_item_${document.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Type Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(document.type.color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (document.type) {
                        DocumentType.WRITER -> Icons.Default.Description
                        DocumentType.SHEETS -> Icons.Default.TableChart
                        DocumentType.SLIDES -> Icons.Default.Slideshow
                        DocumentType.PDF -> Icons.Default.PictureAsPdf
                        DocumentType.TXT -> Icons.AutoMirrored.Filled.InsertDriveFile
                    },
                    contentDescription = document.type.displayName,
                    tint = document.type.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (document.isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorit",
                            tint = Color(0xFFF5A623),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.type.extension.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = document.type.color
                    )
                    Text(
                        text = " • ${document.formattedSize} • ${document.formattedDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // More Options
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opsi Lain",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyDocumentsPlaceholder(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (searchQuery.isNotBlank()) "Tidak ditemukan berkas untuk '$searchQuery'" else "Belum ada dokumen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tekan tombol + di bawah atau pilih Akses Cepat untuk membuat naskah, spreadsheet, atau presentasi baru.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun NotificationItem(
    title: String,
    detail: String,
    time: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(top = 4.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}
