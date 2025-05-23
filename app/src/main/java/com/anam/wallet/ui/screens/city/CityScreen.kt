package com.anam.wallet.ui.screens.city

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam.wallet.R
import com.anam.wallet.model.DidLicense
import com.anam.wallet.ui.components.DidLicenseCard

@Composable
fun CityScreen() {
    // 샘플 DID 신분증 데이터
    val sampleLicense = DidLicense(
        id = "did:anam145:license:cG7J29XRkyk5DyhKEU8uL2NQgYg",
        licenseNumber = "A-123-456-7890",
        holder = "did:anam145:user:4C4e6euUhVfVq5TkS2VjcEQx5QHi",
        controller = "did:anam145:issuer:3aTrHC8Ge2KVrcoK8Bu9AJqNmHtG",
        status = "ACTIVE",
        licenseType = "일반",
        created = "2025-05-23T04:33:01.886Z",
        updated = "2025-05-23T04:33:01.886Z"
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.city_screen_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            DidLicenseCard(license = sampleLicense)
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            OutlinedButton(
                onClick = { 
                    // TODO: Add DID functionality
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add DID",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add DID",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}