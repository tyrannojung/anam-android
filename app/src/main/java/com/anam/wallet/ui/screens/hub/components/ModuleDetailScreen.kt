package com.anam.wallet.ui.screens.hub.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anam.wallet.R
import com.anam.wallet.model.hub.ModuleDetailData
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    navController: NavController,
    moduleId: String? = null
) {
    val scrollState = rememberScrollState()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloaded by remember { mutableStateOf(false) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "downloadProgress"
    )
    
    // 정부24 예제 데이터
    val moduleData = remember {
        ModuleDetailData(
            id = "gov24",
            name = "정부24",
            description = "대한민국 정부의 모든 행정 서비스를 한 곳에서",
            longDescription = """
                정부24는 대한민국 정부가 제공하는 통합 전자정부 서비스입니다.
                
                주민등록등본, 초본부터 각종 증명서까지 정부 서류를 언제 어디서나 간편하게 발급받을 수 있습니다.
                
                • 365일 24시간 이용 가능
                • 1,300여 종의 정부 서비스 제공
                • 안전한 본인 인증 시스템
                • 모바일 최적화 서비스
            """.trimIndent(),
            category = "공공서비스",
            developer = "행정안전부",
            version = "2.3.1",
            size = "45 MB",
            downloads = "100K+",
            rating = 4.7f,
            totalRatings = 23456,
            lastUpdated = "2024년 1월 15일",
            icon = Icons.Filled.AccountBalance,
            primaryColor = Color(0xFF1976D2),
            screenshots = listOf() // 스크린샷은 실제 구현시 추가
        )
    }
    
    LaunchedEffect(isDownloading) {
        if (isDownloading) {
            for (i in 0..100 step 5) {
                downloadProgress = i / 100f
                delay(50)
            }
            isDownloading = false
            isDownloaded = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // 헤더 섹션
            HeaderSection(moduleData)
            
            // 다운로드 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                DownloadButton(
                    isDownloading = isDownloading,
                    isDownloaded = isDownloaded,
                    progress = animatedProgress,
                    onClick = {
                        if (!isDownloaded && !isDownloading) {
                            isDownloading = true
                        }
                    }
                )
            }
            
            // 설명
            DescriptionSection(moduleData.longDescription)
            
            // 상세 정보
            DetailInfoSection(moduleData)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderSection(moduleData: ModuleDetailData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아이콘
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            moduleData.primaryColor.copy(alpha = 0.8f),
                            moduleData.primaryColor
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = moduleData.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 이름
        Text(
            text = moduleData.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // 개발자
        Text(
            text = moduleData.developer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 카테고리 태그
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = moduleData.category,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 다운로드 및 크기 정보
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 다운로드 수
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = moduleData.downloads,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "다운로드",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            
            // 용량
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = moduleData.size,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "크기",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DownloadButton(
    isDownloading: Boolean,
    isDownloaded: Boolean,
    progress: Float,
    onClick: () -> Unit
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isDownloading) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isDownloaded -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.primary
            }
        ),
        enabled = !isDownloading
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 프로그레스 배경
            if (isDownloading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Color.White.copy(alpha = 0.2f)
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Color.White.copy(alpha = 0.3f)
                        )
                )
            }
            
            // 텍스트와 아이콘
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        isDownloaded -> Icons.Filled.CheckCircle
                        isDownloading -> Icons.Filled.Downloading
                        else -> Icons.Filled.Download
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isDownloaded -> "설치 완료"
                        isDownloading -> "${(progress * 100).toInt()}% 다운로드 중..."
                        else -> "다운로드"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}


@Composable
private fun DetailInfoSection(moduleData: ModuleDetailData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "앱 정보",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            InfoRow("버전", moduleData.version)
            InfoRow("업데이트", moduleData.lastUpdated)
            InfoRow("개발자", moduleData.developer)
            InfoRow("크기", moduleData.size)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "설명",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(20.dp),
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}