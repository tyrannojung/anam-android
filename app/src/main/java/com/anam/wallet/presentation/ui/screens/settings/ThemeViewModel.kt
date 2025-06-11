package com.anam.wallet.presentation.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.anam.wallet.presentation.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
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
        android.util.Log.d("ThemeViewModel", "Setting theme mode to: $mode")
        _themeMode.value = mode
        saveThemeMode(mode)
    }
    
    private fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(THEME_KEY, mode.name).apply()
    }
}