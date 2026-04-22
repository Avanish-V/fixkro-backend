package com.iotabuild.zippyhand_Admin.notification.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.notification.model.PartnerNotification
import com.iotabuild.zippyhand_Admin.notification.service.PartnerNotificationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/partners/notifications")
class PartnerNotificationController(
    private val notificationService: PartnerNotificationService
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(PartnerNotificationController::class.java)

    @PostMapping("/devices")
    fun registerDevice(
        @AuthenticationPrincipal token: FirebaseToken,
        @RequestBody request: DeviceRegistrationRequest
    ): ApiResponse<Map<String, String>> {
        logger.info("PartnerNotificationController: Registering device for UID: {}, Token: {}", token.uid, request.token)
        try {
            notificationService.registerDevice(token.uid, request.token, request.platform)
            return ApiResponse.success(mapOf("status" to "success", "message" to "Device registered"))
        } catch (e: Exception) {
            logger.error("PartnerNotificationController: Error registering device", e)
            throw e
        }
    }

    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<List<PartnerNotification>> {
        return ApiResponse.success(notificationService.getNotifications(token.uid))
    }

    @PatchMapping("/{id}/read")
    fun markAsRead(
        @AuthenticationPrincipal token: FirebaseToken,
        @PathVariable id: UUID
    ): ApiResponse<Map<String, String>> {
        notificationService.markAsRead(id)
        return ApiResponse.success(mapOf("status" to "success"))
    }
}

data class DeviceRegistrationRequest(
    val token: String,
    val platform: String = "ANDROID"
)
