package com.iotabuild.zippyhand_Admin.products.models

import org.jetbrains.annotations.NotNull
import java.math.BigDecimal

data class ProductResponse(
    val categoryId: Long,
    val productId: Long,
    val title: String,
    val imageUrl: String,
    val serviceType: String = "",
    val status: Boolean = false,
    val price: Int = 0,
    val descriptions: List<ProductDescription> = emptyList()
)
