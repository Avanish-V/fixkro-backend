package com.iotabuild.zippyhand_Admin.offers

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OfferRepository : JpaRepository<OfferEntity, Long> {
    fun findByCouponCodeIgnoreCase(couponCode: String): Optional<OfferEntity>
    fun existsByCouponCodeIgnoreCase(couponCode: String): Boolean
    fun findAllByOrderByCreatedAtDesc(): List<OfferEntity>

    @org.springframework.data.jpa.repository.Query("""
        SELECT o FROM OfferEntity o 
        WHERE (o.productId = :productId OR (o.productId IS NULL AND (o.categoryId = :categoryId OR o.categoryId IS NULL)))
        AND o.status = 'ACTIVE' 
        AND o.expiryDate > :now
    """)
    fun findActiveOffersForProduct(
        @org.springframework.data.repository.query.Param("productId") productId: Long,
        @org.springframework.data.repository.query.Param("categoryId") categoryId: Long,
        @org.springframework.data.repository.query.Param("now") now: java.time.LocalDateTime
    ): List<OfferEntity>
}
