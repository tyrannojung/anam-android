# AnamWallet Mini-App Architecture

## 1. Overview

AnamWallet is a modular blockchain super-app that supports multiple blockchains and mini-apps, built with a focus on security through process isolation.

### What is a Super App?
- **Definition**: A platform app that provides multiple services within a single application
- **Examples**: WeChat, Alipay, Grab, Gojek
- **Key Feature**: Mini-apps run independently within the main app, allowing users to access various services without leaving the app

### What is a Mini-App?
- **Definition**: Small applications that run within a super app
- **Characteristics**: 
  - Instant execution without separate installation
  - Developed with HTML/CSS/JavaScript
  - Bridge API for accessing native features
  - Each mini-app runs in an isolated environment

## 2. Multi-Process Architecture

### Process Separation Strategy

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Android System                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────────────┐     ┌─────────────────────────┐      │
│  │   Main Process          │     │  :blockchain Process     │      │
│  │   (com.anam.wallet)     │     │  (com.anam.wallet:       │      │
│  │                         │     │   blockchain)            │      │
│  ├─────────────────────────┤     ├─────────────────────────┤      │
│  │ • MainActivity          │     │ • BlockchainService      │      │
│  │ • Government24 MiniApp  │ IPC │   (ForegroundService)    │      │
│  │ • MiniAppManager       │<--->│ • Ethereum WebView       │      │
│  │ • Identity/DID         │ AIDL│ • Bitcoin WebView        │      │
│  │ • UI Components        │     │ • Sui WebView            │      │
│  └─────────────────────────┘     └─────────────────────────┘      │
│           │                                 │                       │
│           │ JavaScript Bridge               │                       │
│           ▼                                 ▼                       │
│  ┌─────────────────────────┐     ┌─────────────────────────┐      │
│  │  Government24 WebView   │     │  Blockchain WebViews    │      │
│  │  (Main Process)         │     │  (Separate Process)     │      │
│  └─────────────────────────┘     └─────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────┘
```

### Why Process Separation?

Android has a limitation where only one WebView renderer process can exist per app. By moving blockchain modules to a separate process, we can:
- Run multiple blockchain WebViews concurrently
- Improve security through process isolation
- Prevent crashes in one module from affecting others

## 3. AIDL-Based IPC Communication

### AIDL Interfaces

```kotlin
// IBlockchainService.aidl
interface IBlockchainService {
    void processRequest(String requestJson, IBlockchainCallback callback);
    String getWalletAddress();
    void switchBlockchain(String blockchainId);
    boolean isBlockchainActive();
    String getActiveBlockchainId();
}

// IBlockchainCallback.aidl
interface IBlockchainCallback {
    void onSuccess(String responseJson);
    void onError(String errorMessage);
}
```

### Cross-Process Payment Flow

```
Government24 (Main)     MiniAppManager     BlockchainService    Ethereum (Blockchain)
      │                      │                    │                    │
      │ requestPayment()     │                    │                    │
      ├─────────────────────>│                    │                    │
      │                      │ AIDL processRequest│                    │
      │                      ├───────────────────>│                    │
      │                      │                    │ evaluateJavaScript │
      │                      │                    ├───────────────────>│
      │                      │                    │                    │
      │                      │                    │<───────────────────┤
      │                      │<───────────────────┤ Transaction Result │
      │<─────────────────────┤ AIDL Callback     │                    │
      │ paymentResponse      │                    │                    │
