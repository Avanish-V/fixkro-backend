package com.iotabuild.zippyhand_Admin.professionals.entity

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
    val email: String = "",
    @Column(nullable = false)
    val name: String = "",
    val photo: String = "",
    val mobile: String = "",
    val address: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val expertise: List<String> = emptyList(),

    val aadhar: String = "",
    var status: String = "INCOMPLETE",

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
