package com.iotabuild.zippyhand_Admin.address.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.address.dto.AddressResponse
import com.iotabuild.zippyhand_Admin.address.dto.CreateAddressRequest
import com.iotabuild.zippyhand_Admin.address.service.AddressService
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/addresses")
class AddressController(private val addressService: AddressService) {

    @PostMapping
    fun addAddress(
        @AuthenticationPrincipal idToken: FirebaseToken,
        @Valid @RequestBody createAddressRequest: CreateAddressRequest
    ): ApiResponse<AddressResponse> {
        val response = addressService.addAddress(idToken, createAddressRequest)
        return ApiResponse.success(response)
    }

    @GetMapping
    fun getUserAddresses(@AuthenticationPrincipal idToken: FirebaseToken?): ApiResponse<List<AddressResponse>> {
        requireNotNull(idToken) { "Unauthorized" }
        val addresses = addressService.getUserAddresses(idToken)
        return ApiResponse.success(addresses)
    }

    @GetMapping("/{addressId}")
    fun getAddressById(
        @AuthenticationPrincipal idToken: FirebaseToken,
        @PathVariable addressId: Long
    ): ApiResponse<AddressResponse> {
        val response = addressService.getAddressById(idToken, addressId)
        return ApiResponse.success(response)
    }

    @DeleteMapping("/{addressId}")
    fun deleteAddress(
        @AuthenticationPrincipal idToken: FirebaseToken?,
        @PathVariable addressId: Long
    ): ApiResponse<String> {
        requireNotNull(idToken) { "Unauthorized" }
        addressService.deleteAddress(idToken, addressId)
        return ApiResponse.success("Address deleted successfully")
    }
}