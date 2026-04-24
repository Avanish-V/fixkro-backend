package com.iotabuild.zippyhand_Admin.wallet.repository

import com.iotabuild.zippyhand_Admin.wallet.entity.WalletLedger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface WalletLedgerRepository : JpaRepository<WalletLedger, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): WalletLedger?

    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN w.type = 'CREDIT' THEN w.amount ELSE -w.amount END), 
            0.0
        ) 
        FROM WalletLedger w 
        WHERE w.technicianId = :uid AND w.status IN ('SETTLED', 'PENDING')
    """)
    fun getBalance(uid: String): BigDecimal

    @Query("SELECT COALESCE(SUM(w.amount), 0.0) FROM WalletLedger w WHERE w.technicianId = :uid AND w.entryType = 'JOB_EARNING' AND w.type = 'CREDIT'")
    fun getTotalLifetimeEarnings(uid: String): BigDecimal

    fun findTop20ByTechnicianIdOrderByCreatedAtDesc(uid: String): List<WalletLedger>
    fun findByReferenceIdAndReferenceType(referenceId: String, referenceType: String): WalletLedger?
}
