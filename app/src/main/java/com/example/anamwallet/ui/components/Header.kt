package com.example.anamwallet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header() {
    CenterAlignedTopAppBar(
        title = { Text("Anamwallet") },
        actions = {
            IconButton(onClick = { /* 설정 버튼 클릭 시 동작 */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}