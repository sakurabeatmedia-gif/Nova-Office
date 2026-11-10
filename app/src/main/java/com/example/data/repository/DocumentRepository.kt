package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.data.model.CharacterProfile
import com.example.data.model.DocumentType
import com.example.data.model.NovelProject
import com.example.data.model.OfficeDocument
import com.example.data.model.TimelineEvent
import com.example.data.model.WorldItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class DocumentRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val dao = database.documentDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    val activeDocuments: Flow<List<OfficeDocument>> =
        dao.getAllActiveDocuments().map { list -> list.map { it.toModel() } }

    val trashDocuments: Flow<List<OfficeDocument>> =
        dao.getTrashDocuments().map { list -> list.map { it.toModel() } }

    val favoriteDocuments: Flow<List<OfficeDocument>> =
        dao.getFavoriteDocuments().map { list -> list.map { it.toModel() } }

    // Active Novel Projects in Memory / Storage
    private val _novelProjects = MutableStateFlow<List<NovelProject>>(emptyList())
    val novelProjects: StateFlow<List<NovelProject>> = _novelProjects.asStateFlow()

    private val _currentActiveProject = MutableStateFlow<NovelProject?>(null)
    val currentActiveProject: StateFlow<NovelProject?> = _currentActiveProject.asStateFlow()

    init {
        scope.launch {
            initSampleDataIfNeeded()
            initNovelProjects()
        }
    }

    private suspend fun initSampleDataIfNeeded() {
        val count = dao.getActiveCount()
        if (count == 0) {
            val sampleDocs = listOf(
                OfficeDocument(
                    id = "doc-novel-ch1",
                    title = "Bayang di Balik Kabut - Bab 1",
                    type = DocumentType.WRITER,
                    content = """BAB 1: JEJAK DI TEPI BATAVIA

Hujan deras mengguyur atap seng gudang tua di pelabuhan Sunda Kelapa. Udara malam itu berbau minyak solar dan garam laut yang pekat. Raditya merapatkan kerah mantel kelabunya, membiarkan tempias air menghantam pipinya yang dingin. 

Di seberang dermaga, sebuah siluet jangkung berdiri mematung di bawah temaram lentera minyak. Sosok itu tidak bergerak, seolah-olah sengaja menunggu kedatangannya.

"Kamu terlambat lima menit, Radit," suara serak itu memecah dengung angin laut. 

Raditya menarik napas panjang. Jemarinya di dalam saku mantel menggenggam erat selongsong kuningan berukir monogram keluarga Larasati—satu-satunya bukti peninggalan adiknya yang raib tanpa jejak tiga pekan silam.

"Lima menit tidak akan mengubah kenyataan bahwa berkas itu ada padamu, Hendra," balas Raditya dingin. Suaranya datar, tetapi urat lehernya menegang.

Hendra menyeringai tipis. Gigi emasnya berkilat sekilas di kegelapan. "Kenyataan? Di kota ini, Radit, kenyataan hanyalah naskah drama yang ditulis oleh siapa pun yang membayar paling mahal."

Tiba-tiba, deru mesin perahu motor membelah kesunyian muara. Sorot lampu halogen menyala menyilaukan pandangan keduanya.""",
                    lastModified = System.currentTimeMillis() - 1000 * 60 * 12, // 12 mins ago
                    sizeBytes = 28400L,
                    isFavorite = true,
                    folderName = "Naskah Novel",
                    wordCount = 184,
                    characterCount = 1240,
                    version = 3
                ),
                OfficeDocument(
                    id = "doc-sheets-finance",
                    title = "Anggaran Penerbitan & Royalti 2026",
                    type = DocumentType.SHEETS,
                    content = """Item,Kategori,Biaya Cetak,Distribusi,Target Penjualan,Proyeksi Royalti
Bab 1-5 Proofreading,Editorial,1500000,500000,2000,4500000
Desain Sampul Hardcover,Kreatif,2500000,300000,2000,4500000
Pencetakan Batch 1 (3000 eks),Produksi,18500000,4200000,3000,12000000
Pemasaran & Launching,Promosi,5000000,1200000,3000,12000000
Distribusi Toko Buku,Operasional,3200000,2800000,3000,12000000
Total,=SUM(C2:C6),=SUM(D2:D6),=SUM(E2:E6),=SUM(F2:F6)""",
                    lastModified = System.currentTimeMillis() - 1000 * 60 * 75,
                    sizeBytes = 14200L,
                    isFavorite = true,
                    folderName = "Finance",
                    wordCount = 56,
                    characterCount = 420,
                    version = 2
                ),
                OfficeDocument(
                    id = "doc-slides-pitch",
                    title = "Pitch Deck - NOVA Creative Studio",
                    type = DocumentType.SLIDES,
                    content = """SLIDE 1|TITLE|NOVA Creative & Editorial Studio|Revolusi Penerbitan Sastra Digital & Workspace Profesional
SLIDE 2|CONTENT|Masalah Industri Sastra Saat Ini|• Siklus editorial konvensional lambat (6-12 bulan per naskah)
• Penulis kesulitan menjaga konsistensi karakter dan ritme (pacing)
• Alat office generik tidak memahami nuansa fiksi dan kaidah sastra
SLIDE 3|TWO_COLUMN|Solusi Terintegrasi NARA AI|Sisi Penulis:
- Ruang tulis minim distraksi
- Analisis ritme dan dialog langsung
- Basis data tokoh & kontinuitas otomatis|Sisi Penerbit:
- Format dokumen DOCX & PDF siap cetak
- Ulasan manuskrip bertaraf industri
- Ekspor multi-format instan
SLIDE 4|QUOTE|"Sebuah karya besar bukan sekadar kata-kata yang tersusun rapi, melainkan jiwa yang bernapas dalam setiap jeda koma."|— NARA AI Editorial Manifesto""",
                    lastModified = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                    sizeBytes = 48500L,
                    isFavorite = false,
                    folderName = "Presentasi",
                    wordCount = 120,
                    characterCount = 890,
                    version = 1
                ),
                OfficeDocument(
                    id = "doc-pdf-contract",
                    title = "Kontrak Kerjasama Penerbitan Manuskrip",
                    type = DocumentType.PDF,
                    content = """SURAT PERJANJIAN PENERBITAN KARYA SASTRA
Nomor: 088/NOVA-PUB/X/2026

Pada hari ini, disepakati perjanjian penerbitan antara:
1. NOVA PUBLISHING HOUSE, berkedudukan di Jakarta Pusat.
2. PENULIS NASKAH ("Pihak Kedua").

PASAL 1: HAK CIPTA DAN LISENSI
Hak cipta moral tetap menjadi milik eksklusif Penulis. Pihak Pertama memperoleh hak eksklusif penerbitan dalam format cetak, e-book, dan adaptasi audio selama jangka waktu 5 (lima) tahun kalender.

PASAL 2: TAHAPAN EDITORIAL
Pihak Pertama menunjuk NARA AI dan Editor Kepala untuk melakukan pendampingan:
- Proofreading dan Copy Editing
- Line Editing dan Ulasan Pacing Bab demi Bab
- Persetujuan akhir naskah oleh Penulis sebelum proses cetak (Print Ready).

PASAL 3: SISTEM ROYALTI
Penulis berhak atas royalti sebesar 12.5% dari harga jual eceran (HET) untuk edisi cetak dan 25% dari penjualan bersih edisi digital.

Tertanda,
Pihak Pertama & Pihak Kedua""",
                    lastModified = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                    sizeBytes = 82400L,
                    isFavorite = true,
                    folderName = "Legal",
                    wordCount = 145,
                    characterCount = 980,
                    version = 1
                ),
                OfficeDocument(
                    id = "doc-txt-notes",
                    title = "Catatan Riset Worldbuilding & Karakter",
                    type = DocumentType.TXT,
                    content = """[ARSIP RISET: BATAVIA NOIR 1930]
1. Arsitektur Pelabuhan Sunda Kelapa:
- Kapal pinisi sandar di dermaga kayu besi
- Gudang peninggalan VOC dinding tebal berlumut
- Lentera minyak tanah keliling

2. Tokoh Raditya Pramana:
- Usia: 32 tahun
- Mantan inspektur kepolisian kolonial
- Trauma: kehilangan sang adik dalam skandal museum
- Bahasa tubuh: sering memutar cincin perak peninggalan ibunya bila sedang berpikir keras.""",
                    lastModified = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
                    sizeBytes = 5600L,
                    isFavorite = false,
                    folderName = "Catatan",
                    wordCount = 72,
                    characterCount = 490,
                    version = 1
                )
            )
            dao.insertAll(sampleDocs.map { DocumentEntity.fromModel(it) })
        }
    }

    private fun initNovelProjects() {
        val sampleProject = NovelProject(
            id = "proj-bayang-kabut",
            title = "Bayang di Balik Kabut",
            genre = "Misteri / Neo-Noir Sejarah",
            premise = "Seorang detektif swasta di Batavia tahun 1930 mengungkap konspirasi penyelundupan artefak kuno yang berkaitan dengan lenyapnya sang adik kandung.",
            characters = listOf(
                CharacterProfile(
                    id = "char-1",
                    name = "Raditya Pramana",
                    role = "Protagonis Utama",
                    personality = "Tenang, observatif, sinis, menyimpan duka mendalam.",
                    internalConflict = "Dilema antara menegakkan keadilan hukum atau balas dendam pribadi.",
                    voiceNotes = "Kalimat pendek, langsung pada sasaran, jarang tersenyum lebar.",
                    relationships = "Kakak kandung Larasati; bermusuhan dengan Hendra Wijaya."
                ),
                CharacterProfile(
                    id = "char-2",
                    name = "Hendra Wijaya",
                    role = "Antagonis / Pialang Pasar Gelap",
                    personality = "Karismatik, manipulatif, santai dalam situasi berbahaya.",
                    internalConflict = "Ketakutan kehilangan pengaruh dan kuasa di kalangan elit pelabuhan.",
                    voiceNotes = "Penuh metafora, nada suara ramah yang menyimpan ancaman terselubung.",
                    relationships = "Mantan kolega ayah Raditya yang membelot ke sindikat Batavia."
                ),
                CharacterProfile(
                    id = "char-3",
                    name = "Maya Larasati",
                    role = "Karakter Kunci / Adik Hilang",
                    personality = "Cerdas, peneliti sejarah muda yang berani mengambil risiko besar.",
                    internalConflict = "Mengetahui rahasia terlarang tentang peninggalan arkais Batavia.",
                    voiceNotes = "Puitis dan analitis saat mencatat temuan.",
                    relationships = "Adik Raditya; menjadi incaran sindikat."
                )
            ),
            worldbuilding = listOf(
                WorldItem(
                    id = "wb-1",
                    category = "Lokasi",
                    name = "Pelabuhan Sunda Kelapa Malam Hari",
                    description = "Dermaga kayu basah berselimut kabut garam, aroma minyak diesel bercampur asin laut, lampu lentera temaram."
                ),
                WorldItem(
                    id = "wb-2",
                    category = "Artefak",
                    name = "Selongsong Kuningan Monogram",
                    description = "Benda peninggalan keluarga Larasati yang berisi gulungan mikrokartografi lokasi brankas bawah tanah museum."
                )
            ),
            timeline = listOf(
                TimelineEvent(
                    id = "tl-1",
                    phase = "Prolog",
                    title = "Lenyapnya Larasati",
                    description = "Maya menghilang dari ruang arsip museum kota secara misterius.",
                    orderIndex = 1
                ),
                TimelineEvent(
                    id = "tl-2",
                    phase = "Bab 1",
                    title = "Pertemuan di Ujung Dermaga",
                    description = "Raditya mengonfrontasi Hendra untuk menuntut pengembalian bukti artefak.",
                    orderIndex = 2
                )
            ),
            styleNotes = "Pertahankan suasana noir yang atmosferik, kalimat bernas dan sensorik, hindari penjabaran eksposisi yang bertele-tele."
        )

        _novelProjects.value = listOf(sampleProject)
        _currentActiveProject.value = sampleProject
    }

    suspend fun getDocumentById(id: String): OfficeDocument? {
        return dao.getDocumentById(id)?.toModel()
    }

    suspend fun saveDocument(doc: OfficeDocument) {
        val words = doc.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val chars = doc.content.length
        val updated = doc.copy(
            lastModified = System.currentTimeMillis(),
            sizeBytes = (doc.content.toByteArray().size + 1024).toLong(),
            wordCount = words,
            characterCount = chars,
            version = doc.version + 1
        )
        dao.insertOrUpdate(DocumentEntity.fromModel(updated))
    }

    suspend fun createNewDocument(
        title: String,
        type: DocumentType,
        folderName: String = "Documents"
    ): OfficeDocument {
        val initialContent = when (type) {
            DocumentType.WRITER -> "JUDUL DOKUMEN BARU\n\nKetik naskah atau draf novel Anda di sini..."
            DocumentType.SHEETS -> "Kolom A,Kolom B,Kolom C,Total\nItem 1,100,200,=SUM(B2:C2)\nItem 2,300,400,=SUM(B3:C3)"
            DocumentType.SLIDES -> "SLIDE 1|TITLE|Judul Presentasi Baru|Subjudul Presentasi\nSLIDE 2|CONTENT|Agenda Utama|• Poin pertama\n• Poin kedua"
            DocumentType.PDF -> "DOKUMEN PDF BARU\n\nDokumen ini siap dibaca, diulas, dan diberi anotasi..."
            DocumentType.TXT -> "Catatan Teks Baru..."
        }

        val doc = OfficeDocument(
            id = "doc-${UUID.randomUUID()}",
            title = title,
            type = type,
            content = initialContent,
            lastModified = System.currentTimeMillis(),
            sizeBytes = 2048L,
            isFavorite = false,
            isTrash = false,
            folderName = folderName,
            wordCount = initialContent.split(Regex("\\s+")).size,
            characterCount = initialContent.length,
            version = 1
        )
        dao.insertOrUpdate(DocumentEntity.fromModel(doc))
        return doc
    }

    suspend fun toggleFavorite(id: String) {
        val doc = dao.getDocumentById(id) ?: return
        dao.insertOrUpdate(doc.copy(isFavorite = !doc.isFavorite))
    }

    suspend fun moveToTrash(id: String) {
        val doc = dao.getDocumentById(id) ?: return
        dao.insertOrUpdate(doc.copy(isTrash = true))
    }

    suspend fun restoreFromTrash(id: String) {
        val doc = dao.getDocumentById(id) ?: return
        dao.insertOrUpdate(doc.copy(isTrash = false))
    }

    suspend fun duplicateDocument(id: String): OfficeDocument? {
        val original = dao.getDocumentById(id)?.toModel() ?: return null
        val copy = original.copy(
            id = "doc-${UUID.randomUUID()}",
            title = "${original.title} (Salinan)",
            lastModified = System.currentTimeMillis(),
            version = 1
        )
        dao.insertOrUpdate(DocumentEntity.fromModel(copy))
        return copy
    }

    suspend fun renameDocument(id: String, newTitle: String) {
        val doc = dao.getDocumentById(id) ?: return
        dao.insertOrUpdate(doc.copy(title = newTitle.trim()))
    }

    suspend fun deletePermanently(id: String) {
        val doc = dao.getDocumentById(id) ?: return
        dao.delete(doc)
    }

    suspend fun emptyTrash() {
        dao.emptyTrash()
    }

    suspend fun importExternalDocument(fileName: String, content: String): OfficeDocument {
        val ext = fileName.substringAfterLast('.', "txt")
        val type = DocumentType.fromExtension(ext)
        val cleanTitle = fileName.substringBeforeLast('.')

        val doc = OfficeDocument(
            id = "doc-imported-${UUID.randomUUID()}",
            title = cleanTitle,
            type = type,
            content = content,
            lastModified = System.currentTimeMillis(),
            sizeBytes = (content.toByteArray().size + 1024).toLong(),
            isFavorite = false,
            isTrash = false,
            folderName = "Imported",
            wordCount = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size,
            characterCount = content.length,
            version = 1
        )
        dao.insertOrUpdate(DocumentEntity.fromModel(doc))
        return doc
    }

    fun selectNovelProject(project: NovelProject) {
        _currentActiveProject.value = project
    }

    fun updateNovelProject(updated: NovelProject) {
        val list = _novelProjects.value.toMutableList()
        val index = list.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            list[index] = updated
        } else {
            list.add(updated)
        }
        _novelProjects.value = list
        if (_currentActiveProject.value?.id == updated.id) {
            _currentActiveProject.value = updated
        }
    }
}
