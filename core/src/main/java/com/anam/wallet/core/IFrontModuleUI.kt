package com.anam.wallet.core

import androidx.compose.runtime.Composable

/**
 * Front Module의 UI 인터페이스
 * 메인앱에서 직접 렌더링됨
 */
interface IFrontModuleUI {
    
    /**
     * 프론트 모듈의 메인 화면
     * @param context 프론트 모듈 실행 컨텍스트
     */
    @Composable
    fun FrontModuleScreen(context: FrontModuleContext)
    
    /**
     * 메인앱 서비스 연결 설정
     * IMainApp 인터페이스를 통해 메인앱의 기능 사용 가능
     */
    fun setMainAppService(service: IMainApp)
    
    /**
     * 모듈 시작 시 호출
     */
    fun onModuleStart()
    
    /**
     * 모듈 정지 시 호출
     */
    fun onModuleStop()
    
    /**
     * 모듈 종료 시 호출
     */
    fun onModuleDestroy()
}

/**
 * 프론트 모듈 실행 컨텍스트
 */
data class FrontModuleContext(
    val moduleId: String,
    val parameters: Map<String, Any> = emptyMap()
)