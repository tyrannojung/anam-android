package com.anam.wallet.presentation.ui.screens.hub

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam.wallet.model.hub.ModuleDetailData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HubUiState(
    val isLoading: Boolean = false,
    val recommendedModules: List<ModuleDetailData> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HubViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(HubUiState())
    val uiState: StateFlow<HubUiState> = _uiState.asStateFlow()
    
    init {
        loadRecommendedModules()
    }
    
    private fun loadRecommendedModules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: 실제로는 서버에서 추천 모듈을 가져와야 함
            val modules = listOf(
                ModuleDetailData(
                    id = "gov24",
                    name = "정부24",
                    description = "대한민국 정부의 모든 행정 서비스를 한 곳에서",
                    longDescription = """
                        정부24는 대한민국 정부가 제공하는 통합 전자정부 서비스입니다.
                        
                        주민등록등본, 초본부터 각각 증명서까지 정부 서류를 언제 어디서나 간편하게 발급받을 수 있습니다.
                        
                        • 365일 24시간 이용 가능
                        • 1,300여 종의 정부 서비스 제공
                    """.trimIndent(),
                    developer = "대한민국 정부",
                    version = "2.0.0",
                    size = "15MB",
                    category = "행정",
                    rating = 4.5f,
                    downloads = "1,000,000+",
                    totalRatings = 50000,
                    lastUpdated = "2024년 1월 15일",
                    icon = androidx.compose.material.icons.Icons.Default.AccountBalance,
                    primaryColor = androidx.compose.ui.graphics.Color(0xFF003764),
                    screenshots = listOf("screenshot1", "screenshot2", "screenshot3")
                )
            )
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                recommendedModules = modules
            )
        }
    }
}