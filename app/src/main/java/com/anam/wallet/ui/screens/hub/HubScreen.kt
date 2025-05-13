package com.anam.wallet.ui.screens.hub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch

@Composable
fun HubScreen(viewModel: HubScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize module manager
    val moduleManager = remember { ModuleManager(context) }

    // State variables
    val uiState by viewModel.uiStateFlow.collectAsState()
    var moduleId by remember { mutableStateOf("3") } // Default module ID

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
        // Title section
        item {
            Text(
                text = "Module Hub",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Module download section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Module Management",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Module ID input
                    OutlinedTextField(
                        value = moduleId,
                        onValueChange = { moduleId = it },
                        label = { Text("Module ID") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Download button
                    Button(
                        onClick = {
                            viewModel.clearStatusMessage()
                            coroutineScope.launch {
                                viewModel.downloadModule(moduleManager, moduleId)
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Download & Load Module")
                    }

                    // Download progress
                    if (uiState.isLoading) {
                        LinearProgressIndicator(
                            progress = { uiState.progress.toFloat() / 100 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    // Status message
                    if (uiState.message.isNotEmpty()) {
                        Text(
                            text = uiState.message,
                            color = if (uiState.isSuccess) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Section header for available modules if any exist
        if (uiState.moduleData.isNotEmpty()) {
            item {
                Text(
                    text = "Installed Modules",
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
        } else if (!uiState.isLoading) {
            // Display when no modules are installed
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No modules installed",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Enter a module ID and download to get started",
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