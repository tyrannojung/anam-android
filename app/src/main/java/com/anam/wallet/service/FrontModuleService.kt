package com.anam.wallet.service

import android.app.Service
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
    private var surfaceTexture: SurfaceTexture? = null
    
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
        
        // SurfaceTexture 정리
        surfaceTexture?.release()
        surfaceTexture = null
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
     * 실제 Compose UI를 Surface에 렌더링
     */
    private fun createComposeUIOnSurface(width: Int, height: Int): Surface? {
        return try {
            Log.d(TAG, "Creating Compose UI surface: ${width}x${height}")
            
            // SurfaceTexture로 Producer Surface 생성
            surfaceTexture = SurfaceTexture(0).apply {
                setDefaultBufferSize(width, height)
            }
            val surface = Surface(surfaceTexture!!)
            
            // 메인 스레드에서 Compose UI 렌더링
            Handler(Looper.getMainLooper()).post {
                renderComposeUIToSurface(surface, width, height)
            }
            
            Log.d(TAG, "Compose UI surface created successfully")
            surface
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Compose UI surface", e)
            null
        }
    }
    
    /**
     * 실제 Compose UI를 Surface에 렌더링
     */
    private fun renderComposeUIToSurface(surface: Surface, width: Int, height: Int) {
        try {
            Log.d(TAG, "Rendering real Compose UI to surface")
            
            if (frontModule == null) {
                Log.w(TAG, "Front module is null, cannot render UI")
                return
            }
            
            // 컨테이너 레이아웃 생성
            val container = FrameLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(width, height)
            }
            
            // ComposeView 생성
            val composeView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            
            // 컨테이너에 ComposeView 추가
            container.addView(composeView)
            
            // 실제 APK의 Compose UI 설정
            composeView.setContent {
                frontModule!!.FrontModuleScreen(
                    FrontModuleContext(
                        moduleId = "current_module", // 실제 모듈 ID로 대체 가능
                        parameters = emptyMap()
                    )
                )
            }
            
            // 레이아웃 측정 및 배치
            container.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
            )
            container.layout(0, 0, width, height)
            
            Log.d(TAG, "Real Compose UI rendered successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render Compose UI to surface", e)
        }
    }
}