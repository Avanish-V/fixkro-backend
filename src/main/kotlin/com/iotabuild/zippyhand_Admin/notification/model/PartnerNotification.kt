package com.iotabuild.zippyhand_Admin.notification.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "partner_notifications")
class PartnerNotification(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "user_uid", nullable = false) val userUid: String,
    @Column(nullable = false) val title: String,
    @Column(nullable = false) val body: String,
    @Column(columnDefinition = "jsonb") 
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    val data: String? = null,
    @Column(name = "is_read") var isRead: Boolean = false,
    @Column(name = "created_at") val createdAt: Instant = Instant.now()
)
