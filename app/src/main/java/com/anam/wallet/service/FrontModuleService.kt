package com.anam.wallet.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.SurfaceControlViewHost
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import com.anam.wallet.IMainAppService
import com.anam.wallet.IFrontModuleService
import com.anam.wallet.core.IFrontModuleUI
import com.anam.wallet.core.FrontModuleContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "FrontModuleService"

/**
 * SurfaceControlViewHost용 LifecycleOwner 구현
 * Service 컨텍스트에서는 자동으로 제공되지 않으므로 수동 생성
 */
class HostLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    
    override val lifecycle: Lifecycle = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore = store
    
    fun moveTo(state: Lifecycle.State) {
        if (state == Lifecycle.State.CREATED) {
            savedStateRegistryController.performRestore(null)
        }
        lifecycleRegistry.currentState = state
    }
    
    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
    }
}

/**
 * 별도 프로세스(:front_module_process)에서 실행되는 서비스
 * 프론트 모듈을 로드하고 UI를 렌더링함
 */
class FrontModuleService : Service() {
    
    private var frontModule: IFrontModuleUI? = null
    private lateinit var mainAppService: IMainAppService
    private var surfaceControlViewHost: SurfaceControlViewHost? = null
    private var hostLifecycleOwner: HostLifecycleOwner? = null
    
    /** 중복 Host 정리용 헬퍼 - Surface 누수 방지 */
    private fun disposeHost() {
        hostLifecycleOwner?.destroy()
        surfaceControlViewHost?.release()
        hostLifecycleOwner = null
        surfaceControlViewHost = null
    }
    
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
        
        override fun createModuleSurfacePackage(hostToken: android.os.IBinder, width: Int, height: Int): SurfaceControlViewHost.SurfacePackage? {
            Log.d(TAG, "Creating module surface package: ${width}x${height} with hostToken")
            
            return try {
                if (frontModule == null) {
                    Log.w(TAG, "Front module not loaded")
                    return null
                }
                
                // 먼저 기존 Host 정리 (누수 방지)
                disposeHost()
                
                // API 29+ 에서만 SurfaceControlViewHost 사용 가능
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createSurfaceControlViewHost(hostToken, width, height)
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
        disposeHost()          // 한 줄로 정리
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
     * SurfaceControlViewHost를 사용하여 실제 Compose UI를 렌더링
     * API 29+ (Android 10+) 에서만 사용 가능
     */
    private fun createSurfaceControlViewHost(hostToken: android.os.IBinder, width: Int, height: Int): SurfaceControlViewHost.SurfacePackage? {
        return try {
            Log.d(TAG, "Creating SurfaceControlViewHost: ${width}x${height}")
            
            // DisplayManager를 통해 기본 디스플레이 얻기
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            
            // WindowContext 생성 (터치/IME 포커스 문제 해결)
            val windowContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                createWindowContext(
                    defaultDisplay,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    null
                )
            } else {
                createDisplayContext(defaultDisplay)
            }
            
            // UI 객체는 메인 루퍼에서 생성해야 함 (Binder 스레드에서 직접 생성하면 안 됨)
            val resultRef = AtomicReference<SurfaceControlViewHost.SurfacePackage?>()
            val latch = CountDownLatch(1)
            
            Handler(Looper.getMainLooper()).post {
                try {
                    // SurfaceControlViewHost 생성 (3-파라미터만 사용)
                    Log.d(TAG, "Using 3-parameter SCVH constructor (touch 연결은 transferTouchGesture로 처리)")
                    surfaceControlViewHost = SurfaceControlViewHost(windowContext, defaultDisplay, hostToken)
                    
                    // LifecycleOwner 생성 및 시작
                    hostLifecycleOwner = HostLifecycleOwner().apply {
                        moveTo(Lifecycle.State.CREATED)   // performRestore()
                        moveTo(Lifecycle.State.STARTED)
                        moveTo(Lifecycle.State.RESUMED)   // 터치 이벤트 활성화
                    }
                    
                    // ComposeView 생성 - 메인 스레드에서
                    val composeView = ComposeView(windowContext).apply {
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
                    
                    // FrameLayout root 생성 및 ViewTree 설정
                    val root = FrameLayout(windowContext).apply {
                        // 1) ViewTree 주입 (addView 전에 먼저 설정)
                        setViewTreeLifecycleOwner(hostLifecycleOwner!!)
                        setViewTreeSavedStateRegistryOwner(hostLifecycleOwner!!)
                        setViewTreeViewModelStoreOwner(hostLifecycleOwner!!)
                        
                        // 2) ComposeView 추가 (ViewTree 설정 후)
                        addView(composeView, FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        ))
                        
                        // 3) 포커스 설정
                        isFocusableInTouchMode = true
                        requestFocus()
                    }
                    
                    // root를 SurfaceControlViewHost에 설정 - 메인 스레드에서
                    surfaceControlViewHost!!.setView(root, width, height)
                    
                    // SurfacePackage 반환
                    val surfacePackage = surfaceControlViewHost!!.surfacePackage
                    Log.d(TAG, "SurfaceControlViewHost created successfully with LifecycleOwner")
                    
                    resultRef.set(surfacePackage)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create SurfaceControlViewHost with LifecycleOwner", e)
                    resultRef.set(null)
                } finally {
                    latch.countDown()
                }
            }
            
            // 메인 스레드 작업 완료까지 대기 (10초 타임아웃 - DexClassLoader 초기화 고려)
            if (latch.await(10, TimeUnit.SECONDS)) {
                return resultRef.get()
            } else {
                Log.e(TAG, "SurfaceControlViewHost creation timed out (10초)")
                return null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create SurfaceControlViewHost", e)
            null
        }
    }
}