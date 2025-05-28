package com.anam.wallet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam.wallet.R

@Composable
fun Header(title: String? = null, showLogo: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (title != null) 80.dp else 100.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = if (title != null) 15.dp else 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLogo) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Anam Logo",
                modifier = Modifier.height(26.dp)
            )
        }
        
        title?.let {
            if (showLogo) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = it,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}