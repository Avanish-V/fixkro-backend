package com.iotabuild.zippyhand_Admin.products.service

import com.iotabuild.zippyhand_Admin.personalization.service.PersonalizationService
import com.iotabuild.zippyhand_Admin.products.dto.CategoryResponse
import com.iotabuild.zippyhand_Admin.products.dto.HomeResponse
import com.iotabuild.zippyhand_Admin.products.dto.ProductResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class CustomerProductService(
    private val queryService: ProductQueryService,
    private val personalizationService: PersonalizationService,
) {

    suspend fun getHome(): HomeResponse = coroutineScope {

        val banner = async { runCatching { personalizationService.getActiveBannerUrl() }}
        val categories = async { queryService.getAllCategories() }
        val selectedCategory = categories.await().first()
        val services = async { runCatching { queryService.getProductsByCategory(selectedCategory.categoryId,true) } }

        return@coroutineScope HomeResponse(
            banner = banner.await().getOrNull() ?: "",
            categories = categories.await(),
            services = services.await().getOrNull() ?: emptyList()
        )

    }

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
