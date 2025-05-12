package com.anam.wallet

import android.content.Context
import android.util.Log
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/** dynamic‑feature 모듈 on‑demand 설치 헬퍼 */
class ModuleInstaller(context: Context) {

    // 클래스 생성 시 Context를 받고, 이를 이용해 SplitInstallManager를 초기화
    private val manager = SplitInstallManagerFactory.create(context)

    /** 이미 설치된 모듈 목록 (예: ["eth","btc"]) */
    // 현재 설치된 동적 모듈 목록을 반환
    fun installedModules(): Set<String> {
        val modules = manager.installedModules
        Log.d("WalletTest", "installedModules 호출: $modules")
        return modules
    }

    /** 모듈 설치 */
    fun install(
        module: String,
        onInstalled: () -> Unit, // Unit을 반환은 즉, "아무것도 반환하지 않음"을 의미 자바로 치면 void와 같음
        onError: (Int) -> Unit
    ) {
        Log.d("WalletTest", "install 호출: $module")

        // 1) 이미 설치돼 있으면 바로 콜백
        if (module in manager.installedModules) {
            Log.d("WalletTest", "$module 모듈이 이미 설치되어 있어 onInstalled 콜백 호출")
            onInstalled()
            return
        }

        // 2) 설치 요청
        val request = SplitInstallRequest.newBuilder()
            .addModule(module)
            .build()

        Log.d("WalletTest", "설치 요청 생성: $request")

        manager.startInstall(request)
            .addOnSuccessListener { id ->
                Log.d("WalletTest", "$module 모듈 설치 요청 성공: requestId=$id")
            }
            .addOnFailureListener { e ->
                Log.e("WalletTest", "$module 모듈 설치 요청 실패", e)
                if (e is SplitInstallException) {
                    Log.e("WalletTest", "SplitInstallException 에러 코드: ${e.errorCode}")
                    onError(e.errorCode)
                } else {
                    onError(-1)
                }
            }

        // 3) 상태 수신 리스너
        // SplitInstallStateUpdatedListener는 설치 설치 상태 변화를 감지하는 리스너, 설치가 완료되면 리스너를 해제하고 성공 콜백을 호출
        val listener = object : SplitInstallStateUpdatedListener {
            override fun onStateUpdate(state: SplitInstallSessionState) {
                Log.d("WalletTest", "$module 모듈 상태 업데이트: ${state.status()}")

                if (module in state.moduleNames() &&
                    state.status() == SplitInstallSessionStatus.INSTALLED
                ) {
                    Log.d("WalletTest", "$module 모듈 설치 완료, 리스너 해제 및 콜백 호출")
                    manager.unregisterListener(this)
                    onInstalled()
                }
            }
        }

        Log.d("WalletTest", "상태 리스너 등록")
        manager.registerListener(listener)
    }

    // 모듈 삭제 상태 확인
    fun checkUninstallStatus(module: String): Boolean {
        try {
            val result = !manager.installedModules.contains(module)
            Log.d("WalletTest", "모듈 '$module' 제거 상태 확인: $result")
            return result
        } catch (e: Exception) {
            Log.e("WalletTest", "모듈 제거 상태 확인 중 오류", e)
            return false
        }
    }
}