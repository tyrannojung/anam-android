package com.anam.wallet.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.anam.wallet.LocalNavController
import com.anam.wallet.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

@Composable
fun MainScreen() {
    // 모듈이 설치되어 있는지 체크하는 상태 (실제로는 ViewModel에서 관리해야 함)
    val hasModules by remember { mutableStateOf(true) }
    
    if (hasModules) {
        // 모듈이 있을 때의 화면 (추후 구현)
        ModuleListScreen()
    } else {
        // 모듈이 없을 때의 온보딩 화면
        EmptyModuleScreen()
    }
}

@Composable
private fun EmptyModuleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // 빈 상자 일러스트
        EmptyBoxIllustration()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 메인 메시지
        Text(
            text = stringResource(R.string.main_empty_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 서브 메시지
        Text(
            text = stringResource(R.string.main_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 설치 가이드 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.main_install_guide_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 스텝 1
                InstallStep(
                    number = "1",
                    icon = Icons.Filled.Hub,
                    text = stringResource(R.string.main_install_step1)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 스텝 2
                InstallStep(
                    number = "2",
                    icon = Icons.Filled.TouchApp,
                    text = stringResource(R.string.main_install_step2)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 스텝 3
                InstallStep(
                    number = "3",
                    icon = Icons.Filled.PlayArrow,
                    text = stringResource(R.string.main_install_step3)
                )
            }
        }
    }
}

@Composable
private fun EmptyBoxIllustration() {
    val animatedSize by animateDpAsState(
        targetValue = 120.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "boxSize"
    )
    
    Box(
        modifier = Modifier.size(animatedSize),
        contentAlignment = Alignment.Center
    ) {
        // 뒷면 상자
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 10.dp, y = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
        )
        
        // 메인 상자
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun InstallStep(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 번호 서클
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 아이콘과 텍스트
        Row(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ModuleListScreen() {
    val scrollState = rememberScrollState()
    val navController = LocalNavController.current
    
    // 활성화된 블록체인 모듈 ID 상태
    var activeBlockchainId by remember { mutableStateOf("eth") }
    
    // 샘플 설치된 모듈 데이터
    val installedBlockchainModules = remember {
        listOf(
            InstalledModule(
                id = "eth",
                name = "Ethereum",
                iconRes = R.drawable.ic_blockchain_ethereum,
                primaryColor = Color(0xFF627EEA),
                balance = "1.5 ETH"
            )
        )
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    val installedAppModules = remember {
        listOf(
            InstalledModule(
                id = "gov24",
                name = "정부24",
                iconRes = R.drawable.ic_blockchain_gov,
                primaryColor = Color(0xFF1976D2),
                isApp = true
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // 블록체인 섹션
        BlockchainModuleSection(
            title = stringResource(R.string.main_section_blockchain),
            modules = installedBlockchainModules,
            activeModuleId = activeBlockchainId,
            onActivateModule = { moduleId ->
                activeBlockchainId = moduleId
            },
            onModuleClick = { module ->
                // 모든 블록체인 모듈 클릭 시 ethereum 미니앱 실행
                navController.navigate("miniapp/ethereum")
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 앱 섹션
        ModuleSection(
            title = stringResource(R.string.main_section_apps),
            modules = installedAppModules,
            onModuleClick = { module ->
                // 모든 앱 모듈은 정부24로 연결
                navController.navigate("miniapp/government24")
            }
        )
        
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

@Composable
private fun BlockchainModuleSection(
    title: String,
    modules: List<InstalledModule>,
    activeModuleId: String,
    onActivateModule: (String) -> Unit,
    onModuleClick: (InstalledModule) -> Unit
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
                    isActive = modules[index].id == activeModuleId,
                    onActivate = { onActivateModule(modules[index].id) },
                    onClick = { onModuleClick(modules[index]) }
                )
            }
        }
    }
}

@Composable
private fun ModuleSection(
    title: String,
    modules: List<InstalledModule>,
    onModuleClick: (InstalledModule) -> Unit
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
    module: InstalledModule,
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
                    Image(
                        painter = painterResource(id = module.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
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
    module: InstalledModule,
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
    
    if (module.isApp == true) {
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
                    Image(
                        painter = painterResource(id = module.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
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

// 설치된 모듈 데이터 클래스
data class InstalledModule(
    val id: String,
    val name: String,
    val iconRes: Int,
    val primaryColor: Color,
    val balance: String? = null,
    val subtitle: String? = null,
    val isApp: Boolean = false
)