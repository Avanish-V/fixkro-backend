package com.iotabuild.zippyhand_Admin.servicearea.service.impl

import com.iotabuild.zippyhand_Admin.servicearea.dto.*
import com.iotabuild.zippyhand_Admin.servicearea.entity.ServiceArea
import com.iotabuild.zippyhand_Admin.servicearea.repository.ServiceAreaRepository
import com.iotabuild.zippyhand_Admin.servicearea.service.ServiceAreaService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ServiceAreaServiceImpl(
    private val serviceAreaRepository: ServiceAreaRepository
) : ServiceAreaService {

    override fun getAllServiceAreas(city: String?, activeOnly: Boolean?): List<ServiceAreaResponse> {
        val areas = when {
            !city.isNullOrBlank() && activeOnly == true ->
                serviceAreaRepository.findByCityNameIgnoreCaseAndIsActiveTrue(city.trim())
            !city.isNullOrBlank() ->
                serviceAreaRepository.findByCityNameIgnoreCase(city.trim())
            activeOnly == true ->
                serviceAreaRepository.findByIsActiveTrue()
            else ->
                serviceAreaRepository.findAll()
        }
        return areas.sortedWith(compareBy({ it.cityName }, { it.pincode }, { it.locality }))
            .map { mapToResponse(it) }
    }

    override fun getServiceAreaById(id: Long): ServiceAreaResponse {
        val area = serviceAreaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Service area not found with id: $id") }
        return mapToResponse(area)
    }

    @Transactional
    override fun createServiceArea(request: ServiceAreaRequest): ServiceAreaResponse {
        val trimmedPincode = request.pincode.trim()
        val trimmedLocality = request.locality.trim()

        if (serviceAreaRepository.existsByPincodeAndLocalityIgnoreCase(trimmedPincode, trimmedLocality)) {
            throw IllegalArgumentException("Locality '$trimmedLocality' with pincode '$trimmedPincode' already exists")
        }

        val serviceArea = ServiceArea(
            pincode = trimmedPincode,
            locality = trimmedLocality,
            cityName = request.cityName.trim(),
            state = request.state.trim(),
            cityId = request.cityId,
            isActive = request.isActive,
            deliveryNote = request.deliveryNote?.trim(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val saved = serviceAreaRepository.save(serviceArea)
        return mapToResponse(saved)
    }

    @Transactional
    override fun updateServiceArea(id: Long, request: ServiceAreaRequest): ServiceAreaResponse {
        val area = serviceAreaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Service area not found with id: $id") }

        area.pincode = request.pincode.trim()
        area.locality = request.locality.trim()
        area.cityName = request.cityName.trim()
        area.state = request.state.trim()
        area.cityId = request.cityId
        area.isActive = request.isActive
        area.deliveryNote = request.deliveryNote?.trim()
        area.updatedAt = LocalDateTime.now()

        val saved = serviceAreaRepository.save(area)
        return mapToResponse(saved)
    }

    @Transactional
    override fun toggleServiceAreaStatus(id: Long, active: Boolean): ServiceAreaResponse {
        val area = serviceAreaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Service area not found with id: $id") }

        area.isActive = active
        area.updatedAt = LocalDateTime.now()
        val saved = serviceAreaRepository.save(area)
        return mapToResponse(saved)
    }

    @Transactional
    override fun deleteServiceArea(id: Long) {
        if (!serviceAreaRepository.existsById(id)) {
            throw NoSuchElementException("Service area not found with id: $id")
        }
        serviceAreaRepository.deleteById(id)
    }

    @Transactional
    override fun bulkCreateServiceAreas(request: BulkServiceAreaRequest): List<ServiceAreaResponse> {
        val trimmedPincode = request.pincode.trim()
        val trimmedCity = request.cityName.trim()
        val trimmedState = request.state.trim()

        val newEntities = mutableListOf<ServiceArea>()

        request.localities.forEach { rawLocality ->
            // Support comma-separated strings inside the list items as well
            rawLocality.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { loc ->
                if (!serviceAreaRepository.existsByPincodeAndLocalityIgnoreCase(trimmedPincode, loc)) {
                    newEntities.add(
                        ServiceArea(
                            pincode = trimmedPincode,
                            locality = loc,
                            cityName = trimmedCity,
                            state = trimmedState,
                            cityId = request.cityId,
                            isActive = request.isActive,
                            deliveryNote = request.deliveryNote?.trim(),
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        )
                    )
                }
            }
        }

        val saved = serviceAreaRepository.saveAll(newEntities)
        return saved.map { mapToResponse(it) }
    }

    override fun checkAvailability(pincode: String): PincodeAvailabilityResponse {
        val cleanPin = pincode.trim()
        val activeAreas = serviceAreaRepository.findByPincodeAndIsActiveTrue(cleanPin)

        if (activeAreas.isEmpty()) {
            return PincodeAvailabilityResponse(
                isServiceable = false,
                pincode = cleanPin,
                cityName = null,
                state = null,
                localities = emptyList(),
                deliveryNote = null,
                message = "Sorry, service is not available at pincode $cleanPin yet. We are expanding rapidly!"
            )
        }

        val first = activeAreas.first()
        val localityNames = activeAreas.map { it.locality }.distinct().sorted()

        return PincodeAvailabilityResponse(
            isServiceable = true,
            pincode = cleanPin,
            cityName = first.cityName,
            state = first.state,
            localities = localityNames,
            deliveryNote = first.deliveryNote,
            message = "Great! Fixkro services are available in ${first.cityName} ($cleanPin)."
        )
    }

    override fun getActivePincodes(): List<String> {
        return serviceAreaRepository.findAllActivePincodes()
    }

    override fun getStats(): ServiceAreaStatsResponse {
        return ServiceAreaStatsResponse(
            totalPincodes = serviceAreaRepository.countDistinctPincodes(),
            totalLocalities = serviceAreaRepository.count(),
            activeLocalities = serviceAreaRepository.countByIsActiveTrue(),
            totalCities = serviceAreaRepository.countDistinctCities()
        )
    }

    private fun mapToResponse(entity: ServiceArea): ServiceAreaResponse {
        return ServiceAreaResponse(
            id = entity.id,
            pincode = entity.pincode,
            locality = entity.locality,
            cityName = entity.cityName,
            state = entity.state,
            cityId = entity.cityId,
            isActive = entity.isActive,
            deliveryNote = entity.deliveryNote,
            createdAt = entity.createdAt.toString(),
            updatedAt = entity.updatedAt.toString()
        )
    }
}
