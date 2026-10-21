package com.iotabuild.zippyhand_Admin.personalization.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.personalization.dto.BannerDto
import com.iotabuild.zippyhand_Admin.personalization.dto.HomeResponse
import com.iotabuild.zippyhand_Admin.personalization.service.PersonalizationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class HomeController(
    private val personalizationService: PersonalizationService
) {

    /**
     * Mobile Home endpoint returning banner and categories.
     * Maps to HomeResponse directly to match mobile client @Serializable data class HomeResponse.
     */
    @GetMapping("/home")
    fun getHome(): HomeResponse {
        return personalizationService.getHomeData()
    }


    /**
     * Optional public endpoint to fetch active banner directly
     */
    @GetMapping("/personalization/banner")
    fun getActiveBanner(): ApiResponse<BannerDto?> {
        return ApiResponse.success(personalizationService.getActiveBanner())
    }
}
