package com.iotabuild.zippyhand_Admin.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "users", indexes = [
    Index(name = "idx_users_firebase_uid", columnList = "firebaseUid"),
    Index(name = "idx_users_email", columnList = "email")
])
@EntityListeners(AuditingEntityListener::class)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val firebaseUid: String,

    @Column(nullable = true)
    var name: String? = null,

    @Column(nullable = true)
    var email: String? = null,

    @Column(nullable = true)
    var phoneNumber: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var role: UserRole = UserRole.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var status: UserStatus = UserStatus.ACTIVE,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)
