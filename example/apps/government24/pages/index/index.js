// 정부24 메인 페이지 로직
console.log('정부24 인덱스 페이지 로드됨');

// 로그인 핸들러
function handleLogin() {
    console.log('로그인 버튼 클릭');
    // VP 연동 예정
}

// 아이디 찾기
function handleFindId() {
    console.log('아이디 찾기 클릭');
    return false;
}

// 비밀번호 찾기
function handleFindPassword() {
    console.log('비밀번호 찾기 클릭');
    return false;
}

// 회원가입
function handleSignup() {
    console.log('회원가입 클릭');
    return false;
}

// 서비스 클릭 핸들러
function handleServiceClick(serviceName) {
    console.log(`서비스 클릭: ${serviceName}`);
    
    // 주민등록등본 클릭 시 결제 페이지로 이동
    if (serviceName === '주민등록등본') {
        window.location.href = '../payment/payment.html';
    } else {
        // 다른 서비스들은 토스트 메시지 표시
        if (window.anam && window.anam.showToast) {
            window.anam.showToast(`${serviceName} 서비스는 준비 중입니다`);
        } else {
            alert(`${serviceName} 서비스는 준비 중입니다`);
        }
    }
}