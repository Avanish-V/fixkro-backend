package com.iotabuild.zippyhand_Admin.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.mustachejava.DefaultMustacheFactory
import com.iotabuild.zippyhand_Admin.notification.dto.SendNotificationRequest
import com.iotabuild.zippyhand_Admin.notification.model.*
import com.iotabuild.zippyhand_Admin.notification.provider.NotificationProvider
import com.iotabuild.zippyhand_Admin.notification.repository.*
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.StringReader
import java.io.StringWriter
import java.time.Instant
import java.util.*
import org.springframework.context.annotation.Lazy

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val templateRepository: TemplateRepository,
    private val eventLogRepository: EventLogRepository,
    private val preferencesRepository: PreferencesRepository,
    private val providers: List<NotificationProvider>,
    private val kafkaProducer: KafkaProducerService,
    private val objectMapper: ObjectMapper
) {
    private val mustacheFactory = DefaultMustacheFactory()

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    fun enqueue(request: SendNotificationRequest, sync: Boolean = false): String {
        val existing = notificationRepository.findByIdempotencyKey(request.idempotencyKey)
        if (existing != null) {
            return existing.id.toString()
        }

        val notification = Notification(
            userId = request.userId,
            idempotencyKey = request.idempotencyKey,
            strategy = request.strategy,
            templateId = request.templateId,
            payload = objectMapper.writeValueAsString(request.payload)
        )
        
        notificationRepository.save(notification)
        
        if (sync) {
            println("NotificationService: Synchronous delivery requested for ${notification.id}")
            processNotification(notification.id)
        } else {
            kafkaProducer.publishNotificationEvent(notification.id)
        }
        
        return notification.id.toString()
    }

    @Transactional
    fun processNotification(id: UUID) {
        val notification = notificationRepository.findById(id).orElse(null) ?: return
        if (notification.status == NotificationStatus.DELIVERED || notification.status == NotificationStatus.CANCELLED) {
            return
        }

        val prefs = preferencesRepository.findById(notification.userId).orElse(Preferences(userId = notification.userId))
        
        @Suppress("UNCHECKED_CAST")
        val payloadMap = objectMapper.readValue(notification.payload, Map::class.java) as Map<String, Any>

        val channelsToTry = determineChannels(notification.strategy, prefs)
        var delivered = false

        for (channel in channelsToTry) {
            val template = templateRepository.findTemplate(notification.templateId, "en", channel)
            if (template == null) {
                println("NotificationService: No template found for ID: ${notification.templateId}, Channel: $channel")
                continue
            }

            val content = renderTemplate(template.content, payloadMap)
            val provider = providers.find { it.channel == channel }
            
            if (provider == null) {
                println("NotificationService: No provider registered for channel: $channel")
                continue
            }

            val success = try {
                provider.send(notification.userId, content, template.subject, payloadMap)
            } catch (e: Exception) {
                println("NotificationService: Error sending via $channel: ${e.message}")
                false
            }
            
            if (success) {
                eventLogRepository.save(EventLog(notificationId = notification.id, channel = channel, status = NotificationStatus.DELIVERED))
                delivered = true
                if (notification.strategy != DeliveryStrategy.BROADCAST) break
            } else {
                eventLogRepository.save(EventLog(notificationId = notification.id, channel = channel, status = NotificationStatus.FAILED, message = "Provider failed"))
            }
        }

        notification.status = if (delivered) {
            NotificationStatus.DELIVERED
        } else {
            notification.retryCount++
            if (notification.retryCount < 3) NotificationStatus.RETRYING else NotificationStatus.FAILED
        }
        
        notification.updatedAt = Instant.now()
        notificationRepository.save(notification)
    }

    private fun renderTemplate(templateContent: String, data: Map<String, Any>): String {
        val compiledTemplate = mustacheFactory.compile(StringReader(templateContent), "template")
        val writer = StringWriter()
        compiledTemplate.execute(writer, data).flush()
        return writer.toString()
    }

    private fun determineChannels(strategy: DeliveryStrategy, prefs: Preferences): List<Channel> {
        val allowed = mutableListOf<Channel>()
        if (prefs.pushEnabled) allowed.add(Channel.PUSH)
        if (prefs.emailEnabled) allowed.add(Channel.EMAIL)
        if (prefs.smsEnabled) allowed.add(Channel.SMS)
        
        return when (strategy) {
            DeliveryStrategy.SINGLE_CHANNEL -> allowed.take(1)
            DeliveryStrategy.MULTI_CHANNEL, DeliveryStrategy.BROADCAST -> allowed
            DeliveryStrategy.FALLBACK_CHAIN -> allowed
        }
    }
}

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    fun publishNotificationEvent(notificationId: UUID) {
        kafkaTemplate.send("notifications.process", notificationId.toString())
    }
}

@Service
class KafkaConsumerService(
    @Lazy private val notificationService: NotificationService
) {
    @KafkaListener(topics = ["notifications.process"], groupId = "zippyhand-admin-group")
    fun consumeNotificationEvent(message: String) {
        try {
            val notificationId = UUID.fromString(message)
            notificationService.processNotification(notificationId)
        } catch (e: Exception) {
            // Log error
        }
    }
}
