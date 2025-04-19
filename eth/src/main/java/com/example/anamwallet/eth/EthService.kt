package com.example.anamwallet.eth

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.anamwallet.core.CoinModule

class EthService : Service() {

    private val impl = object : CoinModule() {
        override fun getSymbol() = "ETH"

        override fun request(jsonRpcPayload: String) =
            """{"id":1,"result":"dummy"}"""
    }

    /** Service 가 클라이언트에 돌려주는 Binder */
    override fun onBind(intent: Intent?): IBinder = impl
}
