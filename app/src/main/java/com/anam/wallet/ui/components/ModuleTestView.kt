package com.anam.wallet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 모듈 테스트 정보를 표시하는 컴포저블
 */
@Composable
fun ModuleTestView(
    moduleSummary: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // 카드 제목
            Text(
                text = "모듈 테스트 결과",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 모듈 정보 표시
            if (moduleSummary.containsKey("error")) {
                // 오류 메시지 표시
                Text(
                    text = moduleSummary["error"] ?: "알 수 없는 오류",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // 모듈 이름
                InfoRow(
                    label = "이름",
                    value = moduleSummary["name"] ?: "-"
                )
                
                // 모듈 심볼
                InfoRow(
                    label = "심볼",
                    value = moduleSummary["symbol"] ?: "-"
                )
                
                // 계정 수
                val accounts = moduleSummary["accounts"] ?: "{}"
                val accountCount = try {
                    val accountsJson = org.json.JSONArray(accounts)
                    accountsJson.length()
                } catch (e: Exception) {
                    0
                }
                
                InfoRow(
                    label = "계정 수",
                    value = accountCount.toString()
                )
                
                // 네트워크 정보
                val networkInfo = moduleSummary["networkInfo"] ?: "{}"
                val networkName = try {
                    val networkJson = org.json.JSONObject(networkInfo)
                    networkJson.optString("name", "-")
                } catch (e: Exception) {
                    "-"
                }
                
                InfoRow(
                    label = "네트워크",
                    value = networkName
                )
            }
        }
    }
}

/**
 * 정보 행 컴포저블
 */
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}