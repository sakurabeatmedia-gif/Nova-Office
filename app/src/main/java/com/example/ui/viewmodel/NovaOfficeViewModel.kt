package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ConnectionTestResult
import com.example.data.ai.GeminiApiService
import com.example.data.ai.NaraEditResult
import com.example.data.model.AiChatMessage
import com.example.data.model.CharacterProfile
import com.example.data.model.DocumentType
import com.example.data.model.EditorialMode
import com.example.data.model.NovelProject
import com.example.data.model.OfficeDocument
import com.example.data.model.TimelineEvent
import com.example.data.model.WorldItem
import com.example.data.repository.DocumentRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class NovaOfficeViewModel(application: Application) : AndroidViewModel(application) {
    val documentRepository = DocumentRepository(application)
    val settingsRepository = SettingsRepository(application)
    private val geminiService = GeminiApiService()

    // Search and Category Filter for Dashboard and Files
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredDocuments: StateFlow<List<OfficeDocument>> = combine(
        documentRepository.activeDocuments,
        _searchQuery,
        _selectedCategory
    ) { docs, query, category ->
        docs.filter { doc ->
            val matchesQuery = query.isBlank() || doc.title.contains(query, ignoreCase = true) || doc.content.contains(query, ignoreCase = true)
            val matchesCategory = when (category) {
                "All" -> true
                "Documents" -> doc.type == DocumentType.WRITER
                "Spreadsheets" -> doc.type == DocumentType.SHEETS
                "Presentations" -> doc.type == DocumentType.SLIDES
                "PDFs" -> doc.type == DocumentType.PDF
                "Text Files" -> doc.type == DocumentType.TXT
                "Favorites" -> doc.isFavorite
                else -> true
            }
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashDocuments: StateFlow<List<OfficeDocument>> = documentRepository.trashDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Document being edited
    private val _currentDocument = MutableStateFlow<OfficeDocument?>(null)
    val currentDocument: StateFlow<OfficeDocument?> = _currentDocument.asStateFlow()

    private val _isAutosaving = MutableStateFlow(false)
    val isAutosaving: StateFlow<Boolean> = _isAutosaving.asStateFlow()

    private val _lastSaveTime = MutableStateFlow(System.currentTimeMillis())
    val lastSaveTime: StateFlow<Long> = _lastSaveTime.asStateFlow()

    // Floating NARA Sheet
    private val _isNaraSheetOpen = MutableStateFlow(false)
    val isNaraSheetOpen: StateFlow<Boolean> = _isNaraSheetOpen.asStateFlow()

    // NARA Chat State
    private val _naraChatMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                role = "nara",
                content = """Halo! Saya NARA AI, editor novel dan rekan kreatif profesional Anda di NOVA OFFICE.

Saya siap mendampingi Anda memoles naskah, menajamkan dialog, menganalisis ritme (pacing) cerita, menjaga konsistensi karakter, hingga ulasan bab berstandar penerbitan.

Apa yang ingin kita kerjakan hari ini? Anda dapat memilih teks dari naskah, berdiskusi mengenai alur, atau mengaktifkan mode editorial khusus.""",
                editorialMode = EditorialMode.LINE_EDITING
            )
        )
    )
    val naraChatMessages: StateFlow<List<AiChatMessage>> = _naraChatMessages.asStateFlow()

    private val _isNaraGenerating = MutableStateFlow(false)
    val isNaraGenerating: StateFlow<Boolean> = _isNaraGenerating.asStateFlow()

    private val _selectedEditorialMode = MutableStateFlow(EditorialMode.LINE_EDITING)
    val selectedEditorialMode: StateFlow<EditorialMode> = _selectedEditorialMode.asStateFlow()

    // In-Editor Diff Proposal Dialog State
    private val _pendingDiffProposal = MutableStateFlow<NaraEditResult?>(null)
    val pendingDiffProposal: StateFlow<NaraEditResult?> = _pendingDiffProposal.asStateFlow()

    // Connection Test State
    private val _connectionTestResult = MutableStateFlow<ConnectionTestResult?>(null)
    val connectionTestResult: StateFlow<ConnectionTestResult?> = _connectionTestResult.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    // UI Feedback Banner
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private var autosaveJob: Job? = null

    init {
        startAutosaveLoop()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setNaraSheetOpen(isOpen: Boolean) {
        _isNaraSheetOpen.value = isOpen
    }

    fun setEditorialMode(mode: EditorialMode) {
        _selectedEditorialMode.value = mode
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun showFeedback(msg: String) {
        _userFeedbackMessage.value = msg
    }

    fun loadDocument(id: String) {
        viewModelScope.launch {
            val doc = documentRepository.getDocumentById(id)
            _currentDocument.value = doc
        }
    }

    fun updateCurrentDocumentContent(newContent: String) {
        val current = _currentDocument.value ?: return
        _currentDocument.value = current.copy(
            content = newContent,
            lastModified = System.currentTimeMillis()
        )
    }

    fun updateCurrentDocumentTitle(newTitle: String) {
        val current = _currentDocument.value ?: return
        _currentDocument.value = current.copy(
            title = newTitle,
            lastModified = System.currentTimeMillis()
        )
        viewModelScope.launch {
            documentRepository.renameDocument(current.id, newTitle)
        }
    }

    fun saveCurrentDocument() {
        val current = _currentDocument.value ?: return
        viewModelScope.launch {
            _isAutosaving.value = true
            documentRepository.saveDocument(current)
            _lastSaveTime.value = System.currentTimeMillis()
            delay(400)
            _isAutosaving.value = false
        }
    }

    private fun startAutosaveLoop() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            while (true) {
                delay(15000)
                _currentDocument.value?.let { doc ->
                    _isAutosaving.value = true
                    documentRepository.saveDocument(doc)
                    _lastSaveTime.value = System.currentTimeMillis()
                    delay(300)
                    _isAutosaving.value = false
                }
            }
        }
    }

    fun createDocument(title: String, type: DocumentType, onCreated: (OfficeDocument) -> Unit) {
        viewModelScope.launch {
            val doc = documentRepository.createNewDocument(title, type)
            _currentDocument.value = doc
            showFeedback("Dokumen '${doc.title}' berhasil dibuat")
            onCreated(doc)
        }
    }

    fun duplicateDocument(id: String) {
        viewModelScope.launch {
            val copy = documentRepository.duplicateDocument(id)
            if (copy != null) {
                showFeedback("Salinan '${copy.title}' berhasil dibuat")
            }
        }
    }

    fun moveToTrash(id: String) {
        viewModelScope.launch {
            documentRepository.moveToTrash(id)
            showFeedback("Dokumen dipindahkan ke Keranjang Sampah")
        }
    }

    fun restoreFromTrash(id: String) {
        viewModelScope.launch {
            documentRepository.restoreFromTrash(id)
            showFeedback("Dokumen berhasil dipulihkan")
        }
    }

    fun deletePermanently(id: String) {
        viewModelScope.launch {
            documentRepository.deletePermanently(id)
            showFeedback("Dokumen dihapus permanen")
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            documentRepository.emptyTrash()
            showFeedback("Keranjang sampah telah dikosongkan")
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            documentRepository.toggleFavorite(id)
        }
    }

    fun renameDocument(id: String, newTitle: String) {
        viewModelScope.launch {
            documentRepository.renameDocument(id, newTitle)
            showFeedback("Nama dokumen berhasil diubah")
        }
    }

    fun importDocument(fileName: String, content: String, onImported: (OfficeDocument) -> Unit) {
        viewModelScope.launch {
            val doc = documentRepository.importExternalDocument(fileName, content)
            showFeedback("Berkas '$fileName' berhasil diimpor")
            onImported(doc)
        }
    }

    // NARA AI Chat and Novel Editing
    fun sendNaraMessage(prompt: String, attachedText: String? = null) {
        if (prompt.isBlank() && attachedText.isNullOrBlank()) return

        val userMessage = AiChatMessage(
            role = "user",
            content = prompt,
            attachedExcerpt = attachedText,
            editorialMode = _selectedEditorialMode.value
        )
        _naraChatMessages.value = _naraChatMessages.value + userMessage
        _isNaraGenerating.value = true

        viewModelScope.launch {
            val apiKey = settingsRepository.apiKey.value
            val model = settingsRepository.model.value
            val currentProject = documentRepository.currentActiveProject.value
            val history = _naraChatMessages.value.map { it.role to it.content }

            val response = geminiService.generateEditorialResponse(
                apiKey = apiKey,
                model = GeminiApiService.REQUIRED_MODEL_ID,
                prompt = prompt,
                mode = _selectedEditorialMode.value,
                contextText = attachedText ?: _currentDocument.value?.content,
                novelProject = currentProject,
                chatHistory = history
            )

            val naraMessage = AiChatMessage(
                role = "nara",
                content = response,
                editorialMode = _selectedEditorialMode.value
            )
            _naraChatMessages.value = _naraChatMessages.value + naraMessage
            _isNaraGenerating.value = false
        }
    }

    fun requestEditSelection(selectedSnippet: String, instruction: String) {
        _isNaraGenerating.value = true
        viewModelScope.launch {
            val apiKey = settingsRepository.apiKey.value
            val currentProject = documentRepository.currentActiveProject.value

            val diffResult = geminiService.editSelectionWithDiff(
                apiKey = apiKey,
                model = GeminiApiService.REQUIRED_MODEL_ID,
                originalSnippet = selectedSnippet,
                instruction = instruction,
                mode = _selectedEditorialMode.value,
                novelProject = currentProject
            )

            _pendingDiffProposal.value = diffResult
            _isNaraGenerating.value = false
        }
    }

    fun applyPendingDiff() {
        val diff = _pendingDiffProposal.value ?: return
        val currentDoc = _currentDocument.value ?: return

        // Replace original snippet in document
        val updatedContent = if (currentDoc.content.contains(diff.originalText)) {
            currentDoc.content.replace(diff.originalText, diff.suggestedRevision)
        } else {
            // Append or prepend if not found verbatim
            currentDoc.content + "\n\n" + diff.suggestedRevision
        }

        updateCurrentDocumentContent(updatedContent)
        saveCurrentDocument()
        showFeedback("Revisi NARA AI berhasil diterapkan pada dokumen!")
        _pendingDiffProposal.value = null
    }

    fun dismissPendingDiff() {
        _pendingDiffProposal.value = null
        showFeedback("Saran revisi dibatalkan.")
    }

    fun clearChat() {
        _naraChatMessages.value = listOf(
            AiChatMessage(
                role = "nara",
                content = "Riwayat percakapan telah dibersihkan. Silakan beri tahu saya naskah atau adegan yang ingin kita ulas selanjutnya.",
                editorialMode = _selectedEditorialMode.value
            )
        )
    }

    fun testAiConnection() {
        val apiKey = settingsRepository.apiKey.value
        _isTestingConnection.value = true
        _connectionTestResult.value = null

        viewModelScope.launch {
            val result = geminiService.testConnection(apiKey, GeminiApiService.REQUIRED_MODEL_ID)
            _connectionTestResult.value = result
            _isTestingConnection.value = false
        }
    }

    fun testGeminiConnection() = testAiConnection()

    fun updateNovelProject(project: NovelProject) {
        documentRepository.updateNovelProject(project)
        showFeedback("Proyek novel '${project.title}' diperbarui")
    }

    fun addCharacterToActiveProject(character: CharacterProfile) {
        val current = documentRepository.currentActiveProject.value ?: return
        val updated = current.copy(characters = current.characters + character)
        documentRepository.updateNovelProject(updated)
        showFeedback("Tokoh '${character.name}' ditambahkan ke basis data proyek")
    }

    fun addWorldItemToActiveProject(item: WorldItem) {
        val current = documentRepository.currentActiveProject.value ?: return
        val updated = current.copy(worldbuilding = current.worldbuilding + item)
        documentRepository.updateNovelProject(updated)
        showFeedback("Item worldbuilding '${item.name}' ditambahkan")
    }

    fun addTimelineEventToActiveProject(event: TimelineEvent) {
        val current = documentRepository.currentActiveProject.value ?: return
        val updated = current.copy(timeline = current.timeline + event)
        documentRepository.updateNovelProject(updated)
        showFeedback("Peristiwa '${event.title}' dicatat pada linimasa")
    }
}
