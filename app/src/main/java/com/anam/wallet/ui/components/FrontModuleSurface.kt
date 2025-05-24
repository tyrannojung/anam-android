package com.anam.wallet.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
import androidx.compose.ui.viewinterop.AndroidView
import com.anam.wallet.IFrontModuleService
import com.anam.wallet.IMainAppService
import com.anam.wallet.service.FrontModuleService
import com.anam.wallet.service.MainAppService

private const val TAG = "FrontModuleSurface"

/**
 * 프론트 모듈을 별도 프로세스에서 실행하고 
 * Surface를 통해 UI를 렌더링하는 컴포넌트
 */
@Composable
fun FrontModuleSurface(
    moduleId: String,
    apkPath: String,
    className: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var frontModuleService by remember { mutableStateOf<IFrontModuleService?>(null) }
    var mainAppService by remember { mutableStateOf<IMainAppService?>(null) }
    
    // 서비스 연결 관리
    val frontServiceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d(TAG, "FrontModuleService connected")
                frontModuleService = IFrontModuleService.Stub.asInterface(service)
                
                // 메인앱 서비스가 준비되면 프론트 모듈 로드
                mainAppService?.let { mainService ->
                    frontModuleService?.setMainAppService(mainService)
                    frontModuleService?.loadModule(apkPath, className, moduleId)
                    isLoading = false
                }
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "FrontModuleService disconnected")
                frontModuleService = null
                errorMessage = "서비스 연결이 끊어졌습니다"
            }
        }
    }
    
    val mainServiceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d(TAG, "MainAppService connected")
                mainAppService = IMainAppService.Stub.asInterface(service)
                
                // 프론트 모듈 서비스가 준비되면 설정
                frontModuleService?.let { frontService ->
                    frontService.setMainAppService(mainAppService!!)
                    frontService.loadModule(apkPath, className, moduleId)
                    isLoading = false
                }
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "MainAppService disconnected")
                mainAppService = null
            }
        }
    }
    
    // 서비스 연결 시작
    LaunchedEffect(moduleId) {
        try {
            // 메인앱 서비스 연결
            val mainIntent = Intent(context, MainAppService::class.java)
            context.bindService(mainIntent, mainServiceConnection, Context.BIND_AUTO_CREATE)
            
            // 프론트 모듈 서비스 연결
            val frontIntent = Intent(context, FrontModuleService::class.java)
            context.bindService(frontIntent, frontServiceConnection, Context.BIND_AUTO_CREATE)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind services", e)
            errorMessage = "서비스 연결 실패: ${e.message}"
            isLoading = false
        }
    }
    
    // 정리
    DisposableEffect(Unit) {
        onDispose {
            try {
                frontModuleService?.stopModule()
                context.unbindService(frontServiceConnection)
                context.unbindService(mainServiceConnection)
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
            
            else -> {
                // TODO: 실제 Surface View 구현
                // 현재는 플레이스홀더
                AndroidView(
                    factory = { context ->
                        android.view.View(context).apply {
                            setBackgroundColor(android.graphics.Color.LTGRAY)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                Text(
                    text = "프론트 모듈 실행 중\n모듈 ID: $moduleId",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}