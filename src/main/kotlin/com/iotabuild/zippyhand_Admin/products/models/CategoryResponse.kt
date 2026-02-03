package com.iotabuild.zippyhand_Admin.products.models

data class CategoryResponse(
    val categoryId: Long,
    val title: String,
    val image: String
)

data class CreateCategoryRequest(
    val title: String,
    val image: String
)
