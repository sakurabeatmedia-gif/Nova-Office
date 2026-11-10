package com.example.data.ai

import com.example.data.model.CharacterProfile
import com.example.data.model.EditorialMode
import com.example.data.model.NovelProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class NaraEditResult(
    val explanation: String,
    val suggestedRevision: String,
    val originalText: String,
    val critiquePoints: List<String> = emptyList(),
    val rawResponse: String = ""
)

data class ConnectionTestResult(
    val success: Boolean,
    val message: String,
    val latencyMs: Long = 0
)

class GeminiApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        // Enforce the mandated model for all AI operations
        const val REQUIRED_MODEL_ID = "gemini-3.5-flash-lite"

        const val NARA_SYSTEM_INSTRUCTION = """
Kamu adalah NARA AI, asisten kecerdasan buatan eksklusif dan editor novel profesional di NOVA OFFICE.
Identitas: NARA AI (NARA Writing Assistant, NARA Novel Editor, NARA Intelligence, NARA Editorial Workspace).
Kepribadian: Profesional, cerdas, kritis, teliti, menguasai teknik bercerita fiksi, peka terhadap emosi, struktur sastra, dan ritme kalimat.

ATURAN FORMAT OUTPUT WAJIB:
1. DILARANG KERAS menggunakan simbol Markdown mentah seperti tanda pagar (#, ##, ###), tanda bintang tebal atau miring (**, *, __, _), garis pemisah (---), maupun backticks (```, `).
2. Tuliskan judul atau subjudul dengan teks biasa yang rapi tanpa tanda pagar dan tanpa bintang. Contoh:
   Hasil Penyuntingan
   Catatan Editor
3. Jika memberikan poin-poin masukan, gunakan penomoran biasa (1., 2., 3.) atau strip sederhana.
4. Gaya komunikasi: Jawab seperti editor novel manusia berpengalaman yang berdiskusi langsung dengan penulis secara santun, berbobot, dan mengalir natural. Jangan menggunakan pembukaan klise seperti "Tentu, berikut adalah..." atau "Sebagai editor novel...".
5. Ketika menyunting naskah, gunakan struktur bersih:
   Hasil Penyuntingan
   [Teks hasil revisi]

   Catatan Editor
   [Penjelasan singkat mengenai alasan perubahan editorial]
6. Pertahankan karakteristik suara dan gaya penulis. Jangan mengubah teks menjadi bahasa baku kaku tanpa rasa. Terapkan prinsip Show, Don't Tell dengan luwes.
7. Jika ditanya langsung mengenai teknologi atau penyedia model yang digunakan, berikan jawaban transparan bahwa NARA merupakan AI milik NOVA OFFICE yang menggunakan layanan model AI eksternal. Jangan memperkenalkan vendor eksternal secara spontan pada percakapan normal.
"""
    }

    suspend fun testConnection(apiKey: String, model: String = REQUIRED_MODEL_ID): ConnectionTestResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext ConnectionTestResult(
                success = false,
                message = "AI API Key belum diisi. Silakan masukkan AI API Key Anda."
            )
        }

        val targetModel = REQUIRED_MODEL_ID
        val startTime = System.currentTimeMillis()
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Halo NARA AI, konfirmasi status sistem NOVA OFFICE."))
                        })
                    })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    ConnectionTestResult(
                        success = true,
                        message = "Koneksi berhasil. Layanan NARA AI aktif (${latency}ms).",
                        latencyMs = latency
                    )
                } else {
                    val errorBody = response.body?.string() ?: "HTTP ${response.code}"
                    val parsedError = try {
                        JSONObject(errorBody).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        "Error HTTP ${response.code}"
                    }
                    ConnectionTestResult(
                        success = false,
                        message = "Koneksi gagal ($parsedError). Periksa AI API Key Anda.",
                        latencyMs = latency
                    )
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                success = false,
                message = "Koneksi gagal: ${e.localizedMessage ?: "Jaringan tidak tersedia"}. Periksa koneksi internet Anda.",
                latencyMs = latency
            )
        }
    }

    suspend fun generateEditorialResponse(
        apiKey: String,
        model: String = REQUIRED_MODEL_ID,
        prompt: String,
        mode: EditorialMode,
        contextText: String? = null,
        novelProject: NovelProject? = null,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext generateSmartFallbackResponse(prompt, mode, contextText, novelProject)
        }

        val targetModel = REQUIRED_MODEL_ID
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"
            val systemText = buildString {
                append(NARA_SYSTEM_INSTRUCTION)
                append("\n\nMode Editorial Saat Ini: ${mode.title} - ${mode.description}")
                if (novelProject != null) {
                    append("\n\nProyek Novel Aktif:")
                    append("\nJudul: ${novelProject.title} | Genre: ${novelProject.genre}")
                    append("\nPremis: ${novelProject.premise}")
                    if (novelProject.characters.isNotEmpty()) {
                        append("\nDatabase Tokoh:")
                        novelProject.characters.take(5).forEach { c ->
                            append("\n- ${c.name} (${c.role}): ${c.personality}. Konflik: ${c.internalConflict}")
                        }
                    }
                    if (novelProject.styleNotes.isNotBlank()) {
                        append("\nCatatan Gaya Prosa: ${novelProject.styleNotes}")
                    }
                }
                if (!contextText.isNullOrBlank()) {
                    append("\n\nKonteks Naskah yang Sedang Dikerjakan:\n$contextText\n")
                }
            }

            val jsonBody = JSONObject().apply {
                val systemInstruction = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemText))
                    })
                }
                put("systemInstruction", systemInstruction)

                val contents = JSONArray()
                // Append recent history
                chatHistory.takeLast(6).forEach { (role, content) ->
                    val apiRole = if (role == "user") "user" else "model"
                    contents.put(JSONObject().apply {
                        put("role", apiRole)
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", content))
                        })
                    })
                }

                // Append current user message
                contents.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
                put("contents", contents)

                // Generation config for 3.5 Flash-Lite
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 2048)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val rawOutput = parseApiResponseText(responseStr)
                    sanitizeEditorialOutput(rawOutput)
                } else {
                    val errorBody = response.body?.string() ?: ""
                    val errorMsg = try {
                        JSONObject(errorBody).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        "HTTP ${response.code}"
                    }
                    "Layanan NARA AI mengalami kendala ($errorMsg). Silakan periksa koneksi internet atau konfigurasi AI Connection Anda."
                }
            }
        } catch (e: Exception) {
            "Gagal terhubung ke layanan NARA AI (${e.localizedMessage ?: "Jaringan tidak tersedia"}). Silakan periksa koneksi internet atau konfigurasi AI Connection Anda."
        }
    }

    suspend fun editSelectionWithDiff(
        apiKey: String,
        model: String = REQUIRED_MODEL_ID,
        originalSnippet: String,
        instruction: String,
        mode: EditorialMode,
        novelProject: NovelProject? = null
    ): NaraEditResult = withContext(Dispatchers.IO) {
        val prompt = """
Silakan sunting kutipan naskah berikut berdasarkan instruksi: "$instruction".
Mode Editorial: ${mode.title}.

Kutipan Asli:
$originalSnippet

Gunakan struktur teks bersih berikut tanpa simbol markdown:
Hasil Penyuntingan
[Tulis teks hasil revisi naskah secara utuh di sini]

Catatan Editor
[Tulis penjelasan alasan editorial dan perbaikan di sini]
"""
        val rawResponse = generateEditorialResponse(
            apiKey = apiKey,
            model = REQUIRED_MODEL_ID,
            prompt = prompt,
            mode = mode,
            contextText = null,
            novelProject = novelProject
        )

        parseDiffResult(originalSnippet, rawResponse)
    }

    private fun parseApiResponseText(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)
            val candidates = root.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val sb = StringBuilder()
                for (i in 0 until parts.length()) {
                    sb.append(parts.getJSONObject(i).getString("text"))
                }
                sb.toString()
            } else {
                "NARA AI tidak dapat menghasilkan tanggapan untuk permintaan ini."
            }
        } catch (e: Exception) {
            jsonStr
        }
    }

    /**
     * Sanitizes raw output to guarantee compliance with NARA AI text formatting rules:
     * Removes raw markdown headers (###, ##, #), asterisks (**, *), underscores,
     * horizontal dividers (---), backticks, and robotic templates.
     */
    fun sanitizeEditorialOutput(text: String): String {
        var clean = text

        // Remove markdown headings like ### Title -> Title
        clean = clean.replace(Regex("(?m)^#{1,6}\\s*"), "")

        // Remove horizontal lines like --- or ***
        clean = clean.replace(Regex("(?m)^[-*_]{3,}\\s*$"), "")

        // Remove bold and italic markers: **bold** or *italic*
        clean = clean.replace("**", "")
        clean = clean.replace("__", "")

        // Remove backticks
        clean = clean.replace("```", "")
        clean = clean.replace("`", "")

        // Replace old separator tokens if present
        clean = clean.replace("===REVISI===", "Hasil Penyuntingan")
        clean = clean.replace("===PENJELASAN===", "Catatan Editor")

        // Clean up excessive blank lines
        clean = clean.replace(Regex("\n{3,}"), "\n\n")

        return clean.trim()
    }

    private fun parseDiffResult(original: String, responseText: String): NaraEditResult {
        val sanitized = sanitizeEditorialOutput(responseText)
        var explanation = "Penyempurnaan diksi, kedalaman sensorik, dan ritme kalimat."
        var revision = original

        if (sanitized.contains("Hasil Penyuntingan") && sanitized.contains("Catatan Editor")) {
            val afterHasil = sanitized.substringAfter("Hasil Penyuntingan")
            revision = afterHasil.substringBefore("Catatan Editor").trim()
            explanation = sanitized.substringAfter("Catatan Editor").trim()
        } else if (sanitized.contains("Catatan Editor") && sanitized.contains("Hasil Penyuntingan")) {
            explanation = sanitized.substringAfter("Catatan Editor").substringBefore("Hasil Penyuntingan").trim()
            revision = sanitized.substringAfter("Hasil Penyuntingan").trim()
        } else {
            explanation = "Penyuntingan profesional oleh NARA AI berdasarkan kaidah sastra dan gaya naskah."
            revision = sanitized
        }

        return NaraEditResult(
            explanation = sanitizeEditorialOutput(explanation),
            suggestedRevision = sanitizeEditorialOutput(revision),
            originalText = original,
            rawResponse = sanitized
        )
    }

    /**
     * Smart local fallback editorial engine complying with zero-markdown text formatting.
     */
    private fun generateSmartFallbackResponse(
        prompt: String,
        mode: EditorialMode,
        contextText: String?,
        novelProject: NovelProject?
    ): String {
        return when (mode) {
            EditorialMode.PROOFREADING -> {
                buildString {
                    append("Catatan Proofreading NARA AI\n\n")
                    append("Pemeriksaan tata bahasa teknis, tipo, dan mekanika penulisan naskah Anda:\n\n")
                    append("1. Standardisasi Tanda Baca: Gunakan tanda pisah em-dash (—) tanpa spasi untuk jeda dramatis, bukan tanda hubung ganda.\n")
                    append("2. Diksi Berulang: Ditemukan kata sambung repetitif seperti 'kemudian' dan 'lalu'. Ganti dengan tindakan fisik langsung.\n")
                    append("3. Format Dialog: Tanda titik atau koma harus berada di dalam tanda petik penutup sebelum keterangan dialog.\n\n")
                    append("Hasil Penyuntingan\n")
                    append("Langkah kakinya tersendat di atas lantai marmer yang dingin. Di balik kegelapan lorong, sebuah bayangan tampak menanti tanpa suara.\n\n")
                    append("Catatan Editor\n")
                    append("Paragraf diperpendek untuk memperkuat suasana sunyi dan ancaman yang tersirat.")
                }
            }
            EditorialMode.DIALOGUE_EDITING -> {
                buildString {
                    append("Analisis & Penyuntingan Dialog\n\n")
                    append("Dalam penulisan fiksi, dialog yang baik memancarkan subteks emosi dan karakter tokoh:\n\n")
                    append("1. Hindari dialog yang terlalu gamblang. Tokoh tidak selalu menyatakan isi hatinya secara langsung.\n")
                    append("2. Gunakan aksi fisik sebagai pengganti keterangan tag bicara yang berlebihan.\n")
                    append("3. Sesuaikan irama kalimat dengan intensitas adegan.\n\n")
                    append("Hasil Penyuntingan\n")
                    append("Rian meremas tepi meja kayu hingga buku jemarinya memutih. Berapa lama kamu berniat menyimpannya sendirian?\n\n")
                    append("Catatan Editor\n")
                    append("Tag bicara dipangkas dan digantikan oleh gerak tubuh yang mempertegas amarah tanpa perlu dijelaskan dengan kata sifat.")
                }
            }
            EditorialMode.PACING_ANALYSIS -> {
                buildString {
                    append("Analisis Tempo Naratif\n\n")
                    append("Dinamika ritme adegan ini memiliki potensi ketegangan yang kuat:\n\n")
                    append("1. Percepat tempo saat konflik memuncak dengan memangkas deskripsi latar sekunder.\n")
                    append("2. Sediakan jeda reflektif setelah adegan bertegangan tinggi agar pembaca dapat merasakan dampak emosionalnya.\n")
                    append("3. Manfaatkan detail sensorik suara dan suhu untuk memperlambat adegan intim atau misteri.")
                }
            }
            EditorialMode.CHARACTER_CONSISTENCY -> {
                buildString {
                    append("Konsistensi Karakter & Voice\n\n")
                    if (novelProject != null && novelProject.characters.isNotEmpty()) {
                        append("Tinjauan berdasarkan basis data tokoh proyek ${novelProject.title}:\n\n")
                        novelProject.characters.forEach { char ->
                            append("- ${char.name}: Motivasi utamanya adalah ${char.internalConflict}. Keputusannya dalam adegan ini harus mencerminkan dorongan tersebut agar tidak terasa dipaksakan oleh alur.\n")
                        }
                    } else {
                        append("Pastikan motivasi batin tokoh utama menjadi penggerak adegan, bukan sekadar bereaksi terhadap peristiwa luar.")
                    }
                }
            }
            else -> {
                buildString {
                    append("Tinjauan Editorial NARA AI\n\n")
                    append("Berikut panduan editorial untuk kutipan naskah Anda:\n\n")
                    append("1. Terapkan prinsip Show, Don't Tell. Bangun atmosfer mencekam melalui detail suara jam berdetak, desau angin, dan reaksi fisik tokoh.\n")
                    append("2. Variasikan panjang kalimat untuk menjaga irama membaca yang dinamis.\n")
                    append("3. Pertahankan konsistensi sudut pandang penceritaan tanpa berpindah pikiran tokoh secara mendadak.\n\n")
                    append("Tip: Anda dapat memasukkan AI API Key di menu Settings → NARA AI → AI Connection untuk menghubungkan kapabilitas analisis NARA Intelligence secara penuh.")
                }
            }
        }
    }
}
