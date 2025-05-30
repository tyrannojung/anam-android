package com.anam.wallet;

// AIDL interface for module to communicate with main app
interface IMainAppService {
    // Request Verifiable Presentation from main app
    String requestVP(String challenge, String presentationDefinition, String requesterName);
    
    // Get wallet information
    String getWalletInfo();
    
    // Check if module has permission
    boolean hasPermission(String permission);
}