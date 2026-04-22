package com.iotabuild.zippyhand_Admin.products.dto

data class ProductResponse(
    val categoryId: Long,
    val productId: Long,
    val title: String,
    val imageUrl: String,
    val serviceType: String,
    val status: Boolean,
    val price: Int,
    val descriptions: List<ProductDescriptionDto>,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val categoryImage: String = ""
)

data class ProductDescriptionDto(
    val title: String = "",
    val shortDescription: String = ""
)

data class CreateProductRequest(
    val categoryId: Long,
    val title: String,
    val imageUrl: String,
    val serviceType: String,
    val status: Boolean,
    val price: Int,
    val descriptions: List<ProductDescriptionDto>
)

data class UpdateProductRequest(
    val categoryId: Long,
    val title: String,
    val imageUrl: String,
    val serviceType: String,
    val status: Boolean,
    val price: Int,
    val descriptions: List<ProductDescriptionDto>
)
