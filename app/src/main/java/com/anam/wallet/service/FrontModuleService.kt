package com.anam.wallet.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.ui.platform.ComposeView
import com.anam.wallet.IMainAppService
import com.anam.wallet.IFrontModuleService
import com.anam.wallet.core.IFrontModuleUI
import com.anam.wallet.core.FrontModuleContext

private const val TAG = "FrontModuleService"

/**
 * 별도 프로세스(:front_module_process)에서 실행되는 서비스
 * 프론트 모듈을 로드하고 UI를 렌더링함
 */
class FrontModuleService : Service() {
    
    private var frontModule: IFrontModuleUI? = null
    private lateinit var mainAppService: IMainAppService
    
    private val binder = object : IFrontModuleService.Stub() {
        override fun loadModule(apkPath: String, className: String, moduleId: String) {
            Log.d(TAG, "Loading front module: $className from $apkPath")
            
            try {
                // 동적으로 APK 로드
                val moduleInstance = loadModuleFromApk(apkPath, className)
                
                if (moduleInstance is IFrontModuleUI) {
                    frontModule = moduleInstance
                    frontModule?.setMainAppService(mainAppService)
                    frontModule?.onModuleStart()
                    
                    Log.d(TAG, "Front module loaded successfully")
                } else {
                    val errorMsg = "Module does not implement IFrontModuleUI: ${moduleInstance::class.java.name}"
                    Log.e(TAG, errorMsg)
                    throw IllegalArgumentException(errorMsg)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load front module", e)
            }
        }
        
        override fun stopModule() {
            Log.d(TAG, "Stopping front module")
            frontModule?.onModuleStop()
            frontModule?.onModuleDestroy()
            frontModule = null
        }
        
        override fun setMainAppService(service: IMainAppService) {
            Log.d(TAG, "Setting main app service")
            mainAppService = service
            frontModule?.setMainAppService(service)
        }
        
        override fun createModuleSurface(width: Int, height: Int): android.view.Surface? {
            Log.d(TAG, "Creating module surface: ${width}x${height}")
            
            return try {
                if (frontModule == null) {
                    Log.w(TAG, "Front module not loaded")
                    return null
                }
                
                // Surface 생성
                createComposeUIOnSurface(width, height)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create module surface", e)
                null
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "FrontModuleService bound")
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FrontModuleService created in process: ${android.os.Process.myPid()}")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FrontModuleService destroyed")
        frontModule?.onModuleDestroy()
    }
    
    private fun loadModuleFromApk(apkPath: String, className: String): Any {
        try {
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
                cacheDir?.absolutePath ?: "/tmp",
                null,
                this.classLoader
            )
            
            // 외부 APK에서 클래스 로드
            val moduleClass = classLoader.loadClass(className)
            val moduleInstance = moduleClass.getDeclaredConstructor().newInstance()
            
            Log.d(TAG, "Module loaded successfully: ${moduleInstance::class.java.name}")
            return moduleInstance
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load module from APK", e)
            throw RuntimeException("모듈 로드 실패: ${e.message}", e)
        }
    }
    
    /**
     * Compose UI를 Surface에 렌더링
     */
    private fun createComposeUIOnSurface(width: Int, height: Int): Surface? {
        try {
            Log.d(TAG, "Creating Compose UI on Surface")
            
            // SurfaceView 생성 (별도 프로세스에서)
            val surfaceView = SurfaceView(this).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(width, height)
                holder.setFormat(PixelFormat.RGBA_8888)
            }
            
            // SurfaceHolder 콜백 설정
            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    Log.d(TAG, "Surface created, rendering Compose UI")
                    
                    // 실제 Compose UI 렌더링
                    renderComposeUIToSurface(holder)
                }
                
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    Log.d(TAG, "Surface changed: ${width}x${height}")
                }
                
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    Log.d(TAG, "Surface destroyed")
                }
            })
            
            return surfaceView.holder.surface
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Surface", e)
            return null
        }
    }
    
    /**
     * 실제 Compose UI를 Surface에 렌더링
     */
    private fun renderComposeUIToSurface(holder: SurfaceHolder) {
        try {
            // ComposeView 생성 (별도 프로세스에서)
            val composeView = ComposeView(this)
            
            // 실제 프론트 모듈의 Composable 설정
            composeView.setContent {
                frontModule?.FrontModuleScreen(
                    FrontModuleContext(
                        moduleId = "current_module", // 실제 모듈 ID로 대체 필요
                        parameters = emptyMap()
                    )
                )
            }
            
            // ComposeView를 Surface에 그리기
            // 이 부분은 추가 작업이 필요 (Canvas를 통한 렌더링)
            Log.d(TAG, "Compose UI content set")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render Compose UI", e)
        }
    }
}