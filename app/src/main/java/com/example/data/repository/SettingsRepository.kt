package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.ai.GeminiApiService
import com.example.data.model.EditorialMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_office_settings", Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(getStoredApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    // Mandated single model: Gemini 3.5 Flash Lite
    private val _model = MutableStateFlow(GeminiApiService.REQUIRED_MODEL_ID)
    val model: StateFlow<String> = _model.asStateFlow()

    private val _theme = MutableStateFlow(
        prefs.getString(KEY_THEME, "dark") ?: "dark"
    )
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _language = MutableStateFlow(
        prefs.getString(KEY_LANG, "id") ?: "id"
    )
    val language: StateFlow<String> = _language.asStateFlow()

    private val _autoSaveSec = MutableStateFlow(
        prefs.getInt(KEY_AUTOSAVE, 15)
    )
    val autoSaveSec: StateFlow<Int> = _autoSaveSec.asStateFlow()

    private val _defaultMode = MutableStateFlow(
        try {
            EditorialMode.valueOf(prefs.getString(KEY_MODE, EditorialMode.LINE_EDITING.name) ?: EditorialMode.LINE_EDITING.name)
        } catch (e: Exception) {
            EditorialMode.LINE_EDITING
        }
    )
    val defaultMode: StateFlow<EditorialMode> = _defaultMode.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING, true)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private fun getStoredApiKey(): String {
        val customKey = prefs.getString(KEY_API_KEY, "") ?: ""
        if (customKey.isNotBlank()) return customKey
        val legacyKey = prefs.getString("key_gemini_api_key", "") ?: ""
        if (legacyKey.isNotBlank()) return legacyKey
        // Fallback to BuildConfig if provided via environment
        return try {
            val buildKey = BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
            buildKey ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getMaskedApiKey(): String {
        val current = _apiKey.value
        if (current.isBlank()) return ""
        if (current.length <= 8) return "••••••••"
        return "••••••••••••${current.takeLast(4)}"
    }

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _apiKey.value = key.trim()
    }

    fun setModel(model: String) {
        // Enforce the mandated model
        val enforced = GeminiApiService.REQUIRED_MODEL_ID
        prefs.edit().putString(KEY_MODEL, enforced).apply()
        _model.value = enforced
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _theme.value = theme
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANG, lang).apply()
        _language.value = lang
    }

    fun setAutoSaveSec(sec: Int) {
        prefs.edit().putInt(KEY_AUTOSAVE, sec).apply()
        _autoSaveSec.value = sec
    }

    fun setDefaultMode(mode: EditorialMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _defaultMode.value = mode
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, completed).apply()
        _onboardingCompleted.value = completed
    }

    companion object {
        private const val KEY_API_KEY = "key_ai_api_key"
        private const val KEY_MODEL = "key_ai_model"
        private const val KEY_THEME = "key_theme_mode"
        private const val KEY_LANG = "key_language"
        private const val KEY_AUTOSAVE = "key_autosave_seconds"
        private const val KEY_MODE = "key_editorial_mode"
        private const val KEY_ONBOARDING = "key_onboarding_completed"

        const val DISPLAY_MODEL_NAME = "NARA Intelligence Core (3.5 Flash-Lite)"
    }
}
