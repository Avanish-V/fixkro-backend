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

    @GetMapping("/personalization/banner")
    fun getActiveBanner(): ApiResponse<String?> {
        return ApiResponse.success(personalizationService.getActiveBannerUrl())
    }
}
