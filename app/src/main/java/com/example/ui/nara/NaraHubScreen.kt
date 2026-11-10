package com.example.ui.nara

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterProfile
import com.example.data.model.EditorialMode
import com.example.data.model.TimelineEvent
import com.example.data.model.WorldItem
import com.example.ui.components.ChatBubbleItem
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaNavyDark
import com.example.ui.viewmodel.NovaOfficeViewModel

@Composable
fun NaraHubScreen(
    viewModel: NovaOfficeViewModel
) {
    val activeProject by viewModel.documentRepository.currentActiveProject.collectAsState()
    val chatMessages by viewModel.naraChatMessages.collectAsState()
    val isGenerating by viewModel.isNaraGenerating.collectAsState()
    val selectedMode by viewModel.selectedEditorialMode.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Editor Studio (Chat), 1: Basis Data Tokoh, 2: Worldbuilding, 3: Linimasa
    var inputPrompt by remember { mutableStateOf("") }
    var showAddCharacterDialog by remember { mutableStateOf(false) }
    var showAddWorldDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Executive NARA Banner
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.linearGradient(listOf(NovaNaraPurple, Color(0xFF4834DF))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("NARA AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(NovaNaraPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Editor Novel Profesional", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NovaNaraPurple)
                                }
                            }
                            Text(
                                text = "Proyek Aktif: ${activeProject?.title ?: "Memuat Proyek..."} (${activeProject?.genre})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Bersihkan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs within NARA Hub
                val tabs = listOf("Studio Diskusi", "Database Tokoh", "Worldbuilding", "Linimasa Plot")
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTab == index) NovaNaraPurple else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        when (activeTab) {
            0 -> {
                // Studio Chat View
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Editorial Mode Bar
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(EditorialMode.values()) { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = { viewModel.setEditorialMode(mode) },
                                label = { Text(mode.title, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Messages List
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chatMessages) { msg ->
                            ChatBubbleItem(message = msg, onCopy = { viewModel.showFeedback("Disalin") })
                        }

                        if (isGenerating) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NovaNaraPurple)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("NARA sedang menyusun tinjauan editorial...", fontSize = 12.sp, color = NovaNaraPurple)
                                }
                            }
                        }
                    }

                    // Input Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputPrompt,
                                onValueChange = { inputPrompt = it },
                                placeholder = { Text("Diskusikan alur, adegan, atau karakter...", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NovaNaraPurple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (inputPrompt.isNotBlank() && !isGenerating) {
                                        viewModel.sendNaraMessage(inputPrompt.trim())
                                        inputPrompt = ""
                                    }
                                },
                                enabled = inputPrompt.isNotBlank() && !isGenerating,
                                modifier = Modifier.background(
                                    if (inputPrompt.isNotBlank()) NovaNaraPurple else Color.Gray.copy(alpha = 0.3f),
                                    CircleShape
                                )
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            1 -> {
                // Character Bible View
                Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daftar Tokoh Proyek", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { showAddCharacterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaNaraPurple),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Tokoh", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(activeProject?.characters ?: emptyList()) { char ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(char.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(char.role, fontSize = 11.sp, color = NovaNaraPurple, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Kepribadian: ${char.personality}", fontSize = 12.sp)
                                    Text("Konflik Batin: ${char.internalConflict}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Catatan Suara (Voice): ${char.voiceNotes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Hubungan: ${char.relationships}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Worldbuilding View
                Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Basis Data Dunia & Lore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { showAddWorldDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaNaraPurple),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Item", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(activeProject?.worldbuilding ?: emptyList()) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(item.category, fontSize = 11.sp, color = NovaCyanAccent, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(item.description, fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Timeline Events View
                Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    Text("Linimasa & Struktur Bab Plot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(activeProject?.timeline ?: emptyList()) { event ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(NovaNaraPurple.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${event.orderIndex}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NovaNaraPurple)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${event.phase}: ${event.title}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(event.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Character Dialog
    if (showAddCharacterDialog) {
        var charName by remember { mutableStateOf("") }
        var charRole by remember { mutableStateOf("Protagonis / Pendukung") }
        var charPersonality by remember { mutableStateOf("") }
        var charConflict by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCharacterDialog = false },
            title = { Text("Tambah Tokoh Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = charName, onValueChange = { charName = it }, label = { Text("Nama Tokoh") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = charRole, onValueChange = { charRole = it }, label = { Text("Peran (Protagonis/Antagonis)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = charPersonality, onValueChange = { charPersonality = it }, label = { Text("Sifat & Kepribadian") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = charConflict, onValueChange = { charConflict = it }, label = { Text("Konflik Batin") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (charName.isNotBlank()) {
                            viewModel.addCharacterToActiveProject(
                                CharacterProfile(
                                    id = java.util.UUID.randomUUID().toString(),
                                    name = charName,
                                    role = charRole,
                                    personality = charPersonality,
                                    internalConflict = charConflict,
                                    voiceNotes = "Organik dan bernas",
                                    relationships = "Dinamis terhadap protagonis"
                                )
                            )
                            showAddCharacterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaNaraPurple)
                ) {
                    Text("Simpan Tokoh")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCharacterDialog = false }) { Text("Batal") }
            }
        )
    }

    // Add World Item Dialog
    if (showAddWorldDialog) {
        var worldName by remember { mutableStateOf("") }
        var worldCategory by remember { mutableStateOf("Lokasi") }
        var worldDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddWorldDialog = false },
            title = { Text("Tambah Item Worldbuilding", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = worldName, onValueChange = { worldName = it }, label = { Text("Nama Lokasi / Konsep") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = worldCategory, onValueChange = { worldCategory = it }, label = { Text("Kategori (Lokasi/Faksi/Artefak)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = worldDesc, onValueChange = { worldDesc = it }, label = { Text("Deskripsi Sensorik / Sejarah") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (worldName.isNotBlank()) {
                            viewModel.addWorldItemToActiveProject(
                                WorldItem(
                                    id = java.util.UUID.randomUUID().toString(),
                                    category = worldCategory,
                                    name = worldName,
                                    description = worldDesc
                                )
                            )
                            showAddWorldDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaNaraPurple)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddWorldDialog = false }) { Text("Batal") }
            }
        )
    }
}
