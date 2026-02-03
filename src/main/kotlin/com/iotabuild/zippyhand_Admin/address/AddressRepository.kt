package com.iotabuild.zippyhand_Admin.address

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.address.Entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, Long>{
    fun getAddressesByUserId(userId: String): List<Address>
}