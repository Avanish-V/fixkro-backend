package com.iotabuild.zippyhand_Admin.notification.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class Channel { PUSH, SMS, EMAIL, WEBHOOK, WHATSAPP }
enum class DeliveryStrategy { SINGLE_CHANNEL, MULTI_CHANNEL, FALLBACK_CHAIN, BROADCAST }
enum class NotificationStatus { PENDING, DELIVERED, FAILED, RETRYING, CANCELLED }

@Entity
@Table(name = "notification_logs", indexes = [Index(columnList = "idempotency_key", unique = true)])
class Notification(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "user_id") val userId: String,
    @Column(name = "idempotency_key") val idempotencyKey: String,
    @Enumerated(EnumType.STRING) val strategy: DeliveryStrategy,
    @Column(name = "template_id") val templateId: String,
    @Column(columnDefinition = "jsonb") 
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    val payload: String, // Stringified JSON
    @Enumerated(EnumType.STRING) var status: NotificationStatus = NotificationStatus.PENDING,
    @Column(name = "created_at") val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "templates")
class Template(
    @Id val id: String,
    @Column(name = "template_id") val templateId: String,
    val locale: String,
    @Enumerated(EnumType.STRING) val channel: Channel,
    val content: String,
    val subject: String? = null
)

@Entity
@Table(name = "device_tokens")
class DeviceToken(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "user_uid") val userId: String,
    val token: String,
    val platform: String,
    @Column(name = "last_active") var lastActive: Instant = Instant.now()
)

@Entity
@Table(name = "preferences")
class Preferences(
    @Id @Column(name = "user_id") val userId: String,
    val emailEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val quietHoursStart: Int? = null,
    val quietHoursEnd: Int? = null,
    val timezone: String = "UTC"
)

@Entity
@Table(name = "event_logs")
class EventLog(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "notification_id") val notificationId: UUID,
    @Enumerated(EnumType.STRING) val channel: Channel,
    @Enumerated(EnumType.STRING) val status: NotificationStatus,
    val message: String? = null,
    val timestamp: Instant = Instant.now()
)
