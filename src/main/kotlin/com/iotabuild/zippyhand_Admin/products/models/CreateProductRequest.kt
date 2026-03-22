package com.iotabuild.zippyhand_Admin.products.models

data class CreateProductRequest(
    val categoryId: Long,
    val title: String,
    val imageUrl: String,
    val serviceType: String = "",
    val status: Boolean = false,
    val price: Int = 0,
    val descriptions: List<ProductDescription> = emptyList()

)
