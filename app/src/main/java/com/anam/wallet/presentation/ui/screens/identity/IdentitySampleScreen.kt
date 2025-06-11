package com.anam.wallet.presentation.ui.screens.identity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam.wallet.R
import com.anam.wallet.LocalNavController

@Composable
fun IdentitySampleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 상단 타이틀
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "내 신분증",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
        
        // 카드 리스트
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 고려대학교 학생증 카드
            StudentCard()
            
            // 추후 다른 카드들이 추가될 공간
        }
    }
}

@Composable
private fun StudentCard() {
    val navController = LocalNavController.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                navController.navigate("StudentCardDetail")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Column {
            // 카드 헤더
            StudentCardHeader()
            
            // 카드 바디
            StudentCardBody()
        }
    }
}

@Composable
private fun StudentCardHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8B1538),
                        Color(0xFFA91E3E)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 배경 로고 (투명도 적용)
        Image(
            painter = painterResource(id = R.drawable.korea_university_logo),
            contentDescription = "Korea University Logo",
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp)
                .alpha(0.1f),
            contentScale = ContentScale.Fit
        )
        
        // 대학 정보
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = "고려대학교",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "정보보호대학원",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StudentCardBody() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 사진
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "프로필 사진",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(15.dp))
        
        // 학생 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "정다운",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "2023572504",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "금융보안학과 · 블록체인전공",
                fontSize = 13.sp,
                color = Color(0xFF888888)
            )
        }
        
        // 유효 배지
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE8F5E9)
        ) {
            Text(
                text = "유효",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
        }
    }
}