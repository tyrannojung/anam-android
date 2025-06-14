// 정부24 메인 페이지 로직
console.log('정부24 인덱스 페이지 로드됨');

// 로그인 상태 확인
let isLoggedIn = false;

// 로그인 핸들러
function handleLogin() {
    console.log('로그인 버튼 클릭');
    // VP 연동 예정 - 나중에 구현
    // 임시로 바로 로그인 성공 처리
    isLoggedIn = true;
    window.location.href = '../payment/payment.html';
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
        window.location.href = '../payment/payment.html';
    } else {
        // 다른 서비스들도 아무 반응 없음
        return;
    }
}