package com.iotabuild.zippyhand_Admin.offers.repository

import com.iotabuild.zippyhand_Admin.offers.entity.OfferEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface OfferRepository : JpaRepository<OfferEntity, Long> {
    fun findByOfferCodeIgnoreCase(offerCode: String): Optional<OfferEntity>
    fun existsByOfferCodeIgnoreCase(offerCode: String): Boolean
    fun findAllByOrderByCreatedAtDesc(): List<OfferEntity>

    @Query("""
        SELECT o FROM OfferEntity o 
        WHERE (o.productId = :productId OR (o.productId IS NULL AND (o.categoryId = :categoryId OR o.categoryId IS NULL)))
        AND o.status = com.iotabuild.zippyhand_Admin.offers.entity.OfferStatus.ACTIVE 
        AND o.expiryDate > :now
    """)
    fun findActiveOffersForProduct(
        @Param("productId") productId: Long,
        @Param("categoryId") categoryId: Long,
        @Param("now") now: LocalDateTime
    ): List<OfferEntity>
}
