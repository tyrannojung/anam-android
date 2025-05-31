package com.anam.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    // NavigationItem 데이터 클래스 - Material Icons 사용
    data class NavigationItem(
        val route: String,
        val icon: ImageVector,
        val selectedIcon: ImageVector,
        val labelResId: Int
    )

    val navigationItems = listOf(
        NavigationItem(
            route = "Main",
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            labelResId = R.string.nav_main
        ),
        NavigationItem(
            route = "Hub",
            icon = Icons.Outlined.Hub,
            selectedIcon = Icons.Filled.Hub,
            labelResId = R.string.nav_hub
        ),
        NavigationItem(
            route = "Browser",
            icon = Icons.Outlined.Language,
            selectedIcon = Icons.Filled.Language,
            labelResId = R.string.nav_browser
        ),
        NavigationItem(
            route = "Identity",
            icon = Icons.Outlined.QrCode,
            selectedIcon = Icons.Filled.QrCode,
            labelResId = R.string.nav_did
        ),
        NavigationItem(
            route = "Settings",
            icon = Icons.Outlined.Settings,
            selectedIcon = Icons.Filled.Settings,
            labelResId = R.string.nav_settings
        )
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
                .background(AnamMediumGray)
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationItems.forEach { navItem ->
                val isSelected = navItem.route == currentScreen
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Remove ripple effect
                        ) {
                            onScreenSelected(navItem.route)
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) navItem.selectedIcon else navItem.icon,
                            contentDescription = stringResource(navItem.labelResId),
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) Color.White else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(navItem.labelResId),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}