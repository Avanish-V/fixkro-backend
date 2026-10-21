package com.iotabuild.zippyhand_Admin.notification.service

import com.iotabuild.zippyhand_Admin.notification.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationScheduler(
    private val notificationRepository: NotificationRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)

    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    fun retryFailedNotifications() {
        val retryableNotifications = notificationRepository.findPendingOrRetrying()
        
        if (retryableNotifications.isEmpty()) return

        logger.info("NotificationScheduler: Found {} notifications to retry/process", retryableNotifications.size)
        
        retryableNotifications.forEach { notification ->
            try {
                // To avoid overloading the system, we only process if it's been at least 5 mins since last update
                val fiveMinutesAgo = java.time.Instant.now().minusSeconds(300)
                if (notification.updatedAt.isBefore(fiveMinutesAgo) || notification.status == com.iotabuild.zippyhand_Admin.notification.model.NotificationStatus.PENDING) {
                    notificationService.processNotification(notification.id)
                }
            } catch (e: Exception) {
                logger.error("NotificationScheduler: Failed to re-process notification ${notification.id}", e)
            }
        }
    }
}
