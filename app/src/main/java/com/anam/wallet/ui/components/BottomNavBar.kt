package com.anam.wallet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.anam.wallet.R

@Composable
fun BottomNavBar(
    navController: NavController,
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    // 화면 ID와 리소스 ID를 매핑
    data class NavItem(val screenId: String, val labelResId: Int, val contentDescResId: Int)

    val navItems = listOf(
        NavItem("Main", R.string.nav_main, R.string.nav_main),
        NavItem("City", R.string.nav_city, R.string.nav_city),
        NavItem("Hub", R.string.nav_hub, R.string.nav_hub),
        NavItem("Browser", R.string.nav_browser, R.string.nav_browser),
        NavItem("Activity", R.string.nav_activity, R.string.nav_activity)
    )

    NavigationBar {
        navItems.forEach { navItem ->
            NavigationBarItem(
                icon = {
                    Icon(
                        when (navItem.screenId) {
                            "Main" -> Icons.Default.Home
                            "City" -> Icons.Default.LocationCity
                            "Hub" -> Icons.Default.AccountBalance
                            "Browser" -> Icons.Default.Language
                            else -> Icons.Default.Insights
                        },
                        // 화면 읽기 도구(Screen Reader) 가 아이콘이나 이미지 같은 시각적 요소를 설명해줄 수 있도록 돕는 속성
                        //"홈 아이콘" 처럼 말로 읽어주는 역할을 함
                        contentDescription = stringResource(navItem.contentDescResId)
                    )
                },
                label = { Text(stringResource(navItem.labelResId)) },
                // selected가 true이면 자동으로 색이 바(선택된 상태로 표시)
                selected = navItem.screenId == currentScreen,
                onClick = {
                    onScreenSelected(navItem.screenId)
                    navController.navigate(navItem.screenId) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}