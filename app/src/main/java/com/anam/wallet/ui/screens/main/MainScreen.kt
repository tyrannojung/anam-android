package com.anam.wallet.ui.screens.main

import androidx.compose.runtime.*
import com.anam.wallet.ui.screens.main.components.EmptyModuleScreen
import com.anam.wallet.ui.screens.main.components.ModuleListScreen

@Composable
fun MainScreen() {
    // 모듈이 설치되어 있는지 체크하는 상태 (실제로는 ViewModel에서 관리해야 함)
    val hasModules by remember { mutableStateOf(true) }
    
    if (hasModules) {
        // 모듈이 있을 때의 화면
        ModuleListScreen()
    } else {
        // 모듈이 없을 때의 온보딩 화면
        EmptyModuleScreen()
    }
}