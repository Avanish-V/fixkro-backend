package com.iotabuild.zippyhand_Admin.notification.controller

import com.iotabuild.zippyhand_Admin.notification.repository.DeviceTokenRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications/debug")
class NotificationDebugController(
    private val deviceTokenRepository: DeviceTokenRepository
) {
    @GetMapping("/tokens/{userId}")
    fun checkTokens(@PathVariable userId: String): Any {
        val tokens = deviceTokenRepository.findByUserId(userId)
        return mapOf(
            "userId" to userId,
            "tokenCount" to tokens.size,
            "tokens" to tokens.map { it.token }
        )
    }

    @GetMapping("/tokens")
    fun listAllTokens(): Any {
        val all = deviceTokenRepository.findAll()
        return mapOf(
            "totalCount" to all.size,
            "entries" to all.map { 
                mapOf("userId" to it.userId, "platform" to it.platform, "tokenSnippet" to it.token.take(10) + "...")
            }
        )
    }
}