```

## 4. DID/VP Integration

### DID (Decentralized Identifier) System

```
┌─────────────────────────────────────────────────────────────────────┐
│                         DID Architecture                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Android Device                          DID Server                 │
│  ┌─────────────────────┐                ┌─────────────────────┐    │
│  │  Secure Storage     │                │  DID Registry       │    │
│  ├─────────────────────┤                ├─────────────────────┤    │
│  │ • Private Keys      │                │ • User DIDs         │    │
│  │ • Public Keys       │   HTTP/REST    │ • License DIDs      │    │
│  │ • Wallet Info       │<──────────────>│ • VC Storage        │    │
│  │ • VCs (Local)       │                │ • VP Verification   │    │
│  └─────────────────────┘                └──────────┬───────────┘    │
│           │                                        │                │
│           │                                        ▼                │
│           │                             ┌─────────────────────┐    │
│           │                             │ Hyperledger Fabric  │    │
│           │                             │    Blockchain       │    │
│           │                             └─────────────────────┘    │
│           │                                                         │
│           ▼                                                         │
│  ┌─────────────────────┐                                          │
│  │  VP Generation      │                                          │
│  ├─────────────────────┤                                          │
│  │ 1. Load VC          │                                          │
│  │ 2. Create VP        │                                          │
│  │ 3. Sign with Key    │                                          │
│  │ 4. Add Proof        │                                          │
│  └─────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────┘
```

### VP Request Flow for Government24 Login

```javascript
// Government24 mini-app requests VP
function handleLogin() {
    const vpRequest = {
        challenge: "gov24_" + Date.now(),
        presentationDefinition: {
            input_descriptors: [{
                id: "driver_license",
                name: "운전면허증",
                purpose: "정부24 로그인을 위한 신원 확인"
            }]
        },
        requesterName: "정부24"
    };
    
    window.anam.requestVP(JSON.stringify(vpRequest));
}
```

### VP Request Handling Flow

```
Government24          MiniAppManager         BottomSheet          DIDService
     │                     │                      │                   │
     │ requestVP()         │                      │                   │
     ├────────────────────>│                      │                   │
     │                     │ Show Consent UI      │                   │
     │                     ├─────────────────────>│                   │
     │                     │                      │ User Confirms     │
     │                     │                      ├──────────────────>│
     │                     │                      │ Generate VP       │
     │                     │                      │<──────────────────┤
     │                     │<─────────────────────┤                   │
     │<────────────────────┤ vpResponse Event    │                   │
     │                     │                      │                   │
```

## 5. Mini-App Package Structure

### Required Components

```
kr.go.government24_1.0.0.zip
├── manifest.json          # Mini-app metadata (required)
├── app.js                # Global JavaScript API
├── app.css              # Global styles
├── pages/               # Pages directory (required)
│   ├── index/          # Main page (required)
│   │   ├── index.html  # Page HTML
│   │   ├── index.js    # Page logic
│   │   └── index.css   # Page styles
│   ├── payment/        # Payment page
│   │   ├── payment.html
│   │   ├── payment.js
│   │   └── payment.css
│   └── success/        # Success page
│       ├── success.html
│       ├── success.js
│       └── success.css
└── assets/             # Resources directory
    ├── images/
    │   └── img-gov-logo.svg
    └── icons/
        └── app_icon.png
```

### manifest.json Structure (W3C MiniApp Standard)

```json
{
  "app_id": "kr.go.government24",
  "name": "정부24",
  "version": "1.0.0",
  "type": "app",
  "description": "대한민국 정부 민원 서비스",
  "pages": ["pages/index/index", "pages/payment/payment", "pages/success/success"],
  "window": {
    "navigationBarTitleText": "정부24",
    "navigationBarBackgroundColor": "#ffffff",
    "backgroundColor": "#f8f9fa"
  },
  "permissions": [
    "storage",
    "vp_request",
    "payment"
  ]
}
```

## 6. JavaScript Bridge API

### Available APIs

```javascript
// VP (Verifiable Presentation) Request
window.anam.requestVP(JSON.stringify({
    challenge: "unique_challenge",
    presentationDefinition: { /* W3C format */ },
    requesterName: "Service Name"
}));

// Payment Request
window.anam.requestPayment(JSON.stringify({
    to: "0x...",
    amount: "0.001",
    data: "0x"
}));

// Storage
window.anam.setStorageItem("key", "value");
const value = window.anam.getStorageItem("key");

// Toast Message
window.anam.showToast("Message");

// System Info
const info = JSON.parse(window.anam.getSystemInfo());
```

### Event Handling

```javascript
// Listen for VP response
window.addEventListener('vpResponse', (event) => {
    const { vp, challenge, error } = event.detail;
    if (error) {
        console.error('VP request failed:', error);
    } else {
        console.log('VP received:', vp);
        // Verify VP with server
    }
});

// Listen for payment response
window.addEventListener('paymentResponse', (event) => {
    const { txHash, error } = event.detail;
    if (error) {
        console.error('Payment failed:', error);
    } else {
        console.log('Transaction hash:', txHash);
    }
});
```

## 7. Security & Isolation

### WebView Isolation
- Each mini-app runs in an independent WebView instance
- File system access restricted to mini-app directory
- External URL access blocked (only file:// protocol allowed)
- Different WebView data directories for each process

### Storage Isolation
```kotlin
// Mini-app specific SharedPreferences
val prefs = context.getSharedPreferences("miniapp_${manifest.appId}", Context.MODE_PRIVATE)

