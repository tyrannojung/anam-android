package com.anam.wallet.ui.screens.hub

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anam.wallet.LocalNavController
import com.anam.wallet.SimpleModuleManager
import com.anam.wallet.ui.components.ModuleTestView
import kotlinx.coroutines.launch

private const val TAG = "SimpleHubScreen"

/**
 * 단순화된 Hub 화면 컴포저블
 */
@Composable
fun SimpleHubScreen(
    moduleManager: SimpleModuleManager,
    viewModel: SimpleHubViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    
    // 상태 변수
    val uiState by viewModel.uiStateFlow.collectAsState()
    
    // 사용자 입력 모듈 ID 상태
    var moduleIdInput by remember { mutableStateOf("7") }
    
    // 최초 컴포지션 시 다운로드된 모듈 목록 새로고침
    LaunchedEffect(moduleManager) {
        Log.d(TAG, "LaunchedEffect: refreshing downloaded modules")
        viewModel.refreshDownloadedModules(moduleManager)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 제목
            Text(
                text = "모듈 테스트",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
            
            // 모듈 다운로드 섹션
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "프론트 모듈 다운로드",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 프론트 모듈 ID 입력 필드
                    OutlinedTextField(
                        value = moduleIdInput,
                        onValueChange = { moduleIdInput = it },
                        label = { Text("프론트 모듈 인덱스") },
                        placeholder = { Text("7, 8, 9 등 입력 (프론트 모듈만)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    // 다운로드 버튼
                    Button(
                        onClick = {
                            viewModel.clearStatusMessage()
                            coroutineScope.launch {
                                viewModel.downloadModule(moduleManager, moduleIdInput.trim())
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("프론트 모듈 다운로드 및 로드")
                    }
                    
                    // 다운로드 진행 상태
                    if (uiState.isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { uiState.progress.toFloat() / 100 },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Text(
                                text = "${uiState.progress}%",
                                modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // 상태 메시지
                    if (uiState.message.isNotEmpty()) {
                        Text(
                            text = uiState.message,
                            color = if (uiState.isSuccess) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // 다운로드된 모듈 목록 표시
            if (uiState.downloadedModules.isNotEmpty()) {
                Text(
                    text = "다운로드된 프론트 모듈",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Start
                )
                
                uiState.downloadedModules.forEach { moduleId ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // 모듈 기본 정보
                            Text(
                                text = "Front Module $moduleId",
                                fontSize = 18.sp,
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "모듈 ID: $moduleId",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            Text(
                                text = "상태: 다운로드 완료",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            // 버튼 영역 (삭제 + 상세 보기)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 삭제 버튼
                                OutlinedButton(
                                    onClick = {
                                        viewModel.deleteModule(moduleManager, moduleId)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("모듈 삭제")
                                }
                                
                                // 상세 보기 버튼
                                Button(
                                    onClick = {
                                        // 모듈 상세 화면으로 이동
                                        navController.navigate("moduleDetail/$moduleId")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("상세 보기")
                                }
                            }
                        }
                    }
                }
            } else if (!uiState.isLoading) {
                // 설치된 모듈이 없을 때 표시
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "설치된 프론트 모듈이 없습니다",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "다운로드 버튼을 눌러 프론트 모듈을 설치하세요",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}