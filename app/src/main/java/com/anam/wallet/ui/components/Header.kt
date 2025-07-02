package com.anam.wallet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam.wallet.R
import com.anam.wallet.miniapp.MiniAppManager
import com.anam.wallet.miniapp.MiniAppScanner
import com.anam.wallet.model.miniapp.ScannedMiniApp

@Composable
fun Header(title: String? = null) {
    val context = LocalContext.current
    val miniAppManager = remember { MiniAppManager.getInstance(context) }
    val miniAppScanner = remember { MiniAppScanner(context) }
    
    // 활성 블록체인 ID 관찰
    val activeBlockchainId by miniAppManager.activeBlockchain.collectAsState()
    
    // 활성 블록체인 정보 가져오기
    var activeBlockchain by remember { mutableStateOf<ScannedMiniApp?>(null) }
    
    LaunchedEffect(activeBlockchainId) {
        android.util.Log.d("Header", "Active blockchain ID changed to: $activeBlockchainId")
        activeBlockchainId?.let { id ->
            try {
                val scannedApps = miniAppScanner.scanInstalledApps()
                activeBlockchain = scannedApps.find { it.appId == id }
                android.util.Log.d("Header", "Found active blockchain: ${activeBlockchain?.name}")
            } catch (e: Exception) {
                android.util.Log.e("Header", "Error scanning apps", e)
                activeBlockchain = null
            }
        } ?: run {
            android.util.Log.d("Header", "Active blockchain ID is null")
            activeBlockchain = null
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // 제목
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title ?: stringResource(R.string.header_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,  // 32sp에서 24sp로 축소
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // 블록체인 상태 칩
        AnimatedVisibility(
            visible = activeBlockchain != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .height(32.dp)
                        .clickable { 
                            // 클릭 시 활성 블록체인 화면으로 이동
                            activeBlockchainId?.let { id ->
                                val intent = android.content.Intent(
                                    context, 
                                    com.anam.wallet.blockchain.BlockchainUIActivity::class.java
                                ).apply {
                                    putExtra(com.anam.wallet.blockchain.BlockchainUIActivity.EXTRA_BLOCKCHAIN_ID, id)
                                }
                                context.startActivity(intent)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔗",
                            fontSize = 14.sp
                        )
                        Text(
                            text = " ${activeBlockchain?.name ?: ""} 활성화됨",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}