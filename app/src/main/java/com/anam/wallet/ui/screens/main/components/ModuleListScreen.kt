package com.anam.wallet.ui.screens.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import com.anam.wallet.LocalNavController
import com.anam.wallet.R
import com.anam.wallet.blockchain.BlockchainUIActivity
import com.anam.wallet.miniapp.MiniAppManager
import com.anam.wallet.miniapp.MiniAppScanner
import com.anam.wallet.model.miniapp.ScannedMiniApp

@Composable
fun ModuleListScreen() {
    val scrollState = rememberScrollState()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val miniAppManager = remember { MiniAppManager.getInstance(context) }
    val miniAppScanner = remember { MiniAppScanner(context) }
    
    // 스캔된 미니앱 상태
    var scannedApps by remember { mutableStateOf<List<ScannedMiniApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 활성화된 블록체인은 이제 BlockchainUIActivity에서 관리
    
    // 미니앱 스캔
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            // 캐시 클리어 (디버깅용. 프로덕트에서 제거해야함)
            // 개발자가 미니앱을 수정했을 때
            // ex. manifest.json 변경
            // → 캐시 클리어하지 않으면 변경사항이 안 보임!
            // miniAppScanner.clearCache()
            
            android.util.Log.d("ModuleListScreen", "Starting mini app scan...")
            // 미니앱 스캔 시작!
            scannedApps = miniAppScanner.scanInstalledApps()
            android.util.Log.d("ModuleListScreen", "Scanned ${scannedApps.size} apps")
            
            // 블록체인 활성화는 이제 사용자가 클릭할 때만 진행
            
            isLoading = false
        } catch (e: Exception) {
            android.util.Log.e("ModuleListScreen", "Error scanning apps", e)
            isLoading = false
        }
    }
    
    // MiniAppManager에서 활성 블록체인 ID 관찰
    val activeBlockchainId by miniAppManager.activeBlockchain.collectAsState()
    
    // 디버깅을 위한 로그
    LaunchedEffect(activeBlockchainId) {
        android.util.Log.d("ModuleListScreen", "Active blockchain changed to: $activeBlockchainId")
    }
    
    // 타입별로 분류
    val blockchainModules = remember(scannedApps) {
        scannedApps.filter { it.type == "blockchain" }
    }
    
    val appModules = remember(scannedApps) {
        scannedApps.filter { it.type == "app" }
    }
    
    if (isLoading) {
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
                    activeModuleId = activeBlockchainId,
                    onModuleClick = { module ->
                        android.util.Log.d("ModuleListScreen", "Blockchain module clicked: ${module.appId}")
                        android.util.Log.d("ModuleListScreen", "Current active blockchain: $activeBlockchainId")
                        // Launch BlockchainUIActivity in blockchain process
                        val intent = Intent(context, BlockchainUIActivity::class.java).apply {
                            putExtra(BlockchainUIActivity.EXTRA_BLOCKCHAIN_ID, module.appId)
                        }
                        context.startActivity(intent)
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
        animationSpec = tween(300),
        label = "borderColor"
    )
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
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
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: 아이콘
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
            
            // 활성화 버튼 제거
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