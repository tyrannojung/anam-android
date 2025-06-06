// 결제 페이지 로직
console.log('결제 페이지 로드됨');

// 현재 선택된 결제 수단
let selectedMethod = 'blockchain';

// 뒤로가기
function handleBack() {
    window.history.back();
}

// 결제 수단 선택
function selectPaymentMethod(method) {
    // 이전 선택 제거 (border만 제거, 활성화 뱃지는 유지)
    document.querySelectorAll('.payment-method').forEach(methodEl => {
        methodEl.classList.remove('active');
    });
    
    // 새로운 선택 추가
    const selectedMethodEl = document.querySelector(`[data-method="${method}"]`);
    selectedMethodEl.classList.add('active');
    
    selectedMethod = method;
    console.log(`선택된 결제 수단: ${method}`);
}

// 결제 처리
function processPayment() {
    console.log(`${selectedMethod}으로 결제 처리 시작`);
    
    let paymentInfo = '';
    
    switch(selectedMethod) {
        case 'blockchain':
            paymentInfo = 'Ethereum 0.00000001 ETH로 결제를 진행합니다';
            break;
        case 'phone':
            paymentInfo = '핸드폰 소액결제로 400원을 결제합니다';
            break;
        case 'card':
            paymentInfo = '신용카드로 400원을 결제합니다';
            break;
    }
    
    // 토스트 메시지 표시
    if (window.anam && window.anam.showToast) {
        window.anam.showToast(paymentInfo);
    } else {
        alert(paymentInfo);
    }
    
    // 실제 결제 로직은 여기에 구현
    // 블록체인 결제의 경우 이더리움 모듈과 연동
    // 핸드폰/카드 결제의 경우 각각의 결제 모듈과 연동
}