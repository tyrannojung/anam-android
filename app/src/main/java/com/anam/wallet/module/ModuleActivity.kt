package com.anam.wallet.module

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anam.wallet.IMainAppService
import com.anam.wallet.core.IFrontModuleUI
import com.anam.wallet.core.IMainApp
import com.anam.wallet.ui.theme.AnamwalletTheme
import dalvik.system.DexClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ModuleActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "ModuleActivity"
        const val EXTRA_MODULE_PATH = "module_path"
        const val EXTRA_MODULE_CLASS = "module_class"
        const val EXTRA_MODULE_NAME = "module_name"
    }
    
    private var mainAppService: IMainAppService? = null
    private var moduleInstance: IFrontModuleUI? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            mainAppService = IMainAppService.Stub.asInterface(service)
            
            // Create MainApp wrapper for module
            moduleInstance?.setMainAppService(object : IMainApp {
                override suspend fun requestVP(
                    challenge: String,
                    presentationDefinition: String,
                    requesterName: String
                ): String? {
                    return try {
                        mainAppService?.requestVP(challenge, presentationDefinition, requesterName)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error requesting VP", e)
                        null
                    }
                }
            })
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            mainAppService = null
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get module info from intent
        val modulePath = intent.getStringExtra(EXTRA_MODULE_PATH) ?: run {
            Log.e(TAG, "No module path provided")
            finish()
            return
        }
        
        val moduleClass = intent.getStringExtra(EXTRA_MODULE_CLASS) ?: run {
            Log.e(TAG, "No module class provided")
            finish()
            return
        }
        
        val moduleName = intent.getStringExtra(EXTRA_MODULE_NAME) ?: "Module"
        
        // Bind to main app service
        val serviceIntent = Intent()
        serviceIntent.setClassName("com.anam.wallet", "com.anam.wallet.service.MainAppService")
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        setContent {
            AnamwalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModuleScreen(
                        modulePath = modulePath,
                        moduleClass = moduleClass,
                        moduleName = moduleName
                    )
                }
            }
        }
    }
    
    @Composable
    fun ModuleScreen(
        modulePath: String,
        moduleClass: String,
        moduleName: String
    ) {
        var isLoading by remember { mutableStateOf(true) }
        var loadError by remember { mutableStateOf<String?>(null) }
        var moduleUI by remember { mutableStateOf<@Composable () -> Unit>({}) }
        
        LaunchedEffect(modulePath) {
            withContext(Dispatchers.IO) {
                try {
                    // Load module APK
                    val apkFile = File(modulePath)
                    if (!apkFile.exists()) {
                        loadError = "Module file not found: $modulePath"
                        isLoading = false
                        return@withContext
                    }
                    
                    // Create DexClassLoader
                    val optimizedDirectory = File(cacheDir, "dex").apply { mkdirs() }
                    val classLoader = DexClassLoader(
                        apkFile.absolutePath,
                        optimizedDirectory.absolutePath,
                        null,
                        this@ModuleActivity.classLoader
                    )
                    
                    // Load module class
                    val loadedClass = classLoader.loadClass(moduleClass)
                    val instance = loadedClass.getDeclaredConstructor().newInstance() as IFrontModuleUI
                    
                    moduleInstance = instance
                    
                    // Set MainApp service if already connected
                    mainAppService?.let {
                        instance.setMainAppService(object : IMainApp {
                            override suspend fun requestVP(
                                challenge: String,
                                presentationDefinition: String,
                                requesterName: String
                            ): String? {
                                return try {
                                    it.requestVP(challenge, presentationDefinition, requesterName)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error requesting VP", e)
                                    null
                                }
                            }
                        })
                    }
                    
                    // Get UI from module
                    withContext(Dispatchers.Main) {
                        val context = com.anam.wallet.core.FrontModuleContext(
                            moduleId = moduleName,
                            parameters = emptyMap()
                        )
                        moduleUI = { instance.FrontModuleScreen(context) }
                        instance.onModuleStart()
                        isLoading = false
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading module", e)
                    loadError = "Failed to load module: ${e.message}"
                    isLoading = false
                }
            }
        }
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            loadError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = loadError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                moduleUI()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Module lifecycle is managed by onModuleStart/Stop
    }
    
    override fun onPause() {
        super.onPause()
        moduleInstance?.onModuleStop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        moduleInstance?.onModuleDestroy()
        unbindService(serviceConnection)
    }
}