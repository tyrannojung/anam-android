package com.anam.wallet;

// AIDL interface for main app to communicate with module
interface IFrontModule {
    // Notify module about lifecycle events
    void onLifecycleEvent(String event);
    
    // Send data to module
    void onDataReceived(String data);
    
    // Check if module is ready
    boolean isReady();
    
    // Get module info
    String getModuleInfo();
}