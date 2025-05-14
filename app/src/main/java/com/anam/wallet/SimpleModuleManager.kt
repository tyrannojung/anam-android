package com.anam.wallet

import android.content.Context
import android.os.Build
import android.util.Log
import com.anam.wallet.core.IPaymentModule
import com.anam.wallet.model.AccountInfo
import com.anam.wallet.model.ModuleInfo
import com.anam.wallet.model.ModuleMetadata
import com.anam.wallet.model.NetworkInfo
import dalvik.system.DexClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * 간소화된 모듈 매니저 클래스
 * 모듈 다운로드 및 로드 기능에 집중
 */
class SimpleModuleManager(internal var context: Context?) {

    companion object {
        private const val TAG = "SimpleModuleManager"
        
        // API URL 설정
        private val API_BASE_URL = when {
            // 에뮬레이터
            Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.startsWith("google/sdk_gphone") -> 
                "http://10.0.2.2:8080"
            // 실제 기기
            else -> "http://localhost:8080"
        }
        
        // 구현 클래스 이름
        private const val MODULE_IMPLEMENTATION_CLASS = "com.anam.wallet.apk.PaymentModuleImpl"
    }
    
    // 로드된 모듈 저장
    private val loadedModules = ConcurrentHashMap<String, IPaymentModule>()
    
    // 모듈 캐시 디렉토리
    private val modulesCacheDir by lazy {
        requireContext().codeCacheDir.let { cacheDir ->
            File(cacheDir, "modules").apply {
                if (!exists()) mkdirs()
            }
        }
    }
    
    // 최적화된 DEX 파일 디렉토리
    private val optimizedDexDir by lazy {
        requireContext().codeCacheDir.let { cacheDir ->
            File(cacheDir, "dex-cache").apply {
                if (!exists()) mkdirs()
            }
        }
    }
    
    /**
     * 필요시 컨텍스트 초기화
     */
    fun initializeContext(context: Context) {
        if (this.context == null) {
            this.context = context
            Log.d(TAG, "Context initialized")
        }
    }
    
    /**
     * 컨텍스트 확인 및 반환
     */
    private fun requireContext(): Context {
        return context ?: throw IllegalStateException("Context is not initialized. Call initializeContext() first.")
    }

    /**
     * 모듈 다운로드 및 로딩 함수
     * @param moduleId 다운로드할 모듈 ID
     * @param onProgress 진행 상황 콜백 (0-100)
     * @param onComplete 완료 콜백 (성공 여부, 메시지)
     */
    suspend fun downloadAndLoadModule(
        moduleId: String = "7",
        onProgress: (Int) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "모듈 다운로드 시작: $moduleId (시간: $startTime)")
            onProgress(0)
            
            // 1. 메타데이터 조회
            Log.d(TAG, "메타데이터 조회 중: $API_BASE_URL/modules/$moduleId")
            val metadata = fetchModuleMetadata(moduleId)
            Log.d(TAG, "메타데이터 수신: ${metadata.name}, 버전 ${metadata.version}")
            onProgress(20)
            
            // 2. 모듈 APK 다운로드
            val apkFile = downloadModuleApk(moduleId, onProgress)
            val downloadTime = System.currentTimeMillis()
            Log.d(TAG, "다운로드 완료: ${apkFile.absolutePath} (${apkFile.length()} bytes)")
            Log.d(TAG, "다운로드 소요 시간: ${downloadTime - startTime}ms")
            onProgress(80)
            
            // 3. 파일 기본 검증
            if (!apkFile.exists() || apkFile.length() == 0L) {
                Log.e(TAG, "다운로드된 파일이 유효하지 않습니다")
                onComplete(false, "다운로드된 파일이 유효하지 않습니다")
                return@withContext
            }
            
            // 4. 파일 권한 설정 (읽기 전용)
            apkFile.setReadable(true, false)
            apkFile.setWritable(false, false)
            onProgress(90)
            
