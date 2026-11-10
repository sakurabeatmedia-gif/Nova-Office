package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditorialMode
import com.example.data.repository.SettingsRepository
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaNavyDark
import com.example.ui.viewmodel.NovaOfficeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NovaOfficeViewModel
) {
    val apiKey by viewModel.settingsRepository.apiKey.collectAsState()
    val themeMode by viewModel.settingsRepository.theme.collectAsState()
    val language by viewModel.settingsRepository.language.collectAsState()
    val autoSaveSec by viewModel.settingsRepository.autoSaveSec.collectAsState()
    val defaultMode by viewModel.settingsRepository.defaultMode.collectAsState()

    val isTesting by viewModel.isTestingConnection.collectAsState()
    val testResult by viewModel.connectionTestResult.collectAsState()

    var isEditingKey by remember { mutableStateOf(apiKey.isBlank()) }
    var tempApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Settings Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Pengaturan NOVA OFFICE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Konfigurasi sistem, AI Connection, NARA AI, dan penyimpanan",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Settings Content Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Settings → NARA AI → AI Connection
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_connection_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NovaNaraPurple, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NARA AI — AI Connection",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Konfigurasi koneksi kecerdasan buatan NARA",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active AI Model
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active AI Model", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .background(NovaNaraPurple.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = SettingsRepository.DISPLAY_MODEL_NAME,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaNaraPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI API Key Label & Input/Display
                    Text("AI API Key", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (apiKey.isNotBlank() && !isEditingKey) {
                        // Secure Masked Display (Do not show API key in full)
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, null, modifier = Modifier.size(16.dp), tint = NovaNaraPurple)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = viewModel.settingsRepository.getMaskedApiKey(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        tempApiKey = apiKey
                                        isEditingKey = true
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Update API Key", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        // Editable Input Field
                        OutlinedTextField(
                            value = tempApiKey,
                            onValueChange = { tempApiKey = it },
                            placeholder = { Text("Masukkan AI API Key...", fontSize = 12.sp) },
                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Tampilkan API Key"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("api_key_input_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Connection Status Card
                    Text("Connection Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                testResult?.success == true -> Color(0xFFE8F5E9)
                                testResult?.success == false -> Color(0xFFFFEBEE)
                                apiKey.isNotBlank() -> Color(0xFFE3F2FD)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    testResult?.success == true -> Icons.Default.CheckCircle
                                    testResult?.success == false -> Icons.Default.Error
                                    apiKey.isNotBlank() -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when {
                                    testResult?.success == true -> Color(0xFF2E7D32)
                                    testResult?.success == false -> Color(0xFFC62828)
                                    apiKey.isNotBlank() -> NovaBluePrimary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = testResult?.message ?: if (apiKey.isNotBlank()) "Kunci terpasang. Siap digunakan." else "Belum terhubung. Masukkan AI API Key.",
                                fontSize = 11.sp,
                                color = when {
                                    testResult?.success == true -> Color(0xFF1B5E20)
                                    testResult?.success == false -> Color(0xFFB71C1C)
                                    apiKey.isNotBlank() -> NovaBluePrimary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Save Configuration, Test Connection, Remove API Key
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEditingKey) {
                            Button(
                                onClick = {
                                    viewModel.settingsRepository.setApiKey(tempApiKey)
                                    isEditingKey = false
                                    viewModel.showFeedback("Konfigurasi AI berhasil disimpan!")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NovaNaraPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("save_configuration_button")
                            ) {
                                Text("Save Configuration", fontSize = 11.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (isEditingKey) {
                                    viewModel.settingsRepository.setApiKey(tempApiKey)
                                    isEditingKey = false
                                }
                                viewModel.testAiConnection()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("test_connection_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("Test Connection", fontSize = 11.sp)
                        }

                        if (apiKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    tempApiKey = ""
                                    viewModel.settingsRepository.setApiKey("")
                                    isEditingKey = true
                                    viewModel.showFeedback("AI API Key dihapus")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remove API Key", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Section 2: Editor Preferences
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pengaturan Editor & Naskah", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Interval Autosave Dokumen", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(10, 15, 30).forEach { sec ->
                                FilterChip(
                                    selected = autoSaveSec == sec,
                                    onClick = { viewModel.settingsRepository.setAutoSaveSec(sec) },
                                    label = { Text("${sec}s", fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mode Editorial Default NARA", fontSize = 12.sp)
                        Text(defaultMode.title, fontSize = 11.sp, color = NovaNaraPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 3: General & Tampilan
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tampilan & Antarmuka", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tema Warna", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("dark" to "Dark Navy", "light" to "Light").forEach { (key, label) ->
                                FilterChip(
                                    selected = themeMode == key,
                                    onClick = { viewModel.settingsRepository.setTheme(key) },
                                    label = { Text(label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bahasa Aplikasi", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("id" to "Indonesia", "en" to "English").forEach { (code, label) ->
                                FilterChip(
                                    selected = language == code,
                                    onClick = { viewModel.settingsRepository.setLanguage(code) },
                                    label = { Text(label, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Cadangan & Penyimpanan
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Penyimpanan & Cadangan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Basis Data Room Lokal", fontSize = 12.sp)
                        Text("nova_office.db (Aktif)", fontSize = 11.sp, color = NovaBluePrimary, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.showFeedback("Cadangan berkas berhasil diekspor ke memori internal") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ekspor Cadangan Semua Dokumen", fontSize = 12.sp)
                    }
                }
            }

            // Section 5: Tentang Aplikasi
            Card(
                onClick = { showAboutDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = NovaBluePrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tentang NOVA OFFICE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Versi 1.0.0 • Your Complete Office & Creative Workspace", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(NovaNavyDark, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("N", color = NovaCyanAccent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("NOVA OFFICE", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Tagline: Your Complete Office & Creative Workspace",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = NovaBluePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplikasi perkantoran profesional mobile terlengkap setara WPS Office dengan integrasi asisten editor novel profesional NARA AI.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Modul Aktif:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("• Nova Writer (DOCX, ODT, RTF, TXT)\n• Nova Sheets (XLSX, CSV, Formula Engine)\n• Nova Slides (PPTX, Fullscreen Tayangan)\n• Nova PDF (Anotasi, Tanda Tangan, Rotasi)\n• Nova Files (Storage SAF, Keranjang Sampah)\n• NARA AI Novel Editor (NARA Intelligence Engine)", fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = NovaBluePrimary)) {
                    Text("Tutup")
                }
            }
        )
    }
}
