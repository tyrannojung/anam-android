package com.anam.wallet.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.SurfaceControlViewHost
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
    private var surfaceControlViewHost: SurfaceControlViewHost? = null
    
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
        
        override fun createModuleSurfacePackage(width: Int, height: Int): SurfaceControlViewHost.SurfacePackage? {
            Log.d(TAG, "Creating module surface package: ${width}x${height}")
            
            return try {
                if (frontModule == null) {
                    Log.w(TAG, "Front module not loaded")
                    return null
                }
                
                // API 29+ 에서만 SurfaceControlViewHost 사용 가능
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createSurfaceControlViewHost(width, height)
                } else {
                    Log.e(TAG, "SurfaceControlViewHost requires API 29+")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create module surface package", e)
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
        
        // SurfaceControlViewHost 정리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            surfaceControlViewHost?.release()
            surfaceControlViewHost = null
        }
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
     * SurfaceControlViewHost를 사용하여 실제 Compose UI를 렌더링
     * API 29+ (Android 10+) 에서만 사용 가능
     */
    private fun createSurfaceControlViewHost(width: Int, height: Int): SurfaceControlViewHost.SurfacePackage? {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Log.e(TAG, "SurfaceControlViewHost requires API 29+")
                return null
            }
            
            Log.d(TAG, "Creating SurfaceControlViewHost: ${width}x${height}")
            
            // SurfaceControlViewHost 생성
            surfaceControlViewHost = SurfaceControlViewHost(
                this, // context
                display, // display - 서비스의 기본 디스플레이 사용
                null as android.os.IBinder? // hostToken - null이면 자동 생성
            )
            
            // ComposeView 생성
            val composeView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                
                // 실제 APK의 Compose UI 설정
                setContent {
                    frontModule!!.FrontModuleScreen(
                        FrontModuleContext(
                            moduleId = "current_module",
                            parameters = emptyMap()
                        )
                    )
                }
            }
            
            // ComposeView를 SurfaceControlViewHost에 설정
            // 이렇게 하면 ComposeView가 올바른 Window에 attach됨
            surfaceControlViewHost!!.setView(composeView, width, height)
            
            // SurfacePackage 반환 - 이것이 다른 프로세스에서 렌더링할 수 있는 객체
            val surfacePackage = surfaceControlViewHost!!.surfacePackage
            Log.d(TAG, "SurfaceControlViewHost created successfully with SurfacePackage")
            
            return surfacePackage
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create SurfaceControlViewHost", e)
            null
        }
    }
}