            // 5. 모듈 로드
            Log.d(TAG, "모듈 로드 중: $moduleId (${apkFile.absolutePath})")
            val success = loadModule(moduleId, apkFile)
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "모듈 로드 결과: $success, 총 소요 시간: ${endTime - startTime}ms")
            
            // 6. 결과 전달
            if (success) {
                onProgress(100)
                val moduleName = getModule(moduleId)?.getName() ?: "Unknown"
                onComplete(true, "모듈 로드 성공: $moduleName")
            } else {
                onComplete(false, "모듈 로드 실패")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "모듈 다운로드 및 로드 중 오류 발생", e)
            onComplete(false, "오류: ${e.message}")
        }
    }
    
    /**
     * 메타데이터 조회
     */
    private suspend fun fetchModuleMetadata(moduleId: String): ModuleMetadata = withContext(Dispatchers.IO) {
        val url = URL("$API_BASE_URL/modules/$moduleId")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        try {
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("서버 오류: ${connection.responseCode}")
            }
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "메타데이터 응답: $responseText")
            
            return@withContext ModuleMetadata.fromJson(responseText)
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * 모듈 APK 다운로드
     */
    private suspend fun downloadModuleApk(
        moduleId: String,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val url = URL("$API_BASE_URL/modules/$moduleId/download")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        try {
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("다운로드 서버 오류: ${connection.responseCode}")
            }
            
            // 다운로드할 파일 준비
            val apkFile = File(modulesCacheDir, "module_$moduleId.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }
            
            // 파일 다운로드 진행
            val fileSize = connection.contentLength
            var downloadedSize = 0
            var lastReportedProgress = 20
            
            connection.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        
                        // 진행 상황 보고 (20-80%)
                        if (fileSize > 0) {
                            val progress = 20 + (downloadedSize.toFloat() / fileSize * 60).toInt()
                            if (progress > lastReportedProgress) {
                                lastReportedProgress = progress
                                onProgress(progress)
                            }
                        }
                    }
                }
            }
            
            return@withContext apkFile
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * 모듈 로드 함수
     */
    private fun loadModule(moduleId: String, apkFile: File): Boolean {
        try {
            // 파일 유효성 검사
            if (!apkFile.exists() || !apkFile.canRead()) {
                Log.e(TAG, "APK 파일이 존재하지 않거나 읽을 수 없음: ${apkFile.absolutePath}")
                return false
            }
            
            // 보안 검사
            if (apkFile.canWrite()) {
                Log.e(TAG, "보안 위반: APK 파일이 쓰기 가능함")
                return false
            }
            
            // DexClassLoader 생성
            val classLoader = DexClassLoader(
                apkFile.absolutePath,
                optimizedDexDir.absolutePath,
                null,
                requireContext().classLoader
            )
            
            try {
                // 모듈 구현 클래스 로드
                val moduleClass = classLoader.loadClass(MODULE_IMPLEMENTATION_CLASS)
                
                // IPaymentModule 인터페이스 구현 여부 검사
                val moduleInterface = Class.forName("com.anam.wallet.core.IPaymentModule")
                if (!moduleInterface.isAssignableFrom(moduleClass)) {
                    Log.e(TAG, "클래스가 IPaymentModule 인터페이스를 구현하지 않음")
                    return false
                }
                
                // 모듈 인스턴스 생성
                val moduleInstance = moduleClass.getDeclaredConstructor().newInstance() as IPaymentModule
                
                // 모듈 저장
                loadedModules[moduleId] = moduleInstance
                
                // 모듈 기본 정보 로깅
                val name = moduleInstance.getName()
                val symbol = moduleInstance.getSymbol()
                Log.d(TAG, "모듈 로드 성공: $moduleId ($name, $symbol)")
                
                return true
            } catch (e: Exception) {
                Log.e(TAG, "모듈 클래스 로드 중 오류", e)
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "모듈 로드 중 오류", e)
            return false
        }
    }
    
    /**
     * 로드된 모듈 ID 목록 가져오기
     */
    fun getLoadedModuleIds(): Set<String> = loadedModules.keys
    
    /**
     * 모듈 로드 여부 확인
     */
    fun isModuleLoaded(moduleId: String): Boolean = loadedModules.containsKey(moduleId)
    
    /**
     * 모듈 인스턴스 가져오기
     */
    fun getModule(moduleId: String): IPaymentModule? = loadedModules[moduleId]
    
    /**
     * 모듈 정보 가져오기
     */
    fun getModuleInfo(moduleId: String): ModuleInfo? {
        val module = getModule(moduleId) ?: return null
        return try {
            ModuleInfo.fromJson(module.getModuleInfo())
        } catch (e: Exception) {
            Log.e(TAG, "모듈 정보 파싱 오류", e)
            null
        }
    }
    
    /**
     * 모듈 계정 목록 가져오기
     */
    fun getModuleAccounts(moduleId: String): List<AccountInfo>? {
        val module = getModule(moduleId) ?: return null
        return try {
            AccountInfo.fromJsonArray(module.getAccounts())
        } catch (e: Exception) {
            Log.e(TAG, "모듈 계정 정보 파싱 오류", e)
            null
        }
    }
    
    /**
     * 모듈 네트워크 정보 가져오기
     */
    fun getNetworkInfo(moduleId: String): NetworkInfo? {
        val module = getModule(moduleId) ?: return null
        return try {
            NetworkInfo.fromJson(module.getNetworkInfo())
        } catch (e: Exception) {
            Log.e(TAG, "네트워크 정보 파싱 오류", e)
            null
        }
    }
    
    /**
     * 모듈 요약 정보 가져오기
     */
    fun getModuleSummary(moduleId: String): Map<String, String> {
        val module = getModule(moduleId) ?: return mapOf("error" to "모듈이 로드되지 않음")
        
        return try {
            mapOf(
                "name" to module.getName(),
                "symbol" to module.getSymbol(),
                "accounts" to module.getAccounts(),
                "networkInfo" to module.getNetworkInfo()
            )
        } catch (e: Exception) {
            mapOf("error" to "모듈 정보 조회 중 오류: ${e.message}")
        }
    }
    
    /**
     * 모듈 언로드
     */
    fun unloadModule(moduleId: String): Boolean {
        return if (loadedModules.containsKey(moduleId)) {
            loadedModules.remove(moduleId)
            true
        } else {
            false
        }
    }
}