package com.iotabuild.zippyhand_Admin.jobs.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "job_assigning")
data class JobAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "order_id", nullable = false)
    val orderId: String,
    
    @Column(name = "technician_id", nullable = false)
    val professionalId: Long,
    
    @Column(name = "uid", nullable = false)
    val uid: String,
    
    var status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, COMPLETED
    
    @Column(name = "otp")
    var otp: String? = null,
    
    @Column(name = "otp_expiry")
    var otpExpiry: LocalDateTime? = null,
    
    val assignedAt: LocalDateTime = LocalDateTime.now(),
    var acceptedAt: LocalDateTime? = null,
    var completedAt: LocalDateTime? = null
)
