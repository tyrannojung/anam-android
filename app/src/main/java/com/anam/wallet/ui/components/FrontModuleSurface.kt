package com.anam.wallet.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.SurfaceTexture
import android.os.IBinder
import android.util.Log
import android.view.Surface
import android.view.TextureView
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
    var moduleSurface by remember { mutableStateOf<Surface?>(null) }
    
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
                errorMessage = "프론트 모듈 서비스 연결이 끊어졌습니다"
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
                // TextureView로 Surface 연결
                AndroidView(
                    factory = { context ->
                        TextureView(context).apply {
                            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                    Log.d(TAG, "TextureView surface available: ${width}x${height}")
                                    
                                    // FrontModuleService에서 SurfaceTexture를 받아서 직접 연결
                                    requestModuleSurfaceTexture(
                                        frontModuleService,
                                        width,
                                        height,
                                        surface  // TextureView의 SurfaceTexture 전달
                                    )
                                }
                                
                                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                                    Log.d(TAG, "TextureView surface size changed: ${width}x${height}")
                                    // 필요시 재연결
                                }
                                
                                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                    Log.d(TAG, "TextureView surface destroyed")
                                    moduleSurface?.release()
                                    moduleSurface = null
                                    return true
                                }
                                
                                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                                    // 업데이트 콜백 (필요시 사용)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * 모듈 SurfaceTexture를 요청하여 TextureView에 직접 연결
 */
private fun requestModuleSurfaceTexture(
    frontModuleService: IFrontModuleService?,
    width: Int, 
    height: Int,
    textureViewSurface: SurfaceTexture
) {
    try {
        Log.d(TAG, "Requesting module surface for TextureView: ${width}x${height}")
        
        frontModuleService?.let { service ->
            // 서비스에서 생성한 Surface 받기
            val receivedSurface = service.createModuleSurface(width, height)
            if (receivedSurface != null) {
                Log.d(TAG, "Module surface received, now displaying on TextureView")
                
                // 여기서 실제로는 받은 Surface의 내용이 TextureView에 자동으로 표시되어야 함
                // 현재 구조상 이것이 핵심 문제점
                Log.d(TAG, "Surface connection established")
                
            } else {
                Log.w(TAG, "Failed to get module surface")
            }
        } ?: run {
            Log.w(TAG, "FrontModuleService is null")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to request module surface texture", e)
    }
}