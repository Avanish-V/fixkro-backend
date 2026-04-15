package com.iotabuild.zippyhand_Admin.notification.provider

import com.iotabuild.zippyhand_Admin.notification.config.TwilioProperties

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification as FcmNotification
import com.iotabuild.zippyhand_Admin.notification.model.Channel
import com.iotabuild.zippyhand_Admin.notification.repository.DeviceTokenRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import org.slf4j.LoggerFactory

interface NotificationProvider {
    fun send(userId: String, content: String, subject: String?, payload: Map<String, Any>): Boolean
    val channel: Channel
}
@Component
class FcmProvider(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebaseMessaging: FirebaseMessaging
) : NotificationProvider {
    private val logger = LoggerFactory.getLogger(FcmProvider::class.java)
    override val channel = Channel.PUSH

    override fun send(userId: String, content: String, subject: String?, payload: Map<String, Any>): Boolean {
        logger.info("FCM Provider: Looking up tokens for user ID: '{}'", userId)
        val tokens = deviceTokenRepository.findByUserId(userId).map { it.token }
        logger.info("FCM Provider: Found {} tokens for user {}", tokens.size, userId)
        
        if (tokens.isEmpty()) {
            logger.warn("FCM Provider: No tokens found for user $userId. Push notification skipped.")
            return false
        }

        return try {
            val androidConfig = AndroidConfig.builder()
                .setNotification(
                    AndroidNotification.builder()
                        .setChannelId("zippyhand_alerts")
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .setSound("default")
                        .build()
                )
                .build()

            val messages = tokens.map { token ->
                com.google.firebase.messaging.Message.builder()
                    .setToken(token)
                    .setAndroidConfig(androidConfig)
                    .setNotification(
                        FcmNotification.builder()
                            .setTitle(subject ?: "Zippyhand Update")
                            .setBody(content)
                            .build()
                    )
                    .putAllData(payload.mapValues { it.value.toString() })
                    .build()
            }

            val response = firebaseMessaging.sendEach(messages)
            logger.info("FCM Provider: Sent via v1 API. Success: {}, Failure: {}", response.successCount, response.failureCount)
            
            if (response.failureCount > 0) {
                response.responses.forEachIndexed { index, resp ->
                    if (!resp.isSuccessful) {
                        logger.error("FCM Provider: Token {} failed with error code: {}. Message: {}", 
                            tokens[index].take(10) + "...", resp.exception.messagingErrorCode, resp.exception.message)
                    }
                }
            }
            response.successCount > 0
        } catch (e: com.google.firebase.messaging.FirebaseMessagingException) {
            logger.error("FCM Provider: Firebase Error [{}]: {}", e.messagingErrorCode, e.message)
            if (e.messagingErrorCode?.toString() == "UNREGISTERED" || e.messagingErrorCode?.toString() == "INVALID_ARGUMENT") {
                logger.warn("FCM Provider: Token might be stale or invalid. Consider cleaning up tokens for user {}", userId)
            }
            false
        } catch (e: Exception) {
            logger.error("FCM Provider: Unexpected error sending to $userId", e)
            false
        }
    }
}

@Component
class TwilioSmsProvider(private val properties: TwilioProperties) : NotificationProvider {
    override val channel = Channel.SMS
    private val logger = LoggerFactory.getLogger(TwilioSmsProvider::class.java)

    override fun send(userId: String, content: String, subject: String?, payload: Map<String, Any>): Boolean {
        return try {
            val phoneNumber = payload["phone_number"] as? String
                ?: throw IllegalArgumentException("phone_number must be provided in payload for SMS channel")

            val message = com.twilio.rest.api.v2010.account.Message.creator(
                com.twilio.type.PhoneNumber(phoneNumber),
                com.twilio.type.PhoneNumber(properties.fromPhoneNumber),
                content
            ).create()

            logger.info("Twilio Provider: SMS sent to {}. SID: {}", phoneNumber, message.sid)
            true
        } catch (e: Exception) {
            logger.error("Twilio Provider: Failed to send SMS to $userId", e)
            false
        }
    }
}

@Component
class SendGridEmailProvider : NotificationProvider {
    override val channel = Channel.EMAIL
    override fun send(userId: String, content: String, subject: String?, payload: Map<String, Any>): Boolean {
        // Integrate with SendGrid SDK
        println("SendGrid Provider: Sending Email to $userId: $subject - $content")
        return true
    }
}

@Component
class WebhookProvider : NotificationProvider {
    override val channel = Channel.WEBHOOK
    override fun send(userId: String, content: String, subject: String?, payload: Map<String, Any>): Boolean {
        println("Webhook Provider: Sending to $userId")
        return true
    }
}

@Component
class WhatsAppProvider : NotificationProvider {
    override val channel = Channel.WHATSAPP
    override fun send(userId: String, content: String, subject: String?, payload: Map<String, Any>): Boolean {
        println("WhatsApp Provider: Sending to $userId: $content")
        return true
    }
}

