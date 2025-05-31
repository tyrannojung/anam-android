package com.anam.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anam.wallet.ui.components.BottomNavBar
import com.anam.wallet.ui.components.Header
import com.anam.wallet.ui.screens.activity.ActivityScreen
import com.anam.wallet.ui.screens.hub.SimpleHubScreen
import com.anam.wallet.ui.screens.browser.BrowserScreen
import com.anam.wallet.ui.screens.main.MainScreen
import com.anam.wallet.ui.theme.AnamwalletTheme

// Create a CompositionLocal for NavController
val LocalNavController = staticCompositionLocalOf<NavController> { 
    error("NavController not provided") 
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AnamwalletTheme {
                WalletApp()
            }
        }
    }
}

@Composable
fun WalletApp() {
    // Jetpack Navigation Compose에서 사용하는 내비게이션 컨트롤러 생성 함수
    // remember가 내부적으로 포함돼 있어, Composable이 리컴포지션(화면 갱신)되어도 navController 객체가 유지됨
    val navController = rememberNavController()
    // remember는 컴포저블 함수가 다시 호출되더라도 값이 유지되게 해줌
    // mutableStateOf는 상태 값을 만듦. 이 값이 바뀌면 관련된 UI도 자동으로 다시 그려짐
    // 여기에 by를 붙여서 currentScreen이라는 일반 변수처럼 쓸 수 있게 함
    var currentScreen by remember { mutableStateOf("Main") }

    CompositionLocalProvider(LocalNavController provides navController) {
        Scaffold(
            topBar = { 
                // Don't show header for Browser screen or Detail screen
                if (currentScreen != "Browser") {
                    Header() 
                }
            },
            bottomBar = {
                // Always show bottom navigation (including Detail screen)
                BottomNavBar(
                    navController = navController,
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "Main",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("Main") {
                    MainScreen()
                }
                composable("Identity") {
                    com.anam.wallet.ui.screens.identity.IdentityScreen()
                }
                composable("Hub") {
                    SimpleHubScreen()
                }
                composable("Browser") {
                    BrowserScreen()
                }
                composable("Activity") {
                    ActivityScreen()
                }
            }
        }
    }
}