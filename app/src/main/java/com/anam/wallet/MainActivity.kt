package com.anam.wallet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anam.wallet.ui.components.BottomNavBar
import com.anam.wallet.ui.components.Header
import com.anam.wallet.ui.screens.activity.ActivityScreen
import com.anam.wallet.ui.screens.hub.SimpleHubScreen
import com.anam.wallet.ui.screens.browser.BrowserScreen
import com.anam.wallet.ui.screens.main.MainScreen
import com.anam.wallet.ui.screens.moduledetail.ModuleDetailScreen
import com.anam.wallet.ui.theme.AnamwalletTheme

private const val TAG = "MainActivity"

// Create a CompositionLocal for NavController
val LocalNavController = staticCompositionLocalOf<NavController> { 
    error("NavController not provided") 
}

// Create a shared/singleton moduleManager that persists across navigation
val moduleManager by lazy {
    // Will be properly initialized in WalletApp
    SimpleModuleManager(null)
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

    // 현재 컴포저블이 실행되는 Context 객체를 가져옴
    // 예: 파일 접근, 토스트 띄우기, ViewModel에 context 넘기기 등에서 씀
    // Android의 Activity나 Application의 context를 Compose에서 가져오는 방식.
    val context = LocalContext.current
    
    // Initialize the moduleManager with context if not already initialized
    if (moduleManager.context == null) {
        moduleManager.initializeContext(context)
        Log.d(TAG, "SimpleModuleManager initialized with context")
    }

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
                    // Use the shared moduleManager
                    SimpleHubScreen(moduleManager = moduleManager)
                }
                composable("Browser") {
                    BrowserScreen()
                }
                composable("Activity") {
                    ActivityScreen()
                }
                
                // Add module detail screen navigation
                composable(
                    route = "moduleDetail/{moduleId}",
                    // arguments 타입이 listOf이고, 여러개를 navArgument 줄 수 있음
                    arguments = listOf(navArgument("moduleId") { type = NavType.StringType })

                // backStackEntry란?	현재 네비게이션 스택에 들어온 경로의 상태 정보 객체
                // 이동했을 때 넘겨받은 경로 파라미터(argument) 등의 정보를 담고 있음
                ) { backStackEntry ->
                    val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                    
                    Log.d(TAG, "Entering ModuleDetailScreen for moduleId: $moduleId")
                    
                    // Use the shared moduleManager
                    ModuleDetailScreen(
                        moduleId = moduleId,
                        moduleManager = moduleManager,
                        onBackClick = { 
                            Log.d(TAG, "Back button pressed from ModuleDetailScreen")
                            navController.popBackStack() 
                        }
                    )
                    
                    // Update currentScreen to hide bottom navigation and header
                    // Jetpack Compose의 DisposableEffect 를 이용해 특정 화면에 진입했을 때와 벗어날 때 실행할 로직을 정의
                    // 해당 컴포저블이 화면에 들어왔을 때	currentScreen = "Detail" 로 설정 (헤더/하단바 숨김 용)
                    // 해당 컴포저블이 화면에서 사라질 때	currentScreen 을 이전 값으로 되돌림
                    // 트리에서 제거될 때(onDispose) 자동으로 정리 작업을 해주는 역할
                    DisposableEffect(Unit) {
                        val prevScreen = currentScreen
                        currentScreen = "Detail"
                        onDispose {
                            Log.d(TAG, "Leaving ModuleDetailScreen, restoring screen: $prevScreen")
                            currentScreen = prevScreen
                        }
                    }
                }
            }
        }
    }
}