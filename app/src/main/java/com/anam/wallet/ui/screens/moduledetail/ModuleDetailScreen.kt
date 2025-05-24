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
import com.anam.wallet.core.IModuleUI
import com.anam.wallet.core.IPaymentModule

private const val TAG = "ModuleDetailScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    moduleId: String, 
    moduleManager: SimpleModuleManager,
    onBackClick: () -> Unit
) {
    // Get module from SimpleModuleManager
    val module = moduleManager.getModule(moduleId)
    
    // Add logging to debug module loading issues
    if (module == null) {
        Log.e(TAG, "모듈을 찾을 수 없음: moduleId=$moduleId")
        Log.d(TAG, "현재 로드된 모듈 목록: ${moduleManager.getLoadedModuleIds()}")
    } else {
        Log.d(TAG, "모듈 로드 성공: moduleId=$moduleId")
        if (module is IPaymentModule) {
            Log.d(TAG, "모듈 정보: 이름=${module.getName()}, 심볼=${module.getSymbol()}")
        }
        Log.d(TAG, "모듈 타입: ${module::class.java.name}")
        Log.d(TAG, "모듈이 IModuleUI 구현 여부: ${module is IModuleUI}")
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (module == null) {
            // Module not found case
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("모듈을 찾을 수 없습니다")
                    Text("모듈 ID: $moduleId")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("뒤로 가기")
                    }
                }
            }
        } else if (module is IModuleUI) {
            // Module implements UI interface
            Log.d(TAG, "IModuleUI 인터페이스 구현 모듈 UI 표시 중")
            
            // Context data to pass to the module
            val uiContext = remember {
                mutableMapOf<String, Any>(
                    "onBack" to onBackClick
                )
            }
            
            // Render module's UI
            (module as IModuleUI).ModuleDetailScreen(uiContext)
        } else {
            // Fallback UI for modules that don't implement IModuleUI
            Log.d(TAG, "기본 UI 폴백 사용 중 (IModuleUI 미구현)")
            FallbackModuleDetailUI(module, onBackClick)
        }
    }
}

// Fallback UI for modules that don't implement IModuleUI
@Composable
private fun FallbackModuleDetailUI(module: Any, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (module is IPaymentModule) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = module.getName(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "심볼: ${module.getSymbol()}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "모듈 정보",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(module.getModuleInfo())
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "계정 정보",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(module.getAccounts())
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "네트워크 정보",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(module.getNetworkInfo())
                }
            }
        } else {
            Log.w(TAG, "알 수 없는 모듈 타입: ${module::class.java.name}")
            Text("모듈 타입을 인식할 수 없습니다")
        }
    }
}