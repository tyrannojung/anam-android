package com.anam.wallet.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anam.wallet.R

@Composable
fun BottomNavBar(
    navController: NavController,
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    // NavigationItem 데이터 클래스
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationItems.forEach { navItem ->
                val isSelected = navItem.route == currentScreen
                
                // 애니메이션 값들
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )
                
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "iconColor"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
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
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) navItem.selectedIcon else navItem.icon,
                            contentDescription = stringResource(navItem.labelResId),
                            modifier = Modifier.size(26.dp),
                            tint = iconColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(navItem.labelResId),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.sp
                        )
                    }
                    
                    // 선택된 아이템 인디케이터
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(32.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}