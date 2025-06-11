package com.anam.wallet.presentation.ui.screens.main

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.anam.wallet.presentation.ui.screens.main.components.EmptyModuleScreen
import com.anam.wallet.presentation.ui.screens.main.components.ModuleListScreen

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.miniApps.isNotEmpty()) {
        // 모듈이 있을 때의 화면
        ModuleListScreen(
            uiState = uiState,
            onActivateBlockchain = viewModel::activateBlockchain,
            onRefresh = viewModel::loadMiniApps
        )
    } else if (!uiState.isLoading) {
        // 모듈이 없을 때의 온보딩 화면
        EmptyModuleScreen()
    }
}