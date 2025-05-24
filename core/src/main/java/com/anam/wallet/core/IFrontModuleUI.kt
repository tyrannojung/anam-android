package com.anam.wallet.core

import androidx.compose.runtime.Composable

/**
 * 별도 프로세스에서 실행되는 Front Module의 UI 인터페이스
 * Surface를 통해 메인앱에 렌더링됨
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
    val parameters: Map<String, Any> = emptyMap(),
    val surfaceCallback: SurfaceCallback? = null,
    val navigationCallback: NavigationCallback? = null
)

/**
 * Surface 관련 콜백
 */
interface SurfaceCallback {
    fun requestSurfaceResize(width: Int, height: Int)
    fun requestFullscreen(enable: Boolean)
}

/**
 * 네비게이션 관련 콜백
 */
interface NavigationCallback {
    fun requestNavigation(destination: String)
    fun requestClose()
}