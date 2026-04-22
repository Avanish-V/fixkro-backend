package com.iotabuild.zippyhand_Admin.notification.repository

import com.iotabuild.zippyhand_Admin.notification.model.PartnerNotification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PartnerNotificationRepository : JpaRepository<PartnerNotification, UUID> {
    fun findByUserUidOrderByCreatedAtDesc(userUid: String): List<PartnerNotification>
}
