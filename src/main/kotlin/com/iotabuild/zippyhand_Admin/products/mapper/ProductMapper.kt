package com.iotabuild.zippyhand_Admin.products.mapper

import com.iotabuild.zippyhand_Admin.products.dto.CategoryResponse
import com.iotabuild.zippyhand_Admin.products.dto.ProductDescriptionDto
import com.iotabuild.zippyhand_Admin.products.dto.ProductResponse
import com.iotabuild.zippyhand_Admin.products.entity.CategoryEntity
import com.iotabuild.zippyhand_Admin.products.entity.ProductEntity
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toProductResponse(
        product: ProductEntity,
        category: CategoryEntity? = null,
        avgRating: Double = 0.0,
        reviewCount: Int = 0
    ): ProductResponse {
        return ProductResponse(
            productId = product.productId,
            categoryId = product.categoryId,
            title = product.title,
            imageUrl = product.image,
            serviceType = product.serviceType,
            status = product.status,
            price = product.price,
            descriptions = product.descriptions.map { ProductDescriptionDto(it.title, it.shortDescription) },
            rating = avgRating,
            reviewCount = reviewCount,
            categoryImage = category?.iconValue ?: ""
        )
    }

    fun toCategoryResponse(category: CategoryEntity): CategoryResponse {
        return CategoryResponse(
            categoryId = category.categoryId,
            title = category.title,
            iconType = category.iconType,
            iconValue = category.iconValue,
            themeColor = category.themeColor,
            status = category.status,
            image = category.iconValue
        )
    }
}
