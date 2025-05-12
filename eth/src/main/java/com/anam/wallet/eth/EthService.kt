package com.anam.wallet.eth

import android.util.Log
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.anam.wallet.core.CoinModule

class EthService : Service() {

    private val impl = object : CoinModule() {
        override fun getSymbol() = "ETH"

        override fun request(jsonRpcPayload: String) =
            """{"id":1,"result":"dummy"}"""

        override fun createAccount(encMessage: String): String {
            // 1. encMessage 복호화 (향후 하이브리드 암호화 구현 시)
            // 2. 실제 이더리움 계정 생성
            // 3. 키스토어 파일 생성 및 저장
            // 4. 생성된 계정 주소 반환

            try {
                // 실제 ETH 계정 생성 로직 구현
                val address = "10x..." // 실제 생성된 주소

                return """{"address":"$address"}"""
            } catch (e: Exception) {
                Log.e("EthService", "계정 생성 실패", e)
                return """{"error":"${e.message}"}"""
            }
        }
    }

    /** Service 가 클라이언트에 돌려주는 Binder */
    override fun onBind(intent: Intent?): IBinder = impl
}
