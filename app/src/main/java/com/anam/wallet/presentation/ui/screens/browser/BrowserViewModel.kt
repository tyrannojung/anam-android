package com.anam.wallet.presentation.ui.screens.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowserUiState(
    val isLoading: Boolean = false,
    val currentUrl: String = "https://www.google.com",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val bookmarks: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class BrowserViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()
    
    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(currentUrl = url)
    }
    
    fun updateLoadingState(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }
    
    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.value = _uiState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }
    
    fun addBookmark(url: String) {
        viewModelScope.launch {
            val currentBookmarks = _uiState.value.bookmarks.toMutableList()
            if (!currentBookmarks.contains(url)) {
                currentBookmarks.add(url)
                _uiState.value = _uiState.value.copy(bookmarks = currentBookmarks)
                // TODO: Save to preferences
            }
        }
    }
    
    fun removeBookmark(url: String) {
        viewModelScope.launch {
            val currentBookmarks = _uiState.value.bookmarks.toMutableList()
            currentBookmarks.remove(url)
            _uiState.value = _uiState.value.copy(bookmarks = currentBookmarks)
            // TODO: Save to preferences
        }
    }
}