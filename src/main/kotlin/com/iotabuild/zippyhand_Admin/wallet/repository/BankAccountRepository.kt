package com.iotabuild.zippyhand_Admin.wallet.repository

import com.iotabuild.zippyhand_Admin.wallet.entity.BankAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BankAccountRepository : JpaRepository<BankAccount, UUID> {
    fun findByTechnicianId(technicianId: String): BankAccount?
}
