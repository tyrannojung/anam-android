package com.anam.wallet

import android.content.*
import android.os.IBinder
import android.util.Log
import com.anam.wallet.core.ICoinModule

class WalletServiceHelper(private val context: Context) {
    fun bindModule(name: String, onReady: (ICoinModule) -> Unit) {
        try {
            Log.d("WalletTest", "bindModule 시작: $name")

            // 첫 글자를 대문자로 변환 (eth -> Eth)
            val capitalizedName = name.replaceFirstChar { it.uppercase() }

            // Intent로 어떤 서비스에 연결할지 정의
            // 인텐트를 생성하여 바인딩할 서비스의 액션과 클래스 이름을 지정.
            // 예를 들어, "eth" 모듈은 "com.anam.wallet.eth.EthService" 클래스를 찾는다.
            val intent = Intent("com.anam.wallet.BIND_COIN_MODULE")
                .setPackage(context.packageName)
                .setClassName(
                    context.packageName,
                    "com.anam.wallet.$name.${capitalizedName}Service"
                )

            Log.d("WalletTest", "서비스 인텐트 생성: $intent")

            val success = context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    Log.d("WalletTest", "onServiceConnected: $name")
                    try {
                        val coinModule = ICoinModule.Stub.asInterface(service)
                        Log.d("WalletTest", "ICoinModule 인터페이스 획득 성공")
                        onReady(coinModule)
                    } catch (e: Exception) {
                        Log.e("WalletTest", "서비스 연결 후 처리 중 오류", e)
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    Log.d("WalletTest", "onServiceDisconnected: $name")
                }

                override fun onBindingDied(name: ComponentName) {
                    Log.e("WalletTest", "onBindingDied: $name")
                }

                override fun onNullBinding(name: ComponentName) {
                    Log.e("WalletTest", "onNullBinding: $name")
                }
            }, Context.BIND_AUTO_CREATE)

            Log.d("WalletTest", "bindService 호출 결과: $success")

            if (!success) {
                Log.e("WalletTest", "서비스 바인딩 실패")
            }
        } catch (e: Exception) {
            Log.e("WalletTest", "bindModule 에서 예외 발생", e)
        }
    }
}