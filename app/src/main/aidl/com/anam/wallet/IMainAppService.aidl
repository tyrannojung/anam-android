package com.anam.wallet;

/**
 * 메인앱이 프론트 모듈에게 제공하는 서비스 인터페이스
 * 프론트 모듈(Verifier)은 VP만 요청하면 됨
 */
interface IMainAppService {
    
    /**
     * VP(Verifiable Presentation) 생성 요청
     * @param challenge 챌린지 값 (nonce)
     * @param presentationDefinition VP 정의 (어떤 credential을 요구하는지)
     * @return VP JSON 문자열
     */
    String requestVP(String challenge, String presentationDefinition);
}