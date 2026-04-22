package com.iotabuild.inAppOTP

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Entity
@Table(name = "job_otps", indexes = [
    Index(name = "idx_otp_order_id", columnList = "order_id", unique = true)
])
data class OtpRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_id", nullable = false, unique = true)
    val orderId: String,

    @Column(name = "otp_hash", nullable = false)
    val otpHash: String,

    @Column(name = "otp_encrypted", nullable = false)
    val otpEncrypted: String,


    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)

@Repository
interface OtpRepository : JpaRepository<OtpRecord, Long> {
    fun findByOrderId(orderId: String): OtpRecord?
    
    @Transactional
    fun deleteByOrderId(orderId: String)
}
