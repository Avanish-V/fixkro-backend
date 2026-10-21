package com.iotabuild.zippyhand_Admin.notification.repository

import com.iotabuild.zippyhand_Admin.notification.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): Notification?
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' OR n.status = 'RETRYING'")
    fun findPendingOrRetrying(): List<Notification>
}

@Repository
interface TemplateRepository : JpaRepository<Template, String> {
    @Query("SELECT t FROM Template t WHERE t.templateId = :id AND t.locale = :locale AND t.channel = :channel")
    fun findTemplate(id: String, locale: String, channel: Channel): Template?
}

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceToken, UUID> {
    fun findByUserId(userId: String): List<DeviceToken>
    fun findByToken(token: String): DeviceToken?
    
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM DeviceToken d WHERE d.token = :token")
    fun deleteByToken(token: String)
}

@Repository
interface PreferencesRepository : JpaRepository<Preferences, String>

@Repository
interface EventLogRepository : JpaRepository<EventLog, UUID>
