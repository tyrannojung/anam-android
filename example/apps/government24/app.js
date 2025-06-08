// AnamWallet 미니앱 JavaScript API
// W3C MiniApp 표준을 따르는 글로벌 API 객체

// 미니앱 인스턴스
let appInstance = null;

// App 전역 함수 - 미니앱 생명주기 관리
function App(config) {
  appInstance = config;
  
  // 페이지 로드 시 자동으로 onLaunch 호출
  if (document.readyState === 'complete' || document.readyState === 'interactive') {
    if (config.onLaunch) {
      config.onLaunch();
    }
  } else {
    document.addEventListener('DOMContentLoaded', function() {
      if (config.onLaunch) {
        config.onLaunch();
      }
    });
  }
  
  return config;
}

// anam 네임스페이스 확인 및 초기화
if (typeof anam === 'undefined') {
  console.error('anam API not available. Running in browser mode.');
  
  // 브라우저에서 테스트할 때를 위한 모의 API
  window.anam = {
    showToast: function(message) {
      console.log('Toast:', message);
      alert(message);
    },
    getSystemInfo: function() {
      return JSON.stringify({
        platform: 'web',
        version: 'mock',
        brand: 'browser',
        model: 'mock',
        screenWidth: window.innerWidth,
        screenHeight: window.innerHeight,
        pixelRatio: window.devicePixelRatio || 1
      });
    },
    getAppInfo: function() {
      return JSON.stringify({
        appId: 'mock.app',
        name: 'Mock App',
        version: '1.0.0',
        permissions: ''
      });
    },
    navigateTo: function(page) {
      console.log('Navigate to:', page);
      window.location.href = page + '.html';
    },
    navigateBack: function() {
      console.log('Navigate back');
      window.history.back();
    },
    setNavigationBarTitle: function(title) {
      console.log('Set navigation bar title:', title);
      document.title = title;
    },
    setStorageItem: function(key, value) {
      localStorage.setItem(key, value);
    },
    getStorageItem: function(key) {
      return localStorage.getItem(key);
    },
    removeStorageItem: function(key) {
      localStorage.removeItem(key);
    },
    clearStorage: function() {
      localStorage.clear();
    },
    requestVP: function(vpType, callback) {
      console.log('Request VP:', vpType);
      // 모의 VP 응답
      const mockVP = {
        type: vpType,
        status: 'success',
        data: {
          credential: 'mock_credential_data',
          issuer: 'AnamWallet',
          subject: 'user_did'
        }
      };
      if (window[callback]) {
        window[callback](mockVP);
      }
    }
  };
}

// 글로벌 App 객체 익스포트
window.App = appInstance;

// 미니앱 유틸리티 함수들

// 시스템 정보 가져오기
function getSystemInfo() {
  try {
    return JSON.parse(anam.getSystemInfo());
  } catch (e) {
    console.error('Failed to get system info:', e);
    return {};
  }
}

// 앱 정보 가져오기
function getAppInfo() {
  try {
    return JSON.parse(anam.getAppInfo());
  } catch (e) {
    console.error('Failed to get app info:', e);
    return {};
  }
}

// 로그인 처리
function handleLogin() {
  anam.showToast('로그인 기능은 준비 중입니다');
  
  // VP 요청 예시
  anam.requestVP('authentication', 'onAuthVPReceived');
}

// VP 수신 콜백
function onAuthVPReceived(vpData) {
  console.log('Received VP:', vpData);
  if (vpData.status === 'success') {
    anam.showToast('인증이 완료되었습니다');
    // 로그인 상태 저장
    anam.setStorageItem('isLoggedIn', 'true');
    anam.setStorageItem('userDid', vpData.data.subject);
  } else {
    anam.showToast('인증에 실패했습니다');
  }
}

// 아이디 찾기
function handleFindId() {
  anam.showToast('아이디 찾기는 준비 중입니다');
}

// 비밀번호 찾기
function handleFindPassword() {
  anam.showToast('비밀번호 찾기는 준비 중입니다');
}

// 회원가입
function handleSignup() {
  anam.showToast('회원가입은 준비 중입니다');
}

// 서비스 클릭 처리
function handleServiceClick(serviceName) {
  anam.showToast(serviceName + ' 서비스를 선택했습니다');
  
  // 서비스별 VP 요청
  switch(serviceName) {
    case '주민등록등본':
      anam.requestVP('resident_registration', 'onServiceVPReceived');
      break;
    case '가족관계증명서':
      anam.requestVP('family_certificate', 'onServiceVPReceived');
      break;
    case '건강보험자격득실확인서':
      anam.requestVP('health_insurance', 'onServiceVPReceived');
      break;
    default:
      console.log('Unknown service:', serviceName);
  }
}

// 서비스 VP 수신 콜백
function onServiceVPReceived(vpData) {
  console.log('Service VP received:', vpData);
  if (vpData.status === 'success') {
    anam.showToast('서류가 준비되었습니다');
    // 여기서 서류를 표시하거나 다운로드 처리
  } else {
    anam.showToast('서류 발급에 실패했습니다');
  }
}

// 미니앱 생명주기 정의
App({
  onLaunch() {
    console.log('정부24 미니앱 시작');
    
    // 시스템 정보 로그
    const systemInfo = getSystemInfo();
    console.log('System info:', systemInfo);
    
    // 앱 정보 로그
    const appInfo = getAppInfo();
    console.log('App info:', appInfo);
  },
  
  onShow() {
    console.log('정부24 미니앱 표시');
    
    // 로그인 상태 확인
    const isLoggedIn = anam.getStorageItem('isLoggedIn');
    if (isLoggedIn === 'true') {
      console.log('User is logged in');
      // 로그인 상태 UI 업데이트
    }
  },
  
  onHide() {
    console.log('정부24 미니앱 숨김');
    // 필요한 정리 작업 수행
  }
});