// IBlockchainCallback.aidl
package com.anam.wallet.blockchain.api;

interface IBlockchainCallback {
    // Called when blockchain request succeeds
    // responseJson contains the raw response from blockchain
    void onSuccess(String responseJson);
    
    // Called when blockchain request fails
    void onError(String errorMessage);
}