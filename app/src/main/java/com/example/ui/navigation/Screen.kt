package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Files : Screen("files")
    data object Tools : Screen("tools")
    data object NaraHub : Screen("nara_hub")
    data object Settings : Screen("settings")

    data class Writer(val docId: String) : Screen("writer/$docId") {
        companion object {
            const val ROUTE = "writer/{docId}"
            fun createRoute(docId: String) = "writer/$docId"
        }
    }

    data class Sheets(val docId: String) : Screen("sheets/$docId") {
        companion object {
            const val ROUTE = "sheets/{docId}"
            fun createRoute(docId: String) = "sheets/$docId"
        }
    }

    data class Slides(val docId: String) : Screen("slides/$docId") {
        companion object {
            const val ROUTE = "slides/{docId}"
            fun createRoute(docId: String) = "slides/$docId"
        }
    }

    data class Pdf(val docId: String) : Screen("pdf/{docId}") {
        companion object {
            const val ROUTE = "pdf/{docId}"
            fun createRoute(docId: String) = "pdf/$docId"
        }
    }

    // Tools sub-screens
    data object ScannerTool : Screen("tool_scanner")
    data object OcrTool : Screen("tool_ocr")
    data object TextDiffTool : Screen("tool_text_diff")
    data object ConverterTool : Screen("tool_converter")
    data object WordCounterTool : Screen("tool_word_counter")
    data object QrTool : Screen("tool_qr")
    data object VoiceNotesTool : Screen("tool_voice_notes")
}
