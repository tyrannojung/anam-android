package com.anam.wallet.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anam.wallet.core.IFrontModuleUI
import com.anam.wallet.core.FrontModuleContext
import com.anam.wallet.service.SimpleMainAppService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "FrontModuleSurface"

/**
 * 외부 APK 모듈을 직접 로드하여 Compose UI를 렌더링하는 컴포넌트
 * 단순화된 버전 - 별도 프로세스나 SurfaceControlViewHost 없이 직접 렌더링
 */
@Composable
fun FrontModuleSurface(
    moduleId: String,
    apkPath: String,
    className: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var moduleInstance by remember { mutableStateOf<IFrontModuleUI?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 인증 팝업 상태
    var showAuthSheet by remember { mutableStateOf(false) }
    var authRequesterName by remember { mutableStateOf("") }
    var authCallback by remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }
    
    // 백그라운드에서 모듈 로드
    LaunchedEffect(moduleId) {
        try {
            Log.d(TAG, "Loading front module: $className from $apkPath")
            
            val loadedModule = withContext(Dispatchers.IO) {
                loadModuleFromApk(apkPath, className)
            }
            
            if (loadedModule is IFrontModuleUI) {
                // SimpleMainAppService 인스턴스 생성 및 설정
                val mainAppService = SimpleMainAppService(context).apply {
                    onAuthRequest = { requesterName, callback ->
                        authRequesterName = requesterName
                        authCallback = callback
                        showAuthSheet = true
                    }
                }
                loadedModule.setMainAppService(mainAppService)
                loadedModule.onModuleStart()
                
                moduleInstance = loadedModule
                Log.d(TAG, "Front module loaded successfully")
            } else {
                errorMessage = "Module does not implement IFrontModuleUI"
                Log.e(TAG, errorMessage!!)
            }
            
        } catch (e: Exception) {
            errorMessage = "Failed to load module: ${e.message}"
            Log.e(TAG, errorMessage!!, e)
        } finally {
            isLoading = false
        }
    }
    
    // 정리
    DisposableEffect(Unit) {
        onDispose {
            try {
                moduleInstance?.onModuleStop()
                moduleInstance?.onModuleDestroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error during cleanup", e)
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("프론트 모듈 로딩 중...")
                }
            }
            
            errorMessage != null -> {
                Text(
                    text = "오류: $errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            moduleInstance != null -> {
                // 직접 Compose UI 렌더링
                moduleInstance!!.FrontModuleScreen(
                    FrontModuleContext(
                        moduleId = moduleId,
                        parameters = emptyMap()
                    )
                )
            }
        }
    }
    
    // 인증 팝업 표시
    if (showAuthSheet) {
        AuthBottomSheet(
            requesterName = authRequesterName,
            onConfirm = {
                authCallback?.complete(true)
                showAuthSheet = false
            },
            onDismiss = {
                authCallback?.complete(false)
                showAuthSheet = false
            }
        )
    }
}

/**
 * APK 파일에서 모듈을 동적으로 로드
 */
private fun loadModuleFromApk(apkPath: String, className: String): Any {
    Log.d(TAG, "Loading module from APK: $apkPath")
    Log.d(TAG, "Class name: $className")
    
    // APK 파일 유효성 검사
    val apkFile = java.io.File(apkPath)
    if (!apkFile.exists() || !apkFile.canRead()) {
        throw IllegalArgumentException("APK 파일이 존재하지 않거나 읽을 수 없음: $apkPath")
    }
    
    // DexClassLoader로 외부 APK 로드
    val classLoader = dalvik.system.DexClassLoader(
        apkPath,
        "/data/data/com.anam.wallet/cache", // 고정된 캐시 경로 사용
        null,
        Thread.currentThread().contextClassLoader
    )
    
    // 외부 APK에서 클래스 로드
    val moduleClass = classLoader.loadClass(className)
    val moduleInstance = moduleClass.getDeclaredConstructor().newInstance()
    
    Log.d(TAG, "Module loaded successfully: ${moduleInstance::class.java.name}")
    return moduleInstance
}