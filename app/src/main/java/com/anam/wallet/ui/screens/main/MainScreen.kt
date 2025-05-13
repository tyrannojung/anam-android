package com.anam.wallet.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anam.wallet.ModuleManager
import com.anam.wallet.ui.components.ModuleCard

@Composable
fun MainScreen(viewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current

    // Initialize module manager
    val moduleManager = remember { ModuleManager(context) }

    // State variables
    val uiState by viewModel.uiStateFlow.collectAsState()

    // Refresh module data on first composition
    LaunchedEffect(moduleManager) {
        viewModel.refreshModuleData(moduleManager)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp - 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        // Section header for loaded modules if any exist
        if (uiState.moduleData.isNotEmpty()) {
            item {
                Text(
                    text = "Loaded Modules",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Start
                )
            }

            // Display module cards
            items(uiState.moduleData.toList()) { (moduleId, moduleData) ->
                ModuleCard(
                    moduleInfo = moduleData.moduleInfo,
                    networkInfo = moduleData.networkInfo,
                    accountInfo = moduleData.accounts,
                    balanceSummary = moduleData.balanceSummary,
                    onUnloadClick = {
                        viewModel.unloadModule(moduleManager, moduleId)
                    }
                )
            }
        }

        // Display message when no modules are loaded
        item {
            if (uiState.moduleData.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No modules loaded",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Go to Hub to download and install modules",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        }
    }
}