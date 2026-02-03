package com.iotabuild.zippyhand_Admin.address

import com.google.firebase.auth.FirebaseToken
import com.google.protobuf.Api
import com.iotabuild.zippyhand_Admin.address.dto.AddressResponse
import com.iotabuild.zippyhand_Admin.address.dto.CreateAddressRequest
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/addresses")
class AddressController (private val addressService: AddressService){

    @PostMapping
    fun addAddress(@AuthenticationPrincipal idToken: FirebaseToken, @Valid @RequestBody createAddressRequest: CreateAddressRequest): ApiResponse<AddressResponse>{
        requireNotNull(idToken) { "Unauthorized" }
        val response = addressService.addAddress(idToken,createAddressRequest)
        return ApiResponse.success(response)
    }

    @GetMapping
    fun getUserAddresses(@AuthenticationPrincipal idToken: FirebaseToken): ApiResponse<List<AddressResponse>>{

        val addresses = addressService.getUserAddresses(idToken)
        return ApiResponse.success(addresses)

    }

}