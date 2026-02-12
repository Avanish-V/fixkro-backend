package com.iotabuild.zippyhand_Admin.reviews

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByProductId(productId: Long): List<Review>
}
