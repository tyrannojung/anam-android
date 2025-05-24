package com.anam.wallet.core

/**
 * 메인앱이 프론트 모듈에게 제공하는 인터페이스
 * VP(Verifiable Presentation) 생성 기능 제공
 */
interface IMainApp {
    /**
     * VP(Verifiable Presentation) 생성 요청
     * @param challenge 챌린지 문자열
     * @param presentationDefinition 프레젠테이션 정의 (JSON)
     * @return 생성된 VP (JSON 문자열)
     */
    fun requestVP(challenge: String, presentationDefinition: String): String
}