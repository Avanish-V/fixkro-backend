package com.iotabuild.zippyhand_Admin.personalization.dto

data class HomeResponse(
    val banner: String,
    val categories: List<HomeCategoryDto>
)

data class HomeCategoryDto(
    val categoryId: Long,
    val title: String,
    val image: String,
    val iconType: String = "url",
    val iconValue: String = "",
    val themeColor: String = "#000000",
    val status: String = "active"
)

data class BannerDto(
    val id: Long,
    val imageUrl: String,
    val title: String?,
    val linkUrl: String?,
    val isActive: Boolean,
    val displayOrder: Int,
    val createdAt: String?,
    val updatedAt: String?
)

data class UpdateBannerRequest(
    val imageUrl: String,
    val title: String? = null,
    val linkUrl: String? = null,
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)
