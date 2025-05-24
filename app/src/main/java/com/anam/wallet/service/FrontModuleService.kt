package com.anam.wallet.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.anam.wallet.IMainAppService
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
}