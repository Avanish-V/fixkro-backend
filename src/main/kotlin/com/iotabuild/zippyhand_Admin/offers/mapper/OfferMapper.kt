package com.iotabuild.zippyhand_Admin.offers.mapper

import com.iotabuild.zippyhand_Admin.offers.dto.OfferResponse
import com.iotabuild.zippyhand_Admin.offers.entity.OfferEntity
import com.iotabuild.zippyhand_Admin.products.entity.CategoryEntity
import com.iotabuild.zippyhand_Admin.products.entity.ProductEntity
import org.springframework.stereotype.Component

@Component
class OfferMapper {

    fun toResponse(
        offer: OfferEntity,
        product: ProductEntity? = null,
        category: CategoryEntity? = null
    ): OfferResponse {
        return OfferResponse(
            id = offer.id,
            offerCode = offer.offerCode,
            shortDescription = offer.shortDescription,
            categoryId = offer.categoryId,
            categoryName = category?.title ?: if (offer.categoryId == null) "All Categories" else "Unknown Category",
            productId = offer.productId,
            productName = product?.title ?: if (offer.productId == null) "All Products" else "Unknown Product",
            discountType = offer.discountType,
            discountValue = offer.discountValue,
            expiryDate = offer.expiryDate,
            limit = offer.usageLimit,
            userType = offer.userType,
            status = offer.status,
            createdAt = offer.createdAt,
            updatedAt = offer.updatedAt
        )
    }
}
