package com.iotabuild.zippyhand_Admin.offers

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OfferRepository : JpaRepository<OfferEntity, Long> {
    fun findByCouponCodeIgnoreCase(couponCode: String): Optional<OfferEntity>
    fun existsByCouponCodeIgnoreCase(couponCode: String): Boolean
    fun findAllByOrderByCreatedAtDesc(): List<OfferEntity>
}
