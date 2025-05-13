package com.anam.wallet

import android.content.*
import android.os.IBinder
import android.util.Log
import com.anam.wallet.core.ICoinModule

/**
 * Helper class to interact with payment modules
 */
class WalletServiceHelper(private val context: Context) {

    companion object {
        private const val TAG = "WalletServiceHelper"
    }

    /**
     * Bind to a module service and execute the callback when ready
     *
     * @param name Module name to bind to (e.g. "eth")
     * @param onReady Callback to execute when the module is ready
     */
    fun bindModule(name: String, onReady: (ICoinModule) -> Unit) {
        try {
            Log.d(TAG, "bindModule 시작: $name")

            // 첫 글자를 대문자로 변환 (eth -> Eth)
            val capitalizedName = name.replaceFirstChar { it.uppercase() }

            // Intent로 어떤 서비스에 연결할지 정의
            val intent = Intent("com.anam.wallet.BIND_COIN_MODULE")
                .setPackage(context.packageName)
                .setClassName(
                    context.packageName,
                    "com.anam.wallet.$name.${capitalizedName}Service"
                )

            Log.d(TAG, "서비스 인텐트 생성: $intent")

            val success = context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    Log.d(TAG, "onServiceConnected: $name")
                    try {
                        val coinModule = ICoinModule.Stub.asInterface(service)
                        Log.d(TAG, "ICoinModule 인터페이스 획득 성공")
                        onReady(coinModule)
                    } catch (e: Exception) {
                        Log.e(TAG, "서비스 연결 후 처리 중 오류", e)
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    Log.d(TAG, "onServiceDisconnected: $name")
                }

                override fun onBindingDied(name: ComponentName) {
                    Log.e(TAG, "onBindingDied: $name")
                }

                override fun onNullBinding(name: ComponentName) {
                    Log.e(TAG, "onNullBinding: $name")
                }
            }, Context.BIND_AUTO_CREATE)

            Log.d(TAG, "bindService 호출 결과: $success")

            if (!success) {
                Log.e(TAG, "서비스 바인딩 실패")
            }
        } catch (e: Exception) {
            Log.e(TAG, "bindModule 에서 예외 발생", e)
        }
    }
}