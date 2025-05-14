package com.anam.wallet.core

import androidx.compose.runtime.Composable

/**
 * Interface for modules that want to provide their own UI for the detail screen.
 */
interface IModuleUI {
    /**
     * Composable function that renders the module's detail screen.
     * 
     * @param context A map containing context values that may be used by the module's UI.
     *                This can include navigation callbacks, theme information, etc.
     */
    @Composable
    fun ModuleDetailScreen(context: Map<String, Any>)
}