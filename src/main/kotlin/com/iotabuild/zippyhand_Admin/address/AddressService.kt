package com.iotabuild.zippyhand_Admin.address

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.address.Entity.Address
import com.iotabuild.zippyhand_Admin.address.dto.AddressResponse
import com.iotabuild.zippyhand_Admin.address.dto.CreateAddressRequest
import com.iotabuild.zippyhand_Admin.address.dto.toResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AddressService(private val addressRepository: AddressRepository) {

    @Transactional
    fun addAddress(idToken: FirebaseToken,addressRequest: CreateAddressRequest): AddressResponse{
        val uid = idToken.uid
        val address =  addressRepository.save(
            Address(
                userId = uid,
                fullName = addressRequest.fullName,
                phone = addressRequest.phone,
                addressLine1 = addressRequest.addressLine1,
                addressLine2 = addressRequest.addressLine2,
                city = addressRequest.city,
                state = addressRequest.state,
                postalCode = addressRequest.postalCode,
            )
        )
        return address.toResponse()
    }

    @Transactional
    fun getUserAddresses(idToken: FirebaseToken?): List<AddressResponse>{

        val uid = idToken?.uid

        val addresses = uid?.let { addressRepository.getAddressesByUserId(it) }

        return addresses?.map {
            it.toResponse()
        } ?: emptyList()
    }

    @Transactional
    fun getAddressById(idToken: FirebaseToken, addressId: Long): AddressResponse {
        val uid = idToken.uid
        val address = addressRepository.findById(addressId).orElse(null)
            ?: throw IllegalArgumentException("Address not found")

        if (address.userId != uid) {
            throw IllegalArgumentException("Unauthorized to access this address")
        }

        return address.toResponse()
    }

    @Transactional
    fun deleteAddress(idToken: FirebaseToken, addressId: Long) {
        val uid = idToken.uid
        val address = addressRepository.findById(addressId).orElse(null)
            ?: throw IllegalArgumentException("Address not found")
            
        if (address.userId != uid) {
            throw IllegalArgumentException("Unauthorized to delete this address")
        }
        
        addressRepository.deleteById(addressId)
    }

}