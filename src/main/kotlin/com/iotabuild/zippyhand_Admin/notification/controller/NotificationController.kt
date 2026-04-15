package com.iotabuild.zippyhand_Admin.notification.controller

import com.iotabuild.zippyhand_Admin.notification.dto.*
import com.iotabuild.zippyhand_Admin.notification.model.DeviceToken
import com.iotabuild.zippyhand_Admin.notification.model.Preferences
import com.iotabuild.zippyhand_Admin.notification.repository.DeviceTokenRepository
import com.iotabuild.zippyhand_Admin.notification.repository.PreferencesRepository
import com.iotabuild.zippyhand_Admin.notification.service.NotificationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val preferencesRepository: PreferencesRepository,
    private val deviceTokenRepository: DeviceTokenRepository
) {

    @PostMapping
    fun sendNotification(@Valid @RequestBody request: SendNotificationRequest): ResponseEntity<NotificationResponse> {
        val id = notificationService.enqueue(request)
        return ResponseEntity.accepted().body(NotificationResponse(id, "ACCEPTED", "Notification enqueued"))
    }

    @PutMapping("/preferences/{userId}")
    fun updatePreferences(@PathVariable userId: String, @RequestBody request: UpdatePreferencesRequest): ResponseEntity<Preferences> {
        val prefs = preferencesRepository.findById(userId).orElse(Preferences(userId = userId))
        
        // Manual updates or use a mapper
        val updatedPrefs = Preferences(
            userId = userId,
            emailEnabled = request.emailEnabled ?: prefs.emailEnabled,
            pushEnabled = request.pushEnabled ?: prefs.pushEnabled,
            smsEnabled = request.smsEnabled ?: prefs.smsEnabled,
            quietHoursStart = request.quietHoursStart ?: prefs.quietHoursStart,
            quietHoursEnd = request.quietHoursEnd ?: prefs.quietHoursEnd,
            timezone = request.timezone ?: prefs.timezone
        )
        
        preferencesRepository.save(updatedPrefs)
        return ResponseEntity.ok(updatedPrefs)
    }

    @PostMapping("/devices/{userId}")
    fun registerDevice(@PathVariable userId: String, @Valid @RequestBody request: RegisterDeviceRequest): ResponseEntity<DeviceToken> {
        val device = DeviceToken(userId = userId, token = request.token, platform = request.platform)
        deviceTokenRepository.save(device)
        return ResponseEntity.ok(device)
    }
}
