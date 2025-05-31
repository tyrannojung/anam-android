package com.anam.wallet.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anam.wallet.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(
    private val context: Context
) : ViewModel() {
    
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val THEME_KEY = "theme_mode"
    
    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private fun loadThemeMode(): ThemeMode {
        val savedMode = prefs.getString(THEME_KEY, ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(savedMode ?: ThemeMode.SYSTEM.name)
    }
    
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        saveThemeMode(mode)
    }
    
    private fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(THEME_KEY, mode.name).apply()
    }
    
    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ThemeViewModel(context)
            }
        }
    }
}