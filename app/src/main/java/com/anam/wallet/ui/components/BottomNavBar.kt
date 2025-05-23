package com.anam.wallet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anam.wallet.R
import com.anam.wallet.ui.theme.AnamDarkGray
import com.anam.wallet.ui.theme.AnamMediumGray

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
        NavItem("Browser", R.string.nav_browser, R.string.nav_browser),
        NavItem("Activity", R.string.nav_activity, R.string.nav_activity),
        NavItem("Identity", R.string.nav_did, R.string.nav_did),
        NavItem("Hub", R.string.nav_hub, R.string.nav_hub)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(AnamDarkGray)
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(AnamMediumGray),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { navItem ->
                val isSelected = navItem.screenId == currentScreen
                val iconRes = when (navItem.screenId) {
                    "Main" -> if (isSelected) R.drawable.menu_home_on else R.drawable.menu_home_off
                    "Browser" -> if (isSelected) R.drawable.menu_webview_on else R.drawable.menu_webview_off
                    "Activity" -> if (isSelected) R.drawable.menu_transaction_on else R.drawable.menu_transaction_off
                    "Identity" -> if (isSelected) R.drawable.menu_qr_on else R.drawable.menu_qr_off
                    "Hub" -> if (isSelected) R.drawable.menu_setting_on else R.drawable.menu_setting_off
                    else -> if (isSelected) R.drawable.menu_home_on else R.drawable.menu_home_off
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onScreenSelected(navItem.screenId)
                            navController.navigate(navItem.screenId) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(navItem.contentDescResId),
                        modifier = Modifier.fillMaxSize(0.8f)
                    )
                }
            }
        }
    }
}