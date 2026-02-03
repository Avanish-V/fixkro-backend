package com.iotabuild.zippyhand_Admin.coupon.repository

import com.iotabuild.zippyhand_Admin.coupon.models.CouponEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * Repository for coupon operations
 */
@Repository
interface CouponRepository : JpaRepository<CouponEntity, Long> {
    
    /**
     * Find coupon by code (case-insensitive)
     */
    @Query("SELECT c FROM CouponEntity c WHERE LOWER(c.code) = LOWER(:code)")
    fun findByCodeIgnoreCase(@Param("code") code: String): Optional<CouponEntity>
    
    /**
     * Check if coupon code exists (case-insensitive)
     */
    @Query("SELECT COUNT(c) > 0 FROM CouponEntity c WHERE LOWER(c.code) = LOWER(:code)")
    fun existsByCodeIgnoreCase(@Param("code") code: String): Boolean
    
    /**
     * Find all active coupons that haven't expired
     */
    @Query("SELECT c FROM CouponEntity c WHERE c.isActive = true AND c.validUntil > :now")
    fun findAllActiveCoupons(@Param("now") now: LocalDateTime): List<CouponEntity>
    
    /**
     * Find all coupons ordered by creation date
     */
    fun findAllByOrderByCreatedAtDesc(): List<CouponEntity>
}
