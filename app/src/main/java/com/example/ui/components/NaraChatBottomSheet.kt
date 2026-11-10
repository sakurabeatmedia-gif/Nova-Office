package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiChatMessage
import com.example.data.model.EditorialMode
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaNavyDark
import com.example.ui.viewmodel.NovaOfficeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaraChatBottomSheet(
    viewModel: NovaOfficeViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val messages by viewModel.naraChatMessages.collectAsState()
    val isGenerating by viewModel.isNaraGenerating.collectAsState()
    val selectedMode by viewModel.selectedEditorialMode.collectAsState()
    val currentDoc by viewModel.currentDocument.collectAsState()
    val activeProject by viewModel.documentRepository.currentActiveProject.collectAsState()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var showModeDropdown by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(NovaNaraPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "NARA",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NARA AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(NovaNaraPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Editor Novel",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaNaraPurple
                            )
                        }
                    }
                    Text(
                        text = if (activeProject != null) "Proyek: ${activeProject?.title}" else "Workspace Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Clear Chat Button
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Bersihkan Chat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Close Button
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Mode Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showModeDropdown = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = NovaNaraPurple
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mode: ${selectedMode.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showModeDropdown,
                        onDismissRequest = { showModeDropdown = false }
                    ) {
                        EditorialMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(mode.title, fontWeight = FontWeight.Medium)
                                        Text(
                                            mode.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setEditorialMode(mode)
                                    showModeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Active document badge
                if (currentDoc != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NovaCyanAccent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Naskah: ${currentDoc?.title?.take(18)}...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NovaCyanAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Quick Prompt Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "Poles Dialog Tokoh" to "Tolong poles dialog dalam naskah agar terdengar lebih tajam dan alami sesuai kepribadian tokoh.",
                    "Show, Don't Tell" to "Tolong evaluasi adegan ini dengan prinsip Show Don't Tell. Manfaatkan detail sensorik.",
                    "Analisis Pacing" to "Bagaimana tempo (pacing) ketegangan adegan ini? Apakah terlalu terburu-buru atau bertele-tele?",
                    "Cek Konsistensi Karakter" to "Apakah reaksi tokoh dalam naskah ini konsisten dengan motivasi internalnya?",
                    "Tinjau Pembuka Bab" to "Tolong beri masukan mendalam mengenai kekuatan hook dan atmosfer kalimat pembuka bab ini."
                )

                items(chips) { (label, prompt) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable {
                            viewModel.sendNaraMessage(prompt)
                        }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(msg.content))
                            viewModel.showFeedback("Teks disalin ke clipboard")
                        }
                    )
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = NovaNaraPurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NARA AI sedang menganalisis naskah...",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaNaraPurple
                            )
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text("Tanyakan atau instruksikan NARA...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nara_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovaNaraPurple,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
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
                        modifier = Modifier
                            .testTag("nara_send_button")
                            .background(
                                if (inputPrompt.isNotBlank()) NovaNaraPurple else Color.Gray.copy(alpha = 0.3f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Kirim",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: AiChatMessage,
    onCopy: () -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(NovaNaraPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) NovaNavyDark else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = if (isUser) 0.dp else 2.dp,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.9f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NARA AI • ${message.editorialMode.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaNaraPurple
                        )
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                val cleanContent = remember(message.content) {
                    com.example.data.ai.GeminiApiService().sanitizeEditorialOutput(message.content)
                }
                Text(
                    text = cleanContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 21.sp
                )
            }
        }
    }
}
