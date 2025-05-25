package com.anam.wallet

import android.content.Context
import android.os.Build
import android.util.Log
import com.anam.wallet.constants.ModuleConstants
import com.anam.wallet.model.ModuleMetadata
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
        
    }
    
    // 다운로드된 모듈 메타데이터 저장
    private val downloadedModules = ConcurrentHashMap<String, String>()
    
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
        moduleId: String,
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
            
            // 5. 다운로드 완료
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "프론트 모듈 다운로드 완료: $moduleId (${apkFile.absolutePath})")
            Log.d(TAG, "총 소요 시간: ${endTime - startTime}ms")
            
            // 6. 다운로드 완료 기록
            downloadedModules[moduleId] = apkFile.absolutePath
            
            // 7. 결과 전달
            onProgress(100)
            onComplete(true, "프론트 모듈 다운로드 완료")
            
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
     * 다운로드된 모듈 ID 목록 가져오기
     */
    fun getDownloadedModuleIds(): Set<String> = downloadedModules.keys
    
    /**
     * 모듈 다운로드 여부 확인
     */
    fun isModuleDownloaded(moduleId: String): Boolean = downloadedModules.containsKey(moduleId)
    
    /**
     * 다운로드된 모듈 APK 경로 가져오기
     */
    fun getModuleApkPath(moduleId: String): String? {
        return if (isModuleDownloaded(moduleId)) {
            File(modulesCacheDir, "module_$moduleId.apk").absolutePath
        } else null
    }
    
    /**
     * 모듈 삭제 (APK 파일 삭제)
     */
    fun deleteModule(moduleId: String): Boolean {
        return try {
            val apkFile = File(modulesCacheDir, "module_$moduleId.apk")
            if (apkFile.exists()) {
                apkFile.delete()
                downloadedModules.remove(moduleId)
                Log.d(TAG, "모듈 삭제 완료: $moduleId")
                true
            } else {
                Log.w(TAG, "삭제할 모듈 파일이 없음: $moduleId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "모듈 삭제 중 오류", e)
            false
        }
    }
}