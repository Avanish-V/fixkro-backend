package com.iotabuild.zippyhand_Admin.wallet.repository

import com.iotabuild.zippyhand_Admin.wallet.entity.WithdrawalRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WithdrawalRequestRepository : JpaRepository<WithdrawalRequest, UUID> {
    fun findByTechnicianIdOrderByCreatedAtDesc(technicianId: String): List<WithdrawalRequest>
    fun findByIdempotencyKey(idempotencyKey: String): WithdrawalRequest?
}
