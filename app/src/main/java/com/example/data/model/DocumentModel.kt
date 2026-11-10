package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaPdfRed
import com.example.ui.theme.NovaSheetsGreen
import com.example.ui.theme.NovaSlidesOrange
import com.example.ui.theme.NovaWriterBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DocumentType(
    val extension: String,
    val displayName: String,
    val color: Color
) {
    WRITER(".docx", "Document", NovaWriterBlue),
    SHEETS(".xlsx", "Spreadsheet", NovaSheetsGreen),
    SLIDES(".pptx", "Presentation", NovaSlidesOrange),
    PDF(".pdf", "PDF Document", NovaPdfRed),
    TXT(".txt", "Text File", NovaNaraPurple);

    companion object {
        fun fromExtension(ext: String): DocumentType {
            return when (ext.lowercase().trimStart('.')) {
                "docx", "doc", "odt", "rtf" -> WRITER
                "xlsx", "xls", "csv", "ods" -> SHEETS
                "pptx", "ppt", "odp" -> SLIDES
                "pdf" -> PDF
                else -> TXT
            }
        }
    }
}

data class OfficeDocument(
    val id: String,
    val title: String,
    val type: DocumentType,
    val content: String,
    val lastModified: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 1024L,
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val folderName: String = "Documents",
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val version: Int = 1
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    val formattedSize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format(Locale.US, "%.1f MB", sizeBytes.toDouble() / (1024 * 1024))
        }
}

enum class EditorialMode(val title: String, val description: String) {
    PROOFREADING("Proofreading", "Penyuntingan ejaan, tipo, dan tata bahasa teknis."),
    COPY_EDITING("Copy Editing", "Penyuntingan konsistensi diksi, tanda baca, dan mekanika penulisan."),
    LINE_EDITING("Line Editing", "Penyuntingan ritme kalimat, kejelasan rasa bahasa, dan variasi prosa."),
    DEVELOPMENTAL_EDITING("Developmental Editing", "Evaluasi struktur cerita, kedalaman tema, dan relevansi adegan."),
    DIALOGUE_EDITING("Dialogue Editing", "Penyuntingan suara tokoh alami, subteks, dan dialek tanpa robotik."),
    NARRATIVE_EDITING("Narrative Editing", "Penyempurnaan show vs tell, kedalaman sensoris, dan eksposisi."),
    CONTINUITY_CHECKING("Continuity Checking", "Pemeriksaan konsistensi linimasa, properti, dan fakta dunia cerita."),
    CHARACTER_CONSISTENCY("Character Consistency", "Pemeriksaan konsistensi kepribadian, motivasi, dan perilaku tokoh."),
    PLOT_ANALYSIS("Plot Analysis", "Analisis kausalitas plot, lubang logika alur, dan taruhan konflik."),
    PACING_ANALYSIS("Pacing Analysis", "Analisis tempo ketegangan naratif, scene versus sequel."),
    LITERARY_CRITIQUE("Literary Critique", "Ulasan sastrawi menyeluruh berstandar penerbit profesional."),
    CHAPTER_REVIEW("Chapter Review", "Pemeriksaan pembuka bab, hooks, klimaks adegan, dan penutup bab.")
}

data class CharacterProfile(
    val id: String,
    val name: String,
    val role: String, // Protagonist, Antagonist, Mentor, Supporting
    val personality: String,
    val internalConflict: String,
    val voiceNotes: String,
    val relationships: String
)

data class WorldItem(
    val id: String,
    val category: String, // Location, Technology, Magic, Faction, History
    val name: String,
    val description: String
)

data class TimelineEvent(
    val id: String,
    val phase: String,
    val title: String,
    val description: String,
    val orderIndex: Int
)

data class NovelProject(
    val id: String,
    val title: String,
    val genre: String,
    val premise: String,
    val characters: List<CharacterProfile> = emptyList(),
    val worldbuilding: List<WorldItem> = emptyList(),
    val timeline: List<TimelineEvent> = emptyList(),
    val styleNotes: String = "Gaya prosa berfokus pada kedalaman psikologis dan dinamika sensorik."
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user", "nara", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedExcerpt: String? = null,
    val editorialMode: EditorialMode = EditorialMode.LINE_EDITING,
    val originalText: String? = null,
    val suggestedRevision: String? = null,
    val explanation: String? = null,
    val isDiffApplied: Boolean = false
)
