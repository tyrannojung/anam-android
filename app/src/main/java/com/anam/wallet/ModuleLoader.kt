package com.anam.wallet

import android.content.Context
import android.util.Log
import com.anam.wallet.core.IPaymentModule
import dalvik.system.DexClassLoader
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles loading external payment modules using DexClassLoader
 */
class ModuleLoader(private val context: Context) {
    
    companion object {
        private const val TAG = "ModuleLoader"
        private const val MODULE_INTERFACE = "com.anam.wallet.core.IPaymentModule"

        // List of potential implementation class names to try
        private val IMPLEMENTATION_CLASSES = listOf(
            "com.anam.payment.PaymentModuleImpl",
            "com.anam.payment.Module",
            "com.anam.wallet.payment.PaymentModule"
        )
    }
    
    // Map to store loaded modules
    private val loadedModules = ConcurrentHashMap<String, IPaymentModule>()
    
    // Map to store classloaders for each module
    private val moduleClassLoaders = ConcurrentHashMap<String, DexClassLoader>()
    
    // Directory for optimized dex files
    private val optimizedDexDir by lazy { 
        File(context.filesDir, "dex-cache").apply { 
            if (!exists()) mkdirs() 
        }
    }
    
    /**
     * Get list of loaded module IDs
     */
    fun getLoadedModuleIds(): Set<String> = loadedModules.keys
    
    /**
     * Check if a module is loaded
     */
    fun isModuleLoaded(moduleId: String): Boolean = loadedModules.containsKey(moduleId)
    
    /**
     * Get a loaded module (internal use only)
     */
    internal fun getModule(moduleId: String): IPaymentModule? = loadedModules[moduleId]
    
    /**
     * Load a module from APK file
     *
     * @param moduleId Unique identifier for the module
     * @param apkPath Path to the APK file
     * @return true if module was loaded successfully
     */
    fun loadModule(moduleId: String, apkPath: File): Boolean {
        try {
            Log.d(TAG, "Loading module: $moduleId from $apkPath")
            
            // Check if APK file exists
            if (!apkPath.exists()) {
                Log.e(TAG, "APK file does not exist: $apkPath")
                return false
            }
            
            // Create DexClassLoader
            val classLoader = DexClassLoader(
                apkPath.absolutePath,
                optimizedDexDir.absolutePath,
                null,
                context.classLoader
            )
            
            // Store the classloader for later use
            moduleClassLoaders[moduleId] = classLoader
            
            // Try to find and load the module implementation class
            val moduleClass = findModuleClass(classLoader)
            if (moduleClass == null) {
                Log.e(TAG, "Failed to find module implementation class")
                return false
            }
            
            // Create an instance of the module
            val moduleInstance = moduleClass.getDeclaredConstructor().newInstance() as IPaymentModule
            
            // Store the module
            loadedModules[moduleId] = moduleInstance
            
            Log.d(TAG, "Successfully loaded module: $moduleId (${moduleInstance.getName()}, ${moduleInstance.getSymbol()})")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading module: $moduleId", e)
            return false
        }
    }
    
    /**
     * Find the module implementation class in the APK
     */
    private fun findModuleClass(classLoader: DexClassLoader): Class<*>? {
        // Try the known implementation classes
        for (className in IMPLEMENTATION_CLASSES) {
            try {
                val moduleClass = classLoader.loadClass(className)
                if (isValidModuleClass(moduleClass)) {
                    Log.d(TAG, "Found module implementation class: $className")
                    return moduleClass
                }
            } catch (e: ClassNotFoundException) {
                // Continue to next candidate
            } catch (e: Exception) {
                Log.e(TAG, "Error checking class: $className", e)
            }
        }

        // If we get here, none of the known implementations were found
        Log.e(TAG, "Failed to find module implementation class")
        return null
    }
    
    /**
     * Check if a class is a valid module implementation
     */
    private fun isValidModuleClass(moduleClass: Class<*>): Boolean {
        try {
            val moduleInterface = Class.forName(MODULE_INTERFACE)
            return moduleInterface.isAssignableFrom(moduleClass)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking module class validity", e)
            return false
        }
    }
    
    /**
     * Unload a module
     */
    fun unloadModule(moduleId: String): Boolean {
        return if (loadedModules.containsKey(moduleId)) {
            loadedModules.remove(moduleId)
            moduleClassLoaders.remove(moduleId)
            Log.d(TAG, "Unloaded module: $moduleId")
            true
        } else {
            Log.d(TAG, "Module not loaded: $moduleId")
            false
        }
    }
    
    /**
     * Get information about loaded modules
     */
    fun getLoadedModuleInfo(): Map<String, ModuleDetails> {
        val info = mutableMapOf<String, ModuleDetails>()
        loadedModules.forEach { (id, module) ->
            info[id] = ModuleDetails(
                id = id,
                name = module.getName(),
                symbol = module.getSymbol()
            )
        }
        return info
    }

    /**
     * Get full module information for a module
     */
    fun getFullModuleInfo(moduleId: String): com.anam.wallet.model.ModuleInfo? {
        val module = getModule(moduleId) ?: return null
        return try {
            com.anam.wallet.model.ModuleInfo.fromJson(module.getModuleInfo())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing module info", e)
            null
        }
    }

    /**
     * Get accounts for a module
     */
    fun getModuleAccounts(moduleId: String): List<com.anam.wallet.model.AccountInfo>? {
        val module = getModule(moduleId) ?: return null
        return try {
            com.anam.wallet.model.AccountInfo.fromJsonArray(module.getAccounts())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing module accounts", e)
            null
        }
    }

    /**
     * Get balance summary for a module account
     */
    fun getModuleBalanceSummary(moduleId: String, address: String): com.anam.wallet.model.BalanceSummary? {
        val module = getModule(moduleId) ?: return null
        return try {
            com.anam.wallet.model.BalanceSummary.fromJson(module.getBalanceSummary(address))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing balance summary", e)
            null
        }
    }

    /**
     * Get network information for a module
     */
    fun getModuleNetworkInfo(moduleId: String): com.anam.wallet.model.NetworkInfo? {
        val module = getModule(moduleId) ?: return null
        return try {
            com.anam.wallet.model.NetworkInfo.fromJson(module.getNetworkInfo())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing network info", e)
            null
        }
    }

    /**
     * Get supported tokens for a module
     */
    fun getModuleSupportedTokens(moduleId: String): List<com.anam.wallet.model.TokenInfo>? {
        val module = getModule(moduleId) ?: return null
        return try {
            com.anam.wallet.model.TokenInfo.fromJsonArray(module.getSupportedTokens())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing supported tokens", e)
            null
        }
    }
}

/**
 * Information about a loaded module (internal use)
 */
data class ModuleDetails(
    val id: String,
    val name: String,
    val symbol: String
)