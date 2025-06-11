package com.anam.wallet.presentation.ui.screens.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LocaleViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val prefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
    private val LOCALE_KEY = "app_locale"
    
    private val _currentLocale = MutableStateFlow(loadCurrentLocale())
    val currentLocale: StateFlow<SupportedLocale> = _currentLocale.asStateFlow()
    
    private fun loadCurrentLocale(): SupportedLocale {
        val savedLocale = prefs.getString(LOCALE_KEY, null)
        return if (savedLocale != null) {
            SupportedLocale.fromCode(savedLocale)
        } else {
            // 시스템 언어 확인
            val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
            
            // 시스템 언어가 한국어면 한국어, 아니면 영어
            if (systemLocale.language == "ko") {
                SupportedLocale.KOREAN
            } else {
                SupportedLocale.ENGLISH
            }
        }
    }
    
    fun setLocale(locale: SupportedLocale) {
        _currentLocale.value = locale
        saveLocale(locale)
        applyLocale(locale)
    }
    
    private fun saveLocale(locale: SupportedLocale) {
        prefs.edit().putString(LOCALE_KEY, locale.code).apply()
    }
    
    private fun applyLocale(locale: SupportedLocale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상: LocaleManager 사용
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(locale.code)
        } else {
            // Android 12 이하: AppCompatDelegate 사용
            val appLocale = LocaleListCompat.forLanguageTags(locale.code)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}

enum class SupportedLocale(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    KOREAN("ko", "Korean", "한국어");
    
    companion object {
        fun fromCode(code: String): SupportedLocale {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}