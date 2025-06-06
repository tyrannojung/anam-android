# 비단주머니 슈퍼앱 미니앱 아키텍처

## 1. 개요

비단주머니는 블록체인 지갑 기능을 기반으로 한 슈퍼앱으로, W3C MiniApp 표준을 따르는 미니앱 생태계를 지원합니다.

### 슈퍼앱이란?
- **정의**: 하나의 앱 안에서 여러 서비스를 제공하는 플랫폼 앱
- **예시**: WeChat, Alipay, Grab, Gojek
- **특징**: 메인 앱 안에서 미니앱들이 독립적으로 실행되며, 사용자는 앱을 나가지 않고 다양한 서비스 이용 가능

### 미니앱이란?
- **정의**: 슈퍼앱 내에서 실행되는 작은 애플리케이션
- **특징**: 
  - 별도 설치 없이 슈퍼앱 내에서 즉시 실행
  - HTML/CSS/JavaScript로 개발
  - 네이티브 기능 접근을 위한 Bridge API 제공
  - 각 미니앱은 격리된 환경에서 실행

## 2. 시스템 아키텍처

```
┌─────────────────────────────────────────────────────┐
│                   비단주머니 슈퍼앱                    │
├─────────────────────────────────────────────────────┤
│                    Navigation                        │
│  ┌────────┬────────┬────────┬────────┬────────┐    │
│  │  Main  │Identity│  Hub   │Browser │Settings│    │
│  └────────┴────────┴────────┴────────┴────────┘    │
├─────────────────────────────────────────────────────┤
│                  MiniApp Runtime                     │
│  ┌─────────────────────────────────────────────┐    │
│  │            WebView Container                 │    │
│  │  ┌─────────────────────────────────────┐    │    │
│  │  │         Government24 Mini App        │    │    │
│  │  │  (HTML/CSS/JS in isolated context)  │    │    │
│  │  └─────────────────────────────────────┘    │    │
│  │                    ↕                         │    │
│  │           JavaScript Bridge API             │    │
│  └─────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────┤
│                 Native Features                      │
│  - DID/VP Management                                │
│  - Blockchain Wallet                                │
│  - Storage & Security                               │
└─────────────────────────────────────────────────────┘
```

## 3. 미니앱 ZIP 패키지 구조

### 필수 구성요소

```
government24_v1.0.0.zip
├── manifest.json          # 미니앱 메타데이터 (필수)
├── app.js                 # 전역 JavaScript API
├── app.css               # 전역 스타일
├── pages/                # 페이지 디렉토리 (필수)
│   └── index/           # 메인 페이지 (필수)
│       ├── index.html   # 페이지 HTML
│       ├── index.js     # 페이지 로직
│       └── index.css    # 페이지 스타일
└── assets/              # 리소스 디렉토리
    └── images/
        └── img-gov-logo.svg
```

### manifest.json 구조 (W3C MiniApp 표준)

```json
{
  "app_id": "kr.go.government24",      // 고유 식별자
  "name": "정부24",                     // 앱 이름
  "version": "1.0.0",                  // 버전
  "description": "대한민국 정부 민원 서비스",
  "pages": ["pages/index/index"],      // 페이지 목록
  "window": {                          // 윈도우 설정 (선택)
    "navigationBarTitleText": "정부24",
    "navigationBarBackgroundColor": "#ffffff",
    "backgroundColor": "#f8f9fa"
  },
  "permissions": [                     // 권한 요청 (선택)
    "storage",
    "vp_request"
  ]
}
```

## 4. 미니앱 생성 방법

### Step 1: 미니앱 개발
```bash
# 디렉토리 구조 생성
mkdir -p government24/pages/index
mkdir -p government24/assets/images

# 필수 파일 생성
touch government24/manifest.json
touch government24/app.js
touch government24/app.css
touch government24/pages/index/index.html
touch government24/pages/index/index.js
touch government24/pages/index/index.css
```

### Step 2: ZIP 패키징
```bash
cd government24
zip -r ../government24_v1.0.0.zip *
```

### Step 3: 앱에 배포
```bash
# Android 프로젝트의 assets 디렉토리에 복사
cp government24_v1.0.0.zip app/src/main/assets/miniapps/
```

## 5. Native-JavaScript 통신

### JavaScript Bridge API (anam)

미니앱은 `anam` 전역 객체를 통해 네이티브 기능에 접근합니다:

```javascript
// 시스템 정보 조회
const systemInfo = JSON.parse(anam.getSystemInfo());
// 결과: {platform: "android", version: "13", ...}

// 토스트 메시지 표시
anam.showToast("안녕하세요!");

// 저장소 사용
anam.setStorageItem("key", "value");
const value = anam.getStorageItem("key");

// VP(Verifiable Presentation) 요청
anam.requestVP("authentication", "onAuthCallback");

// 콜백 함수
function onAuthCallback(vpData) {
    if (vpData.status === "success") {
        console.log("인증 성공:", vpData.data);
    }
}
```

### 미니앱 생명주기 이벤트

미니앱은 W3C MiniApp 표준에 따라 생명주기 이벤트를 제공합니다:

```javascript
// app.js
App({
  onLaunch() {
    console.log('미니앱 시작');
    // 초기화 코드
    const systemInfo = JSON.parse(anam.getSystemInfo());
    console.log('System info:', systemInfo);
  },
  
  onShow() {
    console.log('미니앱 표시');
    // 화면에 보일 때마다 실행
    const isLoggedIn = anam.getStorageItem('isLoggedIn');
    if (isLoggedIn === 'true') {
      // 로그인 상태 UI 업데이트
    }
  },
  
  onHide() {
    console.log('미니앱 숨김');
    // 필요한 정리 작업
  }
});
```

### 네이티브 구현 (Kotlin)

```kotlin
class MiniAppJavaScriptBridge(
    private val context: Context,
    private val manifest: MiniAppManifest
) {
    @JavascriptInterface
    fun showToast(message: String) {
        context.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    @JavascriptInterface
    fun requestVP(vpType: String, callback: String) {
        // VP 요청 처리 로직
    }
}
```

## 6. 격리 및 보안

### WebView 격리
- 각 미니앱은 독립된 WebView 인스턴스에서 실행
- 파일 시스템 접근은 미니앱 디렉토리로 제한
- 외부 URL 접근 차단 (file:// 프로토콜만 허용)

### 저장소 격리
```kotlin
// 미니앱별 SharedPreferences 사용
val prefs = context.getSharedPreferences("miniapp_${manifest.appId}", Context.MODE_PRIVATE)
```

### 권한 관리
- manifest.json의 permissions 배열로 권한 선언
- 런타임에 사용자 동의 필요한 기능은 별도 확인

## 7. 파일 저장 위치

### 미니앱 ZIP 파일
```
app/src/main/assets/miniapps/
└── government24_v1.0.0.zip
```

### 추출된 미니앱 파일
```
/data/data/com.anam.wallet/files/miniapps/
└── government24/
    ├── manifest.json
    ├── app.js
    ├── app.css
    ├── pages/
    └── assets/
```

### 미니앱 데이터
```
/data/data/com.anam.wallet/shared_prefs/
└── miniapp_kr.go.government24.xml
```

## 8. 사용 방법

### 사용자 관점
1. 메인 화면에서 "정부24" 아이콘 클릭
2. 미니앱이 WebView로 로드되어 실행
3. 뒤로가기 버튼으로 메인 화면 복귀

### 개발자 관점
1. 미니앱 개발 (HTML/CSS/JS)
2. manifest.json 작성
3. ZIP 파일로 패키징
4. assets/miniapps/ 디렉토리에 배치
5. MainScreen의 installedAppModules에 추가

```kotlin
val installedAppModules = remember {
    listOf(
        InstalledModule(
            id = "gov24",
            name = "정부24",
            icon = Icons.Filled.AccountBalance,
            primaryColor = Color(0xFF1976D2),
            isApp = true
        )
    )
}
```

## 9. 주요 컴포넌트

### MiniAppLoader.kt
- ZIP 파일 추출 및 캐싱
- manifest.json 파싱
- 미니앱 경로 관리

### MiniAppScreen.kt
- WebView 기반 미니앱 실행 환경
- Composable Navigation 통합
- 뒤로가기 처리

### MiniAppJavaScriptBridge.kt
- Native-JavaScript 통신 브릿지
- API 메서드 구현
- 보안 및 권한 체크

## 10. 장점

1. **크로스 플랫폼**: 웹 기술로 개발하여 Android/iOS 공통 사용
2. **즉시 실행**: 별도 설치 없이 바로 사용
3. **보안**: 격리된 환경에서 실행, VP 기반 인증
4. **확장성**: 새로운 미니앱 추가가 간단
5. **표준 준수**: W3C MiniApp 표준 따름

## 11. 메타마스크와 비교

### 상세 비교표

| 구분 | **메타마스크** | **비단주머니** |
|------|----------------|----------------|
| **아키텍처** | 브라우저 확장 프로그램 | 네이티브 모바일 앱 |
| **확장성** | Snaps (베타, 제한적) | 완전한 모듈 시스템 |
| **지원 블록체인** | EVM 계열 + 허가된 Snap | 모든 블록체인 (Bitcoin, Ethereum, Sui, Solana 등) |
| **단일 실패 지점** | **있음** - 메인 지갑 해킹 시 전체 위험 | **없음** - 모듈별 독립 실행으로 위험 분산 |
| **보안 격리** | iframe 샌드박스 + 제한된 API | 프로세스 레벨 격리 (강력함) |
| **모듈 간 통신** | Snaps API 의존 | 암호화된 IPC 통신 |
| **권한 관리** | 전체 권한 일괄 승인 | 모듈별 세분화된 권한 |
| **키 관리** | 중앙 집중식 (하나의 시드) | 모듈별 독립적 키 관리 |
| **악성 코드 영향** | 전체 지갑 위험 노출 | 해당 모듈만 격리 |
| **업데이트** | 전체 앱 업데이트 필요 | 모듈별 독립 업데이트 |
| **오프라인 지원** | 제한적 | 완전 지원 |
| **일반 앱 통합** | dApp 브라우저 연결만 | 미니앱으로 직접 통합 |
| **개발 언어** | JavaScript만 | 다양한 언어 지원 |
| **배포 방식** | MetaMask 검토 필수 | 오픈 마켓플레이스 |
| **실행 환경** | 브라우저 확장 전용 | 네이티브 앱 + 브라우저 확장 가능 |
| **성능** | 브라우저 성능에 의존 | 네이티브 최적화 가능 |
| **생태계 통합** | Web3 전용 | Web2 + Web3 융합 |

### 핵심 차이점

#### 1. 보안 아키텍처
- **메타마스크**: 하나의 Private Key가 모든 계정을 제어하는 중앙집중식 구조로 단일 실패 지점 존재
- **비단주머니**: 각 모듈이 독립된 프로세스에서 실행되며 개별 키 관리로 위험 분산

#### 2. 확장성
- **메타마스크 Snaps**: 
  - JavaScript 전용
  - EVM 체인 한정
  - MetaMask의 승인 필요
  - 제한된 API만 사용 가능
  
- **비단주머니 모듈**: 
  - 웹 기술 + 네이티브 모듈 지원
  - 모든 블록체인 지원
  - 오픈 마켓플레이스
  - 풍부한 네이티브 API

#### 3. 사용자 경험
- **메타마스크**: 브라우저 확장으로 웹에서만 사용
- **비단주머니**: 모바일 네이티브 앱 + 브라우저 확장으로 다양한 환경에서 사용 가능

## 12. 향후 개선사항

1. 미니앱 스토어 (Hub) 구현
2. 동적 미니앱 다운로드
3. 미니앱 간 통신 프로토콜
4. 더 많은 Native API 추가
5. 오프라인 지원
6. 미니앱 성능 최적화