package com.iotabuild.zippyhand_Admin.servicearea.service

import com.iotabuild.zippyhand_Admin.servicearea.dto.*

interface ServiceAreaService {
    fun getAllServiceAreas(city: String? = null, activeOnly: Boolean? = null): List<ServiceAreaResponse>
    fun getServiceAreaById(id: Long): ServiceAreaResponse
    fun createServiceArea(request: ServiceAreaRequest): ServiceAreaResponse
    fun updateServiceArea(id: Long, request: ServiceAreaRequest): ServiceAreaResponse
    fun toggleServiceAreaStatus(id: Long, active: Boolean): ServiceAreaResponse
    fun deleteServiceArea(id: Long)
    fun bulkCreateServiceAreas(request: BulkServiceAreaRequest): List<ServiceAreaResponse>
    fun checkAvailability(pincode: String): PincodeAvailabilityResponse
    fun getActivePincodes(): List<String>
    fun getStats(): ServiceAreaStatsResponse
}
