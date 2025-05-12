package com.anam.wallet.core;

interface ICoinModule {
    String getSymbol();
    String request(String jsonRpcPayload);
    String createAccount(String encMessage);
}
