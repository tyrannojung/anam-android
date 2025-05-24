package com.anam.wallet.service

import android.util.Log
import com.anam.wallet.core.IMainApp

private const val TAG = "SimpleMainAppService"

/**
 * 단순화된 MainAppService - Service 없이 직접 인스턴스로 사용
 * VP(Verifiable Presentation) 생성 기능 제공
 */
class SimpleMainAppService : IMainApp {
    
    override fun requestVP(challenge: String, presentationDefinition: String): String {
        Log.d(TAG, "VP 생성 요청")
        Log.d(TAG, "Challenge: $challenge")
        Log.d(TAG, "Presentation Definition: $presentationDefinition")
        
        // TODO: 실제 VP 생성 로직 구현
        // 1. presentationDefinition 파싱
        // 2. 요구되는 credential 확인
        // 3. 사용자에게 동의 요청 UI 표시
        // 4. VP 생성 및 서명
        
        // 현재는 테스트용 VP 반환
        return """
        {
            "@context": [
                "https://www.w3.org/2018/credentials/v1",
                "https://www.w3.org/2018/credentials/examples/v1"
            ],
            "type": ["VerifiablePresentation"],
            "verifiableCredential": [{
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://www.w3.org/2018/credentials/examples/v1"
                ],
                "type": ["VerifiableCredential", "IdentityCredential"],
                "credentialSubject": {
                    "id": "did:example:user123",
                    "name": "테스트 사용자",
                    "birthDate": "1990-01-01"
                },
                "issuer": "did:example:issuer456",
                "issuanceDate": "2024-01-01T00:00:00Z"
            }],
            "holder": "did:example:user123",
            "proof": {
                "type": "Ed25519Signature2018",
                "created": "2024-01-01T00:00:00Z",
                "challenge": "$challenge",
                "verificationMethod": "did:example:user123#key1",
                "proofPurpose": "authentication"
            }
        }
        """.trimIndent()
    }
}