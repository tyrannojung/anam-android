package com.anam.wallet.presentation.ui.screens.identity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam.wallet.R

@Composable
fun StudentCardDetailScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 학생증 카드
        Card(
            modifier = Modifier
                .width(350.dp)
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Column {
                // 카드 헤더
                CardDetailHeader()
                
                // 카드 바디
                CardDetailBody()
            }
        }
    }
}

@Composable
private fun CardDetailHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8B1538),
                        Color(0xFFA91E3E)
                    )
                )
            )
            .padding(25.dp)
    ) {
        // 배경 로고
        Image(
            painter = painterResource(id = R.drawable.korea_university_logo),
            contentDescription = "Korea University Logo",
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-30).dp)
                .rotate(-15f)
                .alpha(0.1f),
            contentScale = ContentScale.Fit
        )
        
        // 새로고침 아이콘
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "새로고침",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .clickable { /* 새로고침 액션 */ },
            tint = Color.White
        )
        
        // 대학 정보
        Column {
            Text(
                text = "정보보호대학원 학생증",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(5.dp))
            
            Text(
                text = "고려대학교",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CardDetailBody() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 이미지
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "프로필 사진",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 학생 이름
        Text(
            text = "정다운",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(5.dp))
        
        // 학번
        Text(
            text = "2023572504",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 학과 정보 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider(color = Color(0xFFF0F0F0))
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "금융보안학과",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF888888)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "블록체인전공",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF888888)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Divider(color = Color(0xFFF0F0F0))
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // QR 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // QR 코드
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qr_code),
                    contentDescription = "QR 코드",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            // QR 텍스트
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.clickable { /* 크게보기 액션 */ },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "➕ ",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "크게보기",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Spacer(modifier = Modifier.height(5.dp))
                
                Text(
                    text = "QR을 태하여\n카메라에 스캔하세요.",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    textAlign = TextAlign.End,
                    lineHeight = 18.sp
                )
            }
        }
    }
}