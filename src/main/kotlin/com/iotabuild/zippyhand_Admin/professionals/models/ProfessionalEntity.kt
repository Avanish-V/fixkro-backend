package com.iotabuild.zippyhand_Admin.professionals.models

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "professionals")
data class ProfessionalEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val firebaseUid: String = "",

    @Column(nullable = false)
    val name: String = "",

    val photo: String = "",

    @Column(nullable = false, unique = true)
    val mobile: String = "",

    val address: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val expertise: List<String> = emptyList(),

    val aadharCard: String = "",

    val idPhoto: String = "",

    var status: String = "INCOMPLETE", // "INCOMPLETE", "PENDING", "ACTIVE", "INACTIVE"

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
