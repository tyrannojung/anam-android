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
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.anam.wallet.R

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
    
    // 샘플 설치된 모듈 데이터
    val installedBlockchainModules = remember {
        listOf(
            InstalledModule(
                id = "btc",
                name = "Bitcoin",
                icon = Icons.Filled.CurrencyBitcoin,
                primaryColor = Color(0xFFF7931A),
                balance = "0.0234 BTC",
                value = "₩1,234,567"
            ),
            InstalledModule(
                id = "eth",
                name = "Ethereum",
                icon = Icons.Filled.AccountBalance,
                primaryColor = Color(0xFF627EEA),
                balance = "1.5 ETH",
                value = "₩3,456,789"
            ),
            InstalledModule(
                id = "sui",
                name = "Sui",
                icon = Icons.Filled.Speed,
                primaryColor = Color(0xFF4DA2FF),
                balance = "500 SUI",
                value = "₩567,890"
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
                icon = Icons.Filled.AccountBalance,
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
        ModuleSection(
            title = stringResource(R.string.main_section_blockchain),
            modules = installedBlockchainModules,
            onModuleClick = { module ->
                // TODO: 모듈 실행
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 앱 섹션
        ModuleSection(
            title = stringResource(R.string.main_section_apps),
            modules = installedAppModules,
            onModuleClick = { module ->
                // TODO: 모듈 실행
            }
        )
        
        // 더 추가하기 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            AddMoreCard(
                onClick = { /* TODO: Navigate to Hub */ }
            )
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Bottom navigation 공간
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
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(modules.size) { index ->
                MiniAppCard(
                    module = modules[index],
                    onClick = { onModuleClick(modules[index]) }
                )
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
                .size(100.dp)
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
                    Icon(
                        imageVector = module.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = module.primaryColor
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
    } else {
        // 블록체인 모듈 - 큰 직사각형 카드
        Card(
            modifier = Modifier
                .width(160.dp)
                .height(180.dp)
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 아이콘 박스
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            module.primaryColor.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = module.icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = module.primaryColor
                    )
                }
                
                // 컨텐츠
                Column {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (module.balance != null && module.value != null) {
                        Text(
                            text = module.balance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = module.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val primaryColor: Color,
    val balance: String? = null,
    val value: String? = null,
    val subtitle: String? = null,
    val isApp: Boolean = false
)