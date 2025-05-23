package com.anam.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
            .padding(20.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            containerColor = AnamMediumGray
        ) {
            navItems.forEach { navItem ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            when (navItem.screenId) {
                                "Main" -> Icons.Default.Home
                                "Identity" -> Icons.Default.Badge
                                "Hub" -> Icons.Default.AccountBalance
                                "Browser" -> Icons.Default.Language
                                else -> Icons.Default.Insights
                            },
                            contentDescription = stringResource(navItem.contentDescResId)
                        )
                    },
                    label = { Text(stringResource(navItem.labelResId)) },
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
}