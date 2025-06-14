// 정부24 메인 페이지 로직
console.log('정부24 인덱스 페이지 로드됨');

// 로그인 상태 확인
let isLoggedIn = false;

// 로그인 핸들러
function handleLogin() {
    console.log('로그인 버튼 클릭');
    
    // VP 요청
    const vpRequest = {
        challenge: "gov24_" + Date.now(),
        presentationDefinition: {
            input_descriptors: [{
                id: "driver_license",
                name: "운전면허증",
                purpose: "정부24 로그인을 위한 신원 확인",
                constraints: {
                    fields: [{
                        path: ["$.type"],
                        filter: {
                            type: "string",
                            pattern: "DriverLicenseVC"
                        }
                    }]
                }
            }]
        },
        requesterName: "정부24"
    };
    
    // VP 응답 이벤트 리스너 등록
    window.addEventListener('vpResponse', handleVPResponse, { once: true });
    
    // VP 요청
    if (window.anam && window.anam.requestVP) {
        window.anam.requestVP(JSON.stringify(vpRequest));
    } else {
        console.error('VP 요청 기능을 사용할 수 없습니다.');
        alert('로그인 기능을 사용할 수 없습니다.');
    }
}

// VP 응답 처리
function handleVPResponse(event) {
    console.log('VP 응답:', event.detail);
    
    const vpData = event.detail;
    if (vpData.error) {
        console.error('VP 요청 실패:', vpData.error);
        alert('로그인에 실패했습니다: ' + vpData.error);
        return;
    }
    
    // VP 검증 (실제로는 서버에서 수행)
    console.log('VP 수신 성공:', vpData.vp);
    console.log('Challenge:', vpData.challenge);
    
    // 로그인 성공 처리
    isLoggedIn = true;
    alert('로그인이 완료되었습니다.');
    
    // 결제 페이지로 이동 (navigateTo API 사용)
    window.anam.navigateTo('pages/payment/payment');
}

// 서비스 클릭 핸들러
function handleServiceClick(serviceName) {
    console.log(`서비스 클릭: ${serviceName}`);
    
    // 로그인 상태 확인
    if (!isLoggedIn) {
        // 로그인하지 않았으면 아무 반응 없음
        return;
    }
    
    // 주민등록등본 클릭 시 결제 페이지로 이동
    if (serviceName === '주민등록등본') {
        window.anam.navigateTo('pages/payment/payment');
    } else {
        // 다른 서비스들도 아무 반응 없음
        return;
    }
}