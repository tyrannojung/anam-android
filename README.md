# 안암월렛 (AnamWallet)

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="AnamWallet Logo" width="120"/>
</p>

<p align="center">
  <strong>모듈러 블록체인 슈퍼앱 - 하나의 지갑으로 모든 블록체인을</strong>
</p>

## 개요

안암월렛은 여러 블록체인 지갑을 하나로 통합한 모듈러 슈퍼앱입니다. 사용자는 더 이상 체인별로 다른 지갑을 설치할 필요가 없으며, 개발자는 허브를 통해 새로운 블록체인 모듈을 추가할 수 있습니다.

### 핵심 특징

- 🔧 **모듈러 아키텍처**: 블록체인별 독립 모듈로 무한 확장 가능
- 🔒 **프로세스 격리**: 각 블록체인이 독립 프로세스에서 실행되어 보안성 극대화
- 📱 **미니앱 생태계**: 정부24 등 다양한 서비스를 지갑 내에서 직접 이용
- 🆔 **DID/VC 통합**: W3C 표준 기반 탈중앙화 신원 인증
- ⚡ **백그라운드 실행**: 블록체인 모듈이 백그라운드에서 동작하여 빠른 트랜잭션 처리

## 아키텍처

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Android System                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────────────┐     ┌─────────────────────────┐      │
│  │   Main Process          │     │  :blockchain Process     │      │
│  │   (com.anam.wallet)     │     │  (Background Service)    │      │
│  ├─────────────────────────┤     ├─────────────────────────┤      │
│  │ • 미니앱 (정부24 등)     │ IPC │ • Ethereum Module        │      │
│  │ • DID/VC 관리          │<--->│ • Bitcoin Module         │      │
│  │ • UI Components        │ AIDL│ • Sui Module            │      │
│  │ • 미니앱 마켓플레이스    │     │ • 확장 가능...           │      │
│  └─────────────────────────┘     └─────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────┘
```

### 주요 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose
- **통신**: AIDL (Android Interface Definition Language)
- **보안**: WebViewAssetLoader, EncryptedSharedPreferences
- **미니앱**: HTML/CSS/JavaScript + Native Bridge

## 프로젝트 구조

```
anam-android/
├── app/                                # 메인 애플리케이션
│   ├── src/main/
│   │   ├── aidl/                      # 프로세스 간 통신 인터페이스
│   │   │   └── com/anam/wallet/blockchain/
│   │   │       ├── api/               # 퍼블릭 블록체인 API
│   │   │       └── internal/          # 내부 블록체인 매니저
│   │   │
│   │   ├── assets/
│   │   │   └── miniapps/              # 미니앱 패키지 (.zip)
│   │   │
│   │   ├── java/com/anam/wallet/
│   │   │   ├── blockchain/            # 블록체인 서비스 및 UI
│   │   │   │   ├── BlockchainService.kt
│   │   │   │   └── BlockchainUIActivity.kt
│   │   │   │
│   │   │   ├── crypto/                # 암호화 및 키 관리
│   │   │   │   ├── KeyManager.kt
│   │   │   │   └── SecureStorage.kt
│   │   │   │
│   │   │   ├── miniapp/               # 미니앱 프레임워크
│   │   │   │   ├── MiniAppManager.kt
│   │   │   │   ├── MiniAppJavaScriptBridge.kt
│   │   │   │   └── AssetLoaderWebViewClient.kt
│   │   │   │
│   │   │   ├── model/                 # 데이터 모델
│   │   │   │   ├── Identity.kt
│   │   │   │   └── VerifiableCredential.kt
│   │   │   │
│   │   │   ├── network/               # API 클라이언트
│   │   │   │   └── DIDApiService.kt
│   │   │   │
│   │   │   └── ui/                    # UI 컴포넌트
│   │   │       ├── screens/
│   │   │       │   ├── main/          # 메인 화면
│   │   │       │   └── identity/      # DID/VC 화면
│   │   │       └── theme/             # 테마 설정
│   │   │
│   │   └── res/                       # 리소스 파일
│   │
│   └── build.gradle.kts               # 앱 빌드 설정
│
├── example/                           # 예제 미니앱 및 블록체인 모듈
│   ├── apps/                         # 미니앱 예제
│   │   └── government24/             # 정부24 미니앱
│   └── blockchains/                  # 블록체인 모듈 예제
│       ├── ethereum/
│       └── bitcoin/
│
├── gradle/                           # Gradle 설정
└── build.gradle.kts                 # 프로젝트 빌드 설정
```

## 시작하기

### 요구사항

- Android Studio Arctic Fox 이상
- JDK 17
- Android SDK 24 이상 (Android 7.0+)

### 빌드 및 실행

```bash
# 저장소 클론
git clone https://github.com/ANAM145/anam-android.git
cd anam-android

# 디버그 빌드
./gradlew assembleDebug

# 앱 설치
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 미니앱 개발

1. 미니앱 구조 생성:
```bash
cd example/apps
mkdir my-miniapp
cd my-miniapp
```

2. 필수 파일 작성:
- `manifest.json`: 미니앱 메타데이터
- `pages/index/index.html`: 메인 페이지
- `app.js`: JavaScript 브릿지 API 사용

3. 패키징:
```bash
cd example
./build-miniapps.sh
```

## 주요 기능

### 1. 블록체인 모듈 시스템
- 각 블록체인이 독립된 프로세스에서 실행
- 백그라운드 서비스로 상시 대기
- AIDL을 통한 안전한 통신

### 2. 미니앱 플랫폼
- WebView 기반 경량 앱
- Native API 접근 가능
- 도메인별 스토리지 격리

### 3. DID/VC (탈중앙화 신원)
- W3C 표준 준수
- 자기주권 신원 (Self-Sovereign Identity)
- Verifiable Presentation (VP) 지원

### 4. 보안 특징
- 프로세스 레벨 격리
- 암호화된 로컬 스토리지
- 생체 인증 지원
- WebViewAssetLoader를 통한 안전한 리소스 로딩

## JavaScript Bridge API

미니앱에서 사용 가능한 Native API:

```javascript
// DID 인증 요청
window.anam.requestVP({
    challenge: "unique_challenge",
    presentationDefinition: { /* W3C 형식 */ }
});

// 결제 요청
window.anam.requestPayment({
    to: "0x...",
    amount: "0.001",
    blockchain: "ethereum"
});

// 로컬 스토리지
window.anam.setStorageItem("key", "value");
const value = window.anam.getStorageItem("key");

// 시스템 정보
const info = JSON.parse(window.anam.getSystemInfo());
```

## 기여하기

안암월렛은 모듈러 생태계를 함께 만들어갈 개발자를 환영합니다.

### 블록체인 모듈 추가
1. `example/blockchains/` 참고하여 모듈 개발
2. AIDL 인터페이스 구현
3. Pull Request 제출

### 미니앱 개발
1. `example/apps/` 참고하여 미니앱 개발
2. 마켓플레이스에 등록 (준비 중)

## 라이선스

Copyright © 2025 ANAM145. All rights reserved.

이 소프트웨어는 저작권법 및 국제 저작권 조약에 의해 보호됩니다. 
상업적 사용을 위해서는 별도의 라이선스가 필요합니다.

교육 및 연구 목적의 사용은 출처를 명시하는 조건으로 허용됩니다.

## 연락처

- 웹사이트: [https://anam145.com](https://anam145.com)
- 이메일: contact@anam145.com
- 이슈 트래커: [GitHub Issues](https://github.com/ANAM145/anam-android/issues)

---

<p align="center">
  Built with ❤️ by ANAM145
</p>