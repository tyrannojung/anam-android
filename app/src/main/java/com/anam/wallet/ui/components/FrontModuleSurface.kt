package com.anam.wallet.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.SurfaceControlViewHost
import android.view.SurfaceView
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
 * SurfaceControlViewHost를 통해 UI를 렌더링하는 컴포넌트
 * API 29+ (Android 10+) 에서만 동작
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
                // API 버전 체크
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // SurfaceView로 SurfacePackage 연결
                    AndroidView(
                        factory = { context ->
                            SurfaceView(context).apply {
                                // 터치 이벤트를 위해 포커스 설정
                                isFocusableInTouchMode = true
                                requestFocus()
                                
                                // SurfaceView가 준비되면 SurfacePackage 요청
                                holder.addCallback(object : android.view.SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                                        Log.d(TAG, "SurfaceView surface created")
                                    }
                                    
                                    override fun surfaceChanged(holder: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {
                                        Log.d(TAG, "SurfaceView surface changed: ${width}x${height}")
                                        
                                        // 프론트 모듈 서비스에서 SurfacePackage를 받아서 SurfaceView에 연결
                                        requestModuleSurfacePackage(
                                            frontModuleService,
                                            this@apply,
                                            width,
                                            height
                                        )
                                    }
                                    
                                    override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                                        Log.d(TAG, "SurfaceView surface destroyed")
                                    }
                                })
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // API 29 미만에서는 에러 메시지 표시
                    Text(
                        text = "SurfaceControlViewHost는 Android 10 (API 29) 이상에서만 지원됩니다",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 프론트 모듈 서비스에서 SurfacePackage를 받아서 SurfaceView에 연결
 * API 29+ (Android 10+) 에서만 동작
 */
private fun requestModuleSurfacePackage(
    frontModuleService: IFrontModuleService?,
    surfaceView: SurfaceView,
    width: Int, 
    height: Int
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    
    fun tryRequest() {
        try {
            Log.d(TAG, "Requesting module surface package for SurfaceView: ${width}x${height}")
            
            frontModuleService?.let { service ->
                // SurfaceView의 hostToken 가져오기 (API 29+)
                val hostToken = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    surfaceView.hostToken
                } else {
                    null
                }
                
                if (hostToken == null) {
                    // hostToken이 아직 null이면 한 프레임 뒤 재시도
                    Log.w(TAG, "hostToken 아직 null – 16ms 후 재시도")
                    surfaceView.postDelayed({ tryRequest() }, 16)
                    return@let
                }
                
                Log.d(TAG, "hostToken: ${if (hostToken != null) "OK" else "NULL"}")
                
                // 서비스에서 SurfaceControlViewHost가 생성한 SurfacePackage 받기
                val surfacePackage = service.createModuleSurfacePackage(hostToken, width, height)
                if (surfacePackage != null) {
                    Log.d(TAG, "Module SurfacePackage received from SurfaceControlViewHost")
                    
                    // SurfacePackage를 SurfaceView에 설정
                    surfaceView.setChildSurfacePackage(surfacePackage)
                    
                    Log.d(TAG, "SurfacePackage attached – single-process ✓")
                    
                } else {
                    Log.w(TAG, "SurfacePackage null")
                }
            } ?: run {
                Log.w(TAG, "FrontModuleService is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request module surface package", e)
        }
    }
    
    tryRequest()
}