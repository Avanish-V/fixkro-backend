package com.iotabuild.zippyhand_Admin.address.repository

import com.iotabuild.zippyhand_Admin.address.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun getAddressesByUserId(userId: String): List<Address>
}