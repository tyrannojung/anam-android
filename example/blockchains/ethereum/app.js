// 이더리움 지갑 미니앱 메인 로직

// App 전역 함수 - 미니앱 생명주기 관리
function App(config) {
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

// W3C MiniApp 생명주기 이벤트
App({
  onLaunch() {
    console.log('이더리움 지갑 미니앱 시작');
  },
  onShow() {
    console.log('이더리움 지갑 미니앱 표시');
  },
  onHide() {
    console.log('이더리움 지갑 미니앱 숨김');
  }
});