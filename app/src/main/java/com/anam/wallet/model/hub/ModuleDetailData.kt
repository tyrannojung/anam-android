package com.anam.wallet.model.hub

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 모듈 상세 정보 데이터
 * Hub의 ModuleDetailScreen에서 사용
 */
data class ModuleDetailData(
    val id: String,
    val name: String,
    val description: String,
    val longDescription: String,
    val category: String,
    val developer: String,
    val version: String,
    val size: String,
    val downloads: String,
    val rating: Float,
    val totalRatings: Int,
    val lastUpdated: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val screenshots: List<String>
)