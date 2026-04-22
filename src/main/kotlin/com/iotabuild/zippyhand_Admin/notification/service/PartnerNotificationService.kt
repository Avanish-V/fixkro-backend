package com.iotabuild.zippyhand_Admin.notification.service

import com.iotabuild.zippyhand_Admin.notification.model.DeviceToken
import com.iotabuild.zippyhand_Admin.notification.model.PartnerNotification
import com.iotabuild.zippyhand_Admin.notification.repository.DeviceTokenRepository
import com.iotabuild.zippyhand_Admin.notification.repository.PartnerNotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PartnerNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val notificationRepository: PartnerNotificationRepository
) {
    private val logger = LoggerFactory.getLogger(PartnerNotificationService::class.java)

    @Transactional
    fun registerDevice(userUid: String, token: String, platform: String) {
        logger.debug("PartnerNotificationService: Syncing token for userUid: {}", userUid)
        val tokens = deviceTokenRepository.findByUserId(userUid)
        val existing = tokens.find { it.token == token }
        
        if (existing != null) {
            logger.debug("PartnerNotificationService: Updating existing token")
            existing.lastActive = Instant.now()
            deviceTokenRepository.save(existing)
        } else {
            logger.info("PartnerNotificationService: Saving new device token")
            deviceTokenRepository.save(DeviceToken(userId = userUid, token = token, platform = platform))
        }
    }

    @Transactional(readOnly = true)
    fun getNotifications(userUid: String): List<PartnerNotification> {
        return notificationRepository.findByUserUidOrderByCreatedAtDesc(userUid)
    }

    @Transactional
    fun markAsRead(notificationId: UUID) {
        notificationRepository.findById(notificationId).ifPresent {
            it.isRead = true
            notificationRepository.save(it)
        }
    }

    @Transactional
    fun saveNotification(userUid: String, title: String, body: String, data: String? = null) {
        notificationRepository.save(PartnerNotification(userUid = userUid, title = title, body = body, data = data))
    }
}