// Process-specific WebView data directory
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    WebView.setDataDirectorySuffix("blockchain") // For blockchain process
}
```

### Key Security Features
- **Self-Sovereign Identity**: Private keys exist only on user's device
- **Process Isolation**: Blockchain runs in separate process
- **Secure Storage**: Keys stored in EncryptedSharedPreferences
- **Permission Management**: Granular permissions per mini-app

## 8. File Storage Locations

### Mini-app ZIP Files
```
app/src/main/assets/miniapps/
├── kr.go.government24_1.0.0.zip
└── com.anam.ethereum_1.0.0.zip
```

### Extracted Mini-app Files
```
/data/data/com.anam.wallet/files/miniapps/
├── kr.go.government24/
│   ├── manifest.json
│   ├── app.js
│   ├── pages/
│   └── assets/
└── com.anam.ethereum/
    ├── manifest.json
    ├── app.js
    └── pages/
```

### Secure Storage
```
/data/data/com.anam.wallet/
├── shared_prefs/
│   ├── miniapp_kr.go.government24.xml
│   └── EncryptedSharedPreferences/
│       └── user_keys.xml (encrypted)
└── files/
    └── user.vc (Verifiable Credential)
```

## 9. Development Workflow

### Creating a Mini-App

1. **Develop the mini-app**
```bash
mkdir -p myapp/pages/index
touch myapp/manifest.json
touch myapp/pages/index/index.html
touch myapp/pages/index/index.js
```

2. **Build ZIP package**
```bash
cd example
./build-miniapps.sh
```

3. **Deploy to app**
The build script automatically:
- Creates ZIP files with correct naming (app_id + version)
- Copies to `app/src/main/assets/miniapps/`
- Excludes test files and unnecessary resources

### Testing Mini-Apps

1. Build and install the app
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

2. Access mini-app from main screen
3. Use Chrome DevTools for debugging WebView

## 10. Key Components

### MiniAppLoader.kt
- ZIP file extraction and caching
- manifest.json parsing
- Path management for mini-apps

### MiniAppManager.kt
- Singleton manager for mini-app lifecycle
- Handles VP requests and payment flows
- Manages communication with blockchain process

### MiniAppJavaScriptBridge.kt
- Native-JavaScript communication bridge
- Implements window.anam APIs
- Security and permission checks

### BlockchainService.kt
- Runs in separate `:blockchain` process
- Manages blockchain WebViews
- Handles AIDL communication

### VPRequestBottomSheet.kt
- User consent UI for VP requests
- Shows credential preview
- Handles user confirmation/cancellation

## 11. Advantages Over Traditional Approaches

### Compared to MetaMask

| Feature | **MetaMask** | **AnamWallet** |
|---------|--------------|----------------|
| **Architecture** | Browser extension | Native mobile app with process isolation |
| **Blockchain Support** | EVM chains + limited Snaps | All blockchains in separate processes |
| **Security Model** | Single process, shared memory | Process-level isolation |
| **Mini-App Support** | dApp browser only | Full mini-app ecosystem |
| **Key Management** | Centralized seed phrase | Module-specific keys possible |
| **Crash Impact** | Affects entire wallet | Isolated to specific module |
| **Native Features** | Limited browser APIs | Full native API access |
| **Offline Support** | Limited | Full offline capability |

### Security Benefits of Process Isolation

1. **Memory Isolation**: Each process has its own memory space
2. **Crash Protection**: Blockchain crashes don't affect main app
3. **Permission Separation**: Different permissions per process
4. **Resource Limits**: OS-level resource management per process

## 12. Future Enhancements

1. **Dynamic Mini-App Loading**: Download mini-apps from store
2. **Inter-Mini-App Communication**: Secure message passing
3. **Enhanced VP Features**: Selective disclosure, zero-knowledge proofs
4. **Multi-Chain Payment Router**: Automatic chain selection
5. **Offline VP Generation**: Work without network connection
6. **Mini-App Marketplace**: Decentralized app store

## 13. Conclusion

AnamWallet's architecture provides a secure, modular platform for blockchain interactions and mini-apps. By leveraging Android's process isolation and AIDL for IPC, we overcome WebView limitations while maintaining security. The integration of DID/VP standards enables self-sovereign identity, making it suitable for both Web3 and traditional services like Government24.