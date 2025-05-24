package com.anam.wallet.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
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
                // 실제 Surface 렌더링
                AndroidView(
                    factory = { context ->
                        SurfaceView(context).apply {
                            holder.setFormat(PixelFormat.RGBA_8888)
                            
                            // Surface 준비되면 프론트 모듈 Surface 요청
                            holder.addCallback(object : SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    Log.d(TAG, "Main Surface created, requesting module surface")
                                    requestModuleSurface(
                                        frontModuleService, 
                                        holder.surfaceFrame.width(), 
                                        holder.surfaceFrame.height()
                                    ) { surface ->
                                        moduleSurface = surface
                                        displayModuleSurface(holder, surface)
                                    }
                                }
                                
                                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                                    Log.d(TAG, "Main Surface changed: ${width}x${height}")
                                    requestModuleSurface(
                                        frontModuleService, 
                                        width, 
                                        height
                                    ) { surface ->
                                        moduleSurface = surface
                                        displayModuleSurface(holder, surface)
                                    }
                                }
                                
                                override fun surfaceDestroyed(holder: SurfaceHolder) {
                                    Log.d(TAG, "Main Surface destroyed")
                                    moduleSurface?.release()
                                    moduleSurface = null
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Surface 요청 함수
 */
private fun requestModuleSurface(
    frontModuleService: IFrontModuleService?,
    width: Int, 
    height: Int,
    onSurfaceReceived: (Surface?) -> Unit
) {
    try {
        Log.d(TAG, "Requesting module surface: ${width}x${height}")
        
        frontModuleService?.let { service ->
            val moduleSurface = service.createModuleSurface(width, height)
            if (moduleSurface != null) {
                Log.d(TAG, "Module surface received successfully")
                onSurfaceReceived(moduleSurface)
            } else {
                Log.w(TAG, "Failed to get module surface")
                onSurfaceReceived(null)
            }
        } ?: run {
            Log.w(TAG, "FrontModuleService is null")
            onSurfaceReceived(null)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to request module surface", e)
        onSurfaceReceived(null)
    }
}

/**
 * 모듈 Surface를 메인 Surface에 표시
 */
private fun displayModuleSurface(
    mainHolder: SurfaceHolder,
    moduleSurface: Surface?
) {
    try {
        if (moduleSurface == null) {
            Log.w(TAG, "Module surface is null, cannot display")
            return
        }
        
        Log.d(TAG, "Displaying module surface on main surface")
        
        // Canvas를 통해 모듈 Surface 내용을 메인 Surface에 복사
        val canvas = mainHolder.lockCanvas()
        canvas?.let {
            try {
                // 배경색 설정
                it.drawColor(android.graphics.Color.WHITE)
                
                // 여기서 실제 모듈 Surface 내용을 그려야 함
                // 현재는 단순히 배경만 그림
                
                Log.d(TAG, "Module surface content drawn to main surface")
            } finally {
                mainHolder.unlockCanvasAndPost(it)
            }
        }
        
    } catch (e: Exception) {
        Log.e(TAG, "Failed to display module surface", e)
    }
}