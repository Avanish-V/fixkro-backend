package com.iotabuild.zippyhand_Admin.products.dto

import jakarta.validation.constraints.NotBlank

data class CategoryResponse(
    val categoryId: Long,
    val title: String,
    val iconType: String,
    val iconValue: String,
    val themeColor: String,
    val status: String,
    val image: String? = null
)

data class CreateCategoryRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,
    val iconType: String,
    val iconValue: String,
    val themeColor: String,
    val status: String = "active"
)

data class UpdateCategoryRequest(
    val title: String,
    val iconType: String,
    val iconValue: String,
    val themeColor: String,
    val status: String
)
