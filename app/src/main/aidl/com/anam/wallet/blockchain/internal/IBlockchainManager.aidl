// IBlockchainManager.aidl
package com.anam.wallet.blockchain.internal;

import com.anam.wallet.blockchain.api.IBlockchainCallback;

/**
 * 시스템 내부용 블록체인 관리 인터페이스
 * Internal system interface for blockchain management
 */
interface IBlockchainManager {
    /**
     * 다른 블록체인으로 전환
     * Switch to a different blockchain
     * 
     * @param blockchainId 전환할 블록체인 ID (예: "com.anam.ethereum")
     */
    void switchBlockchain(String blockchainId);
    
    /**
     * 블록체인 활성화 상태 확인
     * Check if a blockchain is currently active
     * 
     * @return true if active, false otherwise
     */
    boolean isBlockchainActive();
    
    /**
     * 현재 활성화된 블록체인 ID 조회
     * Get the ID of currently active blockchain
     * 
     * @return 블록체인 ID 또는 null
     */
    String getActiveBlockchainId();
    
    /**
     * 블록체인 변경 이벤트 리스너 등록
     * Register listener for blockchain change events
     * 
     * @param listener 변경 이벤트를 받을 콜백
     */
    void registerBlockchainChangeListener(IBlockchainCallback listener);
    
    /**
     * 블록체인 변경 이벤트 리스너 해제
     * Unregister blockchain change listener
     * 
     * @param listener 해제할 콜백
     */
    void unregisterBlockchainChangeListener(IBlockchainCallback listener);
}