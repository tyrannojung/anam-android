package com.anam.wallet.ui.screens.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam.wallet.ModuleInstaller
import com.anam.wallet.WalletServiceHelper
import com.anam.wallet.ui.components.ScrollableContent
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory

@Composable
fun MainScreen() {

    val context = LocalContext.current
    val installer = remember { ModuleInstaller(context) }
    val walletSvc = remember { WalletServiceHelper(context) }

    var msg by remember { mutableStateOf("• ETH 모듈 설치 전") }
    var isLoading by remember { mutableStateOf(false) }

    // 초기 상태에서 모듈 상태 확인 및 로그 출력
    LaunchedEffect(key1 = Unit) { // React의 useEffect(() => {}, []) 와 유사 [] (의존성 없음)이랑 같은 의미로 key1 = Unit을 주면, 이 컴포저블이 처음 화면에 나타날 때 딱 한 번만 실행됨
        val installedModules = installer.installedModules() // 현재 앱에 설치된 모듈 이름 목록을 반환
        Log.d("WalletTest", "현재 설치된 모듈: $installedModules")
        if ("eth" in installedModules) {
            msg = "• ETH 모듈이 이미 설치되어 있습니다"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            msg,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ===== 설치 버튼 =====
        if ("eth" !in installer.installedModules()) {
            Button(
                onClick = {
                    isLoading = true
                    msg = "📥 설치 요청 중..."
                    Log.d("WalletTest", "ETH 모듈 설치 시작")

                    try {
                        installer.install(
                            module = "eth",
                            onInstalled = {
                                msg = "✅ 설치 완료, 바인딩 중..."
                                Log.d("WalletTest", "ETH 모듈 설치 완료, 바인딩 시작")
                                bindEthModule(walletSvc) { newMsg ->
                                    msg = newMsg
                                    isLoading = false
                                }
                            },
                            onError = { code ->
                                msg = "❌ 설치 실패 ($code)"
                                Log.e("WalletTest", "ETH 모듈 설치 실패: $code")
                                isLoading = false
                            }
                        )
                    } catch (e: Exception) {
                        msg = "❌ 예외 발생: ${e.message}"
                        Log.e("WalletTest", "설치 중 예외 발생", e)
                        isLoading = false
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("ETH 모듈 설치")
            }

            // 테스트용 모듈 제거 버튼 추가
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    try {
                        // 모듈 강제 제거 시도
                        val splitInstallManager = SplitInstallManagerFactory.create(context)
                        splitInstallManager.deferredUninstall(listOf("eth"))
                        msg = "🗑️ ETH 모듈 제거 요청됨 (앱 재시작 필요)"
                        Log.d("WalletTest", "ETH 모듈 제거 요청됨")
                    } catch (e: Exception) {
                        msg = "❌ 모듈 제거 실패: ${e.message}"
                        Log.e("WalletTest", "모듈 제거 중 예외 발생", e)
                    }
                }
            ) { Text("ETH 모듈 제거 (테스트용)") }

        } else {
            Button(
                onClick = {
                    isLoading = true
                    msg = "🔄 이미 설치된 모듈에 바인딩 중..."
                    Log.d("WalletTest", "이미 설치된 ETH 모듈에 바인딩 시작")
                    bindEthModule(walletSvc) { newMsg ->
                        msg = newMsg
                        isLoading = false
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("ETH 모듈 이미 설치됨")
            }

            // 테스트용 모듈 제거 버튼 추가
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    try {
                        // 모듈 강제 제거 시도
                        val splitInstallManager = SplitInstallManagerFactory.create(context)
                        splitInstallManager.deferredUninstall(listOf("eth"))
                        msg = "🗑️ ETH 모듈 제거 요청됨 (앱 재시작 필요)"
                        Log.d("WalletTest", "ETH 모듈 제거 요청됨")
                    } catch (e: Exception) {
                        msg = "❌ 모듈 제거 실패: ${e.message}"
                        Log.e("WalletTest", "모듈 제거 중 예외 발생", e)
                    }
                }
            ) { Text("ETH 모듈 제거 (테스트용)") }
        }

        Spacer(Modifier.height(24.dp))

        // 기존 데모 콘텐츠
        Text(
            text = "Hello Main",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        ScrollableContent()
    }
}

// 바인딩 헬퍼 함수
private fun bindEthModule(
    walletSvc: WalletServiceHelper,
    updateMsg: (String) -> Unit
) {
    try {
        Log.d("WalletTest", "바인딩 호출 시작")
        walletSvc.bindModule("eth") { eth ->
            try {
                Log.d("WalletTest", "바인딩 성공, createAccount 호출")
                val addr = eth.createAccount("stub")
                Log.d("WalletTest", "계정 생성 완료: $addr")
                updateMsg("🎉 바인딩 성공 → $addr")
            } catch (e: Exception) {
                Log.e("WalletTest", "계정 생성 중 예외 발생", e)
                updateMsg("❌ 계정 생성 실패: ${e.message}")
            }
        }
    } catch (e: Exception) {
        Log.e("WalletTest", "바인딩 중 예외 발생", e)
        updateMsg("❌ 바인딩 실패: ${e.message}")
    }
}