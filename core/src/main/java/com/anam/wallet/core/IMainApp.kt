package com.anam.wallet.core

/**
 * 메인앱이 프론트 모듈에게 제공하는 인터페이스
 * VP(Verifiable Presentation) 생성 기능 제공
 */
interface IMainApp {
    /**
     * VP(Verifiable Presentation) 생성 요청
     * 사용자 인증 팝업을 표시하고, 승인 시 VP를 생성하여 반환
     * @param challenge 챌린지 문자열
     * @param presentationDefinition 프레젠테이션 정의 (JSON)
     * @param requesterName 요청한 앱/서비스 이름 (팝업에 표시용)
     * @return 생성된 VP (JSON 문자열), 사용자가 거부하면 null
     */
    suspend fun requestVP(
        challenge: String, 
        presentationDefinition: String,
        requesterName: String = "외부 서비스"
    ): String?
}