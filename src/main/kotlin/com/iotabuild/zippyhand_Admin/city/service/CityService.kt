package com.iotabuild.zippyhand_Admin.city.service

import com.iotabuild.zippyhand_Admin.city.dto.CityRequest
import com.iotabuild.zippyhand_Admin.city.dto.CityResponse
import com.iotabuild.zippyhand_Admin.city.entity.City
import com.iotabuild.zippyhand_Admin.city.repository.CityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CityService(private val cityRepository: CityRepository) {

    fun getAllCities(): List<CityResponse> {
        val cities = cityRepository.findAll()
        return cities.map { mapToResponse(it) }
    }

    fun getAllActiveCities(): List<CityResponse> {
        val cities = cityRepository.findByIsActiveTrue()
        
        // If no cities in DB, return some defaults for initialization
        if (cities.isEmpty() && cityRepository.count() == 0L) {
            return listOf(
                CityResponse(1, "Agra", "Uttar Pradesh", "282001", "🏰", true),
                CityResponse(2, "Delhi", "Delhi", "110001", "🕌", true),
                CityResponse(3, "Mumbai", "Maharashtra", "400001", "🏙️", true),
                CityResponse(4, "Bangalore", "Karnataka", "560001", "🌳", true),
                CityResponse(5, "Pune", "Maharashtra", "411001", "⛰️", true),
                CityResponse(6, "Hyderabad", "Telangana", "500001", "🏢", true)
            )
        }
        
        return cities.map { mapToResponse(it) }
    }

    @Transactional
    fun createCity(request: CityRequest): CityResponse {
        val city = City(
            name = request.name,
            state = request.state,
            pincode = request.pincode,
            symbol = request.symbol,
            isActive = request.isActive
        )
        return mapToResponse(cityRepository.save(city))
    }

    @Transactional
    fun updateCity(id: Long, request: CityRequest): CityResponse {
        val city = cityRepository.findById(id).orElseThrow { RuntimeException("City not found") }
        val updatedCity = city.copy(
            name = request.name,
            state = request.state,
            pincode = request.pincode,
            symbol = request.symbol,
            isActive = request.isActive,
            updatedAt = java.time.LocalDateTime.now()
        )
        return mapToResponse(cityRepository.save(updatedCity))
    }

    @Transactional
    fun deleteCity(id: Long) {
        cityRepository.deleteById(id)
    }

    private fun mapToResponse(city: City): CityResponse {
        return CityResponse(
            id = city.id,
            name = city.name,
            state = city.state,
            pincode = city.pincode,
            symbol = city.symbol,
            isActive = city.isActive
        )
    }
}
