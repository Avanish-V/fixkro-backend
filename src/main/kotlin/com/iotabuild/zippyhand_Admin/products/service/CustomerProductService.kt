package com.iotabuild.zippyhand_Admin.products.service

import com.iotabuild.zippyhand_Admin.products.dto.CategoryResponse
import com.iotabuild.zippyhand_Admin.products.dto.ProductResponse
import org.springframework.stereotype.Service

@Service
class CustomerProductService(
    private val queryService: ProductQueryService
) {

    fun getAllCategories(): List<CategoryResponse> {
        return queryService.getAllCategories().filter { it.status == "active" }
    }

    fun getCategoryById(id: Long): CategoryResponse {
        return queryService.getCategory(id)
    }

    fun getProductsByCategory(categoryId: Long): List<ProductResponse> {
        return queryService.getProductsByCategory(categoryId, status = true)
    }

    fun getAllActiveProducts(): List<ProductResponse> {
        return queryService.getAllProducts(status = true)
    }

    fun getProductDetails(id: Long): ProductResponse {
        return queryService.getProduct(id)
    }
}
