package com.iotabuild.zippyhand_Admin.order

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.time.LocalDateTime

/**
 * Entity tracking the assignment of jobs to technicians/partners.
 * Note: partnerId and technicianId refer to the same professional. 
 * We use the database Long ID (technicianId) for the relationship.
 */
@Entity
@Table(name = "job_assigning")
data class JobAssigning(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Column(name = "technician_id", nullable = false)
    val technicianId: Long, // Database ID for the technician

    @Column(name = "uid", nullable = false)
    val uid: String,

    var status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, CANCELLED

    val assignedAt: LocalDateTime = LocalDateTime.now(),
    var acceptedAt: LocalDateTime? = null

    )

interface JobAssigningRepository : JpaRepository<JobAssigning, Long> {
    fun findByOrderId(orderId: String): List<JobAssigning>
    fun findTopByOrderIdOrderByAssignedAtDesc(orderId: String): JobAssigning?
}
