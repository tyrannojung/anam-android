// IBlockchainService.aidl
package com.anam.wallet.blockchain;

import com.anam.wallet.blockchain.IBlockchainCallback;

interface IBlockchainService {
    // Process any blockchain request (payment, contract call, etc.)
    // JSON string is passed as-is to the active blockchain
    void processRequest(String requestJson, IBlockchainCallback callback);
    
    // Get current wallet address for the active blockchain
    String getWalletAddress();
    
    // Switch to a different blockchain (ethereum, sui, bitcoin, etc.)
    void switchBlockchain(String blockchainId);
    
    // Check if a blockchain is currently active
    boolean isBlockchainActive();
    
    // Get the ID of currently active blockchain
    String getActiveBlockchainId();
}