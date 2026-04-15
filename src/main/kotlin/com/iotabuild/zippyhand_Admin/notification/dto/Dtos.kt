package com.iotabuild.zippyhand_Admin.notification.dto
import com.iotabuild.zippyhand_Admin.notification.model.DeliveryStrategy
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class SendNotificationRequest(
    @field:NotBlank val userId: String,
    @field:NotBlank val idempotencyKey: String,
    val strategy: DeliveryStrategy = DeliveryStrategy.MULTI_CHANNEL,
    @field:NotBlank val templateId: String,
    @field:NotEmpty val payload: Map<String, Any>
)

data class NotificationResponse(
    val id: String,
    val status: String,
    val message: String
)

data class UpdatePreferencesRequest(
    val emailEnabled: Boolean?,
    val pushEnabled: Boolean?,
    val smsEnabled: Boolean?,
    val quietHoursStart: Int?,
    val quietHoursEnd: Int?,
    val timezone: String?
)

data class RegisterDeviceRequest(
    @field:NotBlank val token: String,
    @field:NotBlank val platform: String
)
