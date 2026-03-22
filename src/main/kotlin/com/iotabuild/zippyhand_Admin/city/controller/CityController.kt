package com.iotabuild.zippyhand_Admin.city.controller

import com.iotabuild.zippyhand_Admin.city.dto.CityRequest
import com.iotabuild.zippyhand_Admin.city.dto.CityResponse
import com.iotabuild.zippyhand_Admin.city.service.CityService
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/cities")
class CityController(private val cityService: CityService) {

    @GetMapping
    fun getAllCities(): ApiResponse<List<CityResponse>> {
        return ApiResponse.success(cityService.getAllCities())
    }

    @GetMapping("/active")
    fun getActiveCities(): ApiResponse<List<CityResponse>> {
        return ApiResponse.success(cityService.getAllActiveCities())
    }

    @PostMapping
    fun createCity(@RequestBody request: CityRequest): ApiResponse<CityResponse> {
        return ApiResponse.success(cityService.createCity(request))
    }

    @PutMapping("/{id}")
    fun updateCity(
        @PathVariable id: Long,
        @RequestBody request: CityRequest
    ): ApiResponse<CityResponse> {
        return ApiResponse.success(cityService.updateCity(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteCity(@PathVariable id: Long): ApiResponse<Unit> {
        cityService.deleteCity(id)
        return ApiResponse.success(Unit)
    }
}
