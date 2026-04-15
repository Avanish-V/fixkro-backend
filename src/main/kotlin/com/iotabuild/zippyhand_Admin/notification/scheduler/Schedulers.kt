package com.iotabuild.zippyhand_Admin.notification.scheduler

import com.iotabuild.zippyhand_Admin.notification.repository.NotificationRepository
import com.iotabuild.zippyhand_Admin.notification.service.KafkaProducerService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationRetryScheduler(
    private val notificationRepository: NotificationRepository,
    private val kafkaProducer: KafkaProducerService
) {

    @Scheduled(fixedDelay = 60000) // 1 minute
    @SchedulerLock(name = "retryPendingNotifications", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    fun retryPendingNotifications() {
        val pending = notificationRepository.findPendingOrRetrying()
        for (notification in pending) {
            kafkaProducer.publishNotificationEvent(notification.id)
        }
        if (pending.isNotEmpty()) {
            println("NotificationRetryScheduler: Retrying ${pending.size} notifications")
        }
    }
}
