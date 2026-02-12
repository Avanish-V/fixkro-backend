package com.iotabuild.zippyhand_Admin.products.models

data class CategoryResponse(
    val categoryId: Long,
    val title: String,
    val iconType: String,
    val iconValue: String,
    val themeColor: String,
    val status: String
)

data class CreateCategoryRequest(
    val title: String,
    val iconType: String,
    val iconValue: String,
    val themeColor: String,
    val status: String
)

data class UpdateCategoryRequest(
    val title: String,
    val iconType: String,
    val iconValue: String,
    val themeColor: String,
    val status: String
)
