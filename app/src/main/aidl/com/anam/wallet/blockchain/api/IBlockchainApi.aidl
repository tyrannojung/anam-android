// IBlockchainApi.aidl
package com.anam.wallet.blockchain.api;

import com.anam.wallet.blockchain.api.IBlockchainCallback;

/**
 * 미니앱 개발자를 위한 블록체인 API
 * Mini-app developers should use this interface
 */
interface IBlockchainApi {
    /**
     * 블록체인 요청 처리 (결제, 스마트 컨트랙트 호출 등)
     * Process blockchain requests (payment, smart contract calls, etc.)
     * 
     * @param requestJson JSON 형식의 요청 데이터
     * @param callback 비동기 응답을 받을 콜백
     */
    void processRequest(String requestJson, IBlockchainCallback callback);
    
    /**
     * 현재 활성화된 블록체인의 지갑 주소 가져오기
     * Get wallet address for the currently active blockchain
     * 
     * @return 지갑 주소 (예: "0x1234..." for Ethereum)
     */
    String getWalletAddress();
}