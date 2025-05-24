package com.anam.wallet.ui.screens.moduledetail

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anam.wallet.SimpleModuleManager
import com.anam.wallet.constants.ModuleConstants
import com.anam.wallet.ui.components.FrontModuleSurface

private const val TAG = "ModuleDetailScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    moduleId: String, 
    moduleManager: SimpleModuleManager,
    onBackClick: () -> Unit
) {
    // 다운로드된 모듈 확인
    val isDownloaded = moduleManager.isModuleDownloaded(moduleId)
    
    // 로깅
    if (isDownloaded) {
        Log.d(TAG, "프론트 모듈 발견: moduleId=$moduleId")
        Log.d(TAG, "APK 경로: ${moduleManager.getModuleApkPath(moduleId)}")
    } else {
        Log.e(TAG, "다운로드된 모듈을 찾을 수 없음: moduleId=$moduleId")
        Log.d(TAG, "다운로드된 모듈 목록: ${moduleManager.getDownloadedModuleIds()}")
    }
    
    if (isDownloaded) {
        // 프론트 모듈 실행 (별도 프로세스)
        Log.d(TAG, "프론트 모듈 별도 프로세스에서 실행 중")
        
        FrontModuleSurface(
            moduleId = moduleId,
            apkPath = moduleManager.getModuleApkPath(moduleId) ?: "",
            className = ModuleConstants.FRONT_MODULE_IMPLEMENTATION_CLASS,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // 모듈을 찾을 수 없는 경우
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("프론트 모듈을 찾을 수 없습니다")
                Text("모듈 ID: $moduleId")
                Spacer(modifier = Modifier.height(16.dp))
                Text("다운로드된 모듈을 확인해주세요")
            }
        }
    }
}