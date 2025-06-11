package com.anam.wallet.presentation.ui.screens.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anam.wallet.LocalNavController
import com.anam.wallet.R
import com.anam.wallet.model.miniapp.ScannedMiniApp

@Composable
fun ModuleListScreen(
    uiState: com.anam.wallet.presentation.ui.screens.main.MainUiState,
    onActivateBlockchain: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val scrollState = rememberScrollState()
    val navController = LocalNavController.current
    
    // 타입별로 분류
    val blockchainModules = remember(uiState.miniApps) {
        uiState.miniApps.filter { it.type == "blockchain" }
    }
    
    val appModules = remember(uiState.miniApps) {
        uiState.miniApps.filter { it.type == "app" }
    }
    
    if (uiState.isLoading) {
        // 로딩 상태
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            // 블록체인 섹션
            if (blockchainModules.isNotEmpty()) {
                BlockchainModuleSection(
                    title = stringResource(R.string.main_section_blockchain),
                    modules = blockchainModules,
                    activeModuleId = uiState.activeBlockchainId,
                    onActivateModule = onActivateBlockchain,
                    onModuleClick = { module ->
                        android.util.Log.d("ModuleListScreen", "Blockchain module clicked: ${module.appId}")
                        navController.navigate("miniapp/${module.appId}")
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 앱 섹션
            if (appModules.isNotEmpty()) {
                AppModuleSection(
                    title = stringResource(R.string.main_section_apps),
                    modules = appModules,
                    onModuleClick = { module ->
                        android.util.Log.d("ModuleListScreen", "App module clicked: ${module.appId}")
                        navController.navigate("miniapp/${module.appId}")
                    }
                )
            }
            
            // 더 추가하기 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                AddMoreCard(
                    onClick = { 
                        navController.navigate("Hub") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Bottom navigation 공간
        }
    }
}

@Composable
private fun BlockchainModuleSection(
    title: String,
    modules: List<ScannedMiniApp>,
    activeModuleId: String?,
    onActivateModule: (String) -> Unit,
    onModuleClick: (ScannedMiniApp) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(modules.size) { index ->
                BlockchainModuleCard(
                    module = modules[index],
                    isActive = modules[index].appId == activeModuleId,
                    onActivate = { onActivateModule(modules[index].appId) },
                    onClick = { onModuleClick(modules[index]) }
                )
            }
        }
    }
}

@Composable
private fun AppModuleSection(
    title: String,
    modules: List<ScannedMiniApp>,
    onModuleClick: (ScannedMiniApp) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        
        // 3개씩 그리드로 표시
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            modules.chunked(3).forEach { rowModules ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowModules.forEach { module ->
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            MiniAppCard(
                                module = module,
                                onClick = { onModuleClick(module) }
                            )
                        }
                    }
                    // 빈 공간 채우기 (3개 미만일 때)
                    repeat(3 - rowModules.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockchainModuleCard(
    module: ScannedMiniApp,
    isActive: Boolean,
    onActivate: () -> Unit,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "borderColor"
    )
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .scale(animatedScale)
            .clickable { onClick() }
            .then(
                if (isActive) {
                    Modifier.border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: 아이콘과 활성화 상태
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 아이콘 박스
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            module.primaryColor.copy(alpha = if (isActive) 0.15f else 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 동적 아이콘 로드 시도, 실패시 폴백 아이콘 사용
                    if (module.iconBitmap != null) {
                        Image(
                            bitmap = module.iconBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    } else if (module.fallbackIconRes != null) {
                        Image(
                            painter = painterResource(id = module.fallbackIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // 활성화 상태 표시
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.main_blockchain_active),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // 중간: 이름과 잔액
            Column {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (module.balance != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.balance,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 하단: 활성화 버튼
            if (!isActive) {
                Button(
                    onClick = { 
                        onActivate()
                        // 클릭 이벤트가 카드로 전파되지 않도록 함
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clickable(enabled = false) { }, // 클릭 이벤트 전파 방지
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.main_blockchain_activate),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // 활성화된 경우 빈 공간
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun MiniAppCard(
    module: ScannedMiniApp,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // 앱 모듈 - 작은 정사각형 카드
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(animatedScale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        module.primaryColor.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 동적 아이콘 로드 시도, 실패시 폴백 아이콘 사용
                if (module.iconBitmap != null) {
                    Image(
                        bitmap = module.iconBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (module.fallbackIconRes != null) {
                    Image(
                        painter = painterResource(id = module.fallbackIconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 앱 이름
            Text(
                text = module.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AddMoreCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = stringResource(R.string.main_add_more_modules),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}