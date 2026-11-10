package com.example.ui.slides

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.FloatingNaraButton
import com.example.ui.theme.NovaNavyDark
import com.example.ui.theme.NovaSlidesOrange
import com.example.ui.viewmodel.NovaOfficeViewModel

data class SlideItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var layout: String = "CONTENT", // TITLE, CONTENT, TWO_COLUMN, QUOTE
    var title: String = "Judul Slide",
    var subtitle: String = "Subjudul slide presentasi",
    var body: String = "• Poin pertama\n• Poin kedua pembahasan materi",
    var speakerNotes: String = "Catatan presenter untuk slide ini."
)

enum class SlideTheme(val title: String, val bg: Color, val text: Color, val accent: Color) {
    EXECUTIVE_NAVY("Executive Navy", Color(0xFF0B192C), Color.White, Color(0xFF00D2FF)),
    CHARCOAL_DARK("Charcoal Dark", Color(0xFF1E201E), Color.White, Color(0xFFF5A623)),
    WARM_CORAL("Warm Coral", Color(0xFFFAF0EE), Color(0xFF2D3142), NovaSlidesOrange),
    MINIMAL_WHITE("Minimal White", Color.White, Color(0xFF0F172A), NovaSlidesOrange)
}

@Composable
fun NovaSlidesScreen(
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

    var slides by remember {
        mutableStateOf(
            listOf(
                SlideItem(layout = "TITLE", title = "NOVA Creative Studio", subtitle = "Presentasi Strategis & Editorial 2026"),
                SlideItem(layout = "CONTENT", title = "Lanskap Industri Saat Ini", body = "• Kebutuhan penerbitan cepat meningkat\n• Standar naskah semakin kompetitif\n• AI konvensional kehilangan rasa sastra"),
                SlideItem(layout = "QUOTE", title = "Manifesto Sastra", subtitle = "Sebuah karya besar hidup dari ritme jiwa, bukan sekadar kompilasi kata."),
                SlideItem(layout = "CONTENT", title = "Rencana Implementasi", body = "1. Penyuntingan terpadu dengan NARA AI\n2. Penerbitan format ganda DOCX & PDF\n3. Distribusi global multi-channel")
            )
        )
    }

    var currentSlideIndex by remember { mutableStateOf(0) }
    var currentTheme by remember { mutableStateOf(SlideTheme.EXECUTIVE_NAVY) }
    var isFullscreenShow by remember { mutableStateOf(false) }
    var showNotesDrawer by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    fun syncSlidesToDoc() {
        val encoded = slides.joinToString("\n") { s ->
            "${s.id}|${s.layout}|${s.title}|${s.subtitle}|${s.body}|${s.speakerNotes}"
        }
        viewModel.updateCurrentDocumentContent(encoded)
    }

    val activeSlide = slides.getOrNull(currentSlideIndex) ?: slides.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Header
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
                    syncSlidesToDoc()
                    viewModel.saveCurrentDocument()
                    onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentDoc?.title ?: "Presentasi Nova",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Nova Slides • ${slides.size} Slide",
                        fontSize = 11.sp,
                        color = NovaSlidesOrange
                    )
                }

                // Fullscreen Slideshow button
                Button(
                    onClick = { isFullscreenShow = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaSlidesOrange),
                    contentPadding = ButtonDefaults.ContentPadding,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("slides_play_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tayang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = { showThemeMenu = true }) {
                    Icon(Icons.Default.Palette, contentDescription = "Tema", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = {
                    syncSlidesToDoc()
                    viewModel.saveCurrentDocument()
                    viewModel.showFeedback("Presentasi disimpan")
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Simpan", tint = NovaSlidesOrange)
                }

                DropdownMenu(expanded = showThemeMenu, onDismissRequest = { showThemeMenu = false }) {
                    SlideTheme.values().forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.title) },
                            onClick = {
                                currentTheme = theme
                                showThemeMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Center: Interactive Slide Canvas (16:9 Aspect Ratio)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = currentTheme.bg),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .shadow(12.dp, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = when (activeSlide.layout) {
                        "TITLE", "QUOTE" -> Arrangement.Center
                        else -> Arrangement.Top
                    },
                    horizontalAlignment = when (activeSlide.layout) {
                        "TITLE", "QUOTE" -> Alignment.CenterHorizontally
                        else -> Alignment.Start
                    }
                ) {
                    // Editable Slide Title
                    BasicTextField(
                        value = activeSlide.title,
                        onValueChange = {
                            val copy = slides.toMutableList()
                            copy[currentSlideIndex] = activeSlide.copy(title = it)
                            slides = copy
                            syncSlidesToDoc()
                        },
                        textStyle = TextStyle(
                            fontSize = if (activeSlide.layout == "TITLE") 22.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.text,
                            textAlign = if (activeSlide.layout == "TITLE") TextAlign.Center else TextAlign.Start
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("slide_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeSlide.layout == "TITLE" || activeSlide.layout == "QUOTE") {
                        BasicTextField(
                            value = activeSlide.subtitle,
                            onValueChange = {
                                val copy = slides.toMutableList()
                                copy[currentSlideIndex] = activeSlide.copy(subtitle = it)
                                slides = copy
                                syncSlidesToDoc()
                            },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = currentTheme.accent,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        BasicTextField(
                            value = activeSlide.body,
                            onValueChange = {
                                val copy = slides.toMutableList()
                                copy[currentSlideIndex] = activeSlide.copy(body = it)
                                slides = copy
                                syncSlidesToDoc()
                            },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = currentTheme.text.copy(alpha = 0.9f),
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                        )
                    }
                }
            }

            // Floating NARA Button
            FloatingNaraButton(
                onClick = { viewModel.setNaraSheetOpen(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp),
                label = "NARA Slides"
            )
        }

        // Layout Picker Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "TITLE" to "Judul",
                "CONTENT" to "Konten",
                "QUOTE" to "Kutipan"
            ).forEach { (layoutKey, label) ->
                FilterChip(
                    selected = activeSlide.layout == layoutKey,
                    onClick = {
                        val copy = slides.toMutableList()
                        copy[currentSlideIndex] = activeSlide.copy(layout = layoutKey)
                        slides = copy
                        syncSlidesToDoc()
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }

            FilterChip(
                selected = showNotesDrawer,
                onClick = { showNotesDrawer = !showNotesDrawer },
                label = { Text("Catatan Pembicara", fontSize = 11.sp) }
            )
        }

        // Speaker notes collapsible drawer
        if (showNotesDrawer) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Catatan Pembicara (Speaker Notes):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = activeSlide.speakerNotes,
                        onValueChange = {
                            val copy = slides.toMutableList()
                            copy[currentSlideIndex] = activeSlide.copy(speakerNotes = it)
                            slides = copy
                            syncSlidesToDoc()
                        },
                        textStyle = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Bottom Thumbnail Strip Carousel
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(slides) { index, slide ->
                        val isSelected = index == currentSlideIndex
                        Card(
                            onClick = { currentSlideIndex = index },
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = currentTheme.bg),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(NovaSlidesOrange),
                                width = 2.dp
                            ) else null,
                            modifier = Modifier
                                .width(70.dp)
                                .height(45.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = currentTheme.text,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Slide Actions: Add, Duplicate, Delete
                IconButton(onClick = {
                    val newSlide = SlideItem(title = "Slide Baru ${slides.size + 1}")
                    slides = slides + newSlide
                    currentSlideIndex = slides.size - 1
                    syncSlidesToDoc()
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Slide", tint = NovaSlidesOrange)
                }

                IconButton(onClick = {
                    val dupe = activeSlide.copy(id = java.util.UUID.randomUUID().toString(), title = "${activeSlide.title} (Salinan)")
                    slides = slides + dupe
                    currentSlideIndex = slides.size - 1
                    syncSlidesToDoc()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplikasi Slide", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (slides.size > 1) {
                    IconButton(onClick = {
                        slides = slides.filterIndexed { i, _ -> i != currentSlideIndex }
                        currentSlideIndex = currentSlideIndex.coerceAtMost(slides.size - 1)
                        syncSlidesToDoc()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Slide", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    // Fullscreen Slideshow Modal
    if (isFullscreenShow) {
        Dialog(
            onDismissRequest = { isFullscreenShow = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(currentTheme.bg)
                    .padding(24.dp)
            ) {
                // Exit button
                IconButton(
                    onClick = { isFullscreenShow = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Keluar Tayangan", tint = currentTheme.text)
                }

                // Slide counter
                Text(
                    text = "${currentSlideIndex + 1} / ${slides.size}",
                    color = currentTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // Slide Content in Fullscreen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 64.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = if (activeSlide.layout == "TITLE" || activeSlide.layout == "QUOTE") Alignment.CenterHorizontally else Alignment.Start
                ) {
                    Text(
                        text = activeSlide.title,
                        color = currentTheme.text,
                        fontSize = if (activeSlide.layout == "TITLE") 32.sp else 26.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = if (activeSlide.layout == "TITLE") TextAlign.Center else TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (activeSlide.layout == "TITLE" || activeSlide.layout == "QUOTE") {
                        Text(
                            text = activeSlide.subtitle,
                            color = currentTheme.accent,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = activeSlide.body,
                            color = currentTheme.text.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            lineHeight = 28.sp
                        )
                    }
                }

                // Prev / Next Controls at bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (currentSlideIndex > 0) currentSlideIndex--
                        },
                        enabled = currentSlideIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Sebelumnya", tint = currentTheme.text)
                    }

                    IconButton(
                        onClick = {
                            if (currentSlideIndex < slides.size - 1) currentSlideIndex++
                        },
                        enabled = currentSlideIndex < slides.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Berikutnya", tint = currentTheme.text)
                    }
                }
            }
        }
    }
}
