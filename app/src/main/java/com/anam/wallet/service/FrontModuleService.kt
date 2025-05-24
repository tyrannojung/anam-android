package com.anam.wallet.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.media.ImageReader
import android.os.IBinder
import android.util.Log
import android.view.Surface
import com.anam.wallet.IMainAppService
import com.anam.wallet.IFrontModuleService
import com.anam.wallet.core.IFrontModuleUI

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
     * ImageReader를 사용하여 Window에 의존하지 않는 Surface 생성
     */
    private fun createComposeUIOnSurface(width: Int, height: Int): Surface? {
        return try {
            Log.d(TAG, "Creating module surface with ImageReader: ${width}x${height}")
            
            // ImageReader로 Window-less Surface 생성
            val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
            val surface = imageReader.surface
            
            // Surface에 직접 그리기
            drawModuleUIOnSurface(surface, width, height)
            
            Log.d(TAG, "Module surface created and drawn successfully")
            surface
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create module surface", e)
            null
        }
    }
    
    /**
     * Surface에 모듈 UI 직접 그리기
     */
    private fun drawModuleUIOnSurface(surface: Surface, width: Int, height: Int) {
        try {
            Log.d(TAG, "Drawing module UI directly on surface")
            
            val canvas = surface.lockCanvas(Rect(0, 0, width, height))
            canvas?.let {
                try {
                    // 배경색 설정
                    it.drawColor(Color.LTGRAY)
                    
                    // 모듈 UI 테스트 드로잉
                    val paint = android.graphics.Paint().apply {
                        color = Color.BLACK
                        textSize = 60f
                        isAntiAlias = true
                    }
                    
                    // 모듈 정보 표시
                    it.drawText("Front Module Loaded!", 100f, 200f, paint)
                    it.drawText("Module is running in separate process", 100f, 300f, paint)
                    
                    // 실제 로드된 모듈 정보 표시
                    frontModule?.let { module ->
                        it.drawText("Module Class: ${module.javaClass.simpleName}", 100f, 400f, paint)
                        it.drawText("Status: Ready", 100f, 500f, paint)
                    } ?: run {
                        it.drawText("Status: Module not loaded", 100f, 400f, paint)
                    }
                    
                    // 추가 정보
                    it.drawText("Surface: ${width}x${height}", 100f, 600f, paint)
                    it.drawText("Process: ${android.os.Process.myPid()}", 100f, 700f, paint)
                    
                    Log.d(TAG, "Module UI drawn successfully on surface")
                    
                } finally {
                    surface.unlockCanvasAndPost(it)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to draw module UI on surface", e)
        }
    }
}