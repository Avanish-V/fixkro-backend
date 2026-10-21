package com.iotabuild.zippyhand_Admin.personalization.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.personalization.dto.*
import com.iotabuild.zippyhand_Admin.personalization.service.PersonalizationService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/admin/personalization")
class AdminPersonalizationController(
    private val personalizationService: PersonalizationService
) {

    @GetMapping("/banner")
    fun getActiveBanner(): ApiResponse<BannerDto?> {
        return ApiResponse.success(personalizationService.getActiveBanner())
    }

    @GetMapping("/banners")
    fun getAllBanners(): ApiResponse<List<BannerDto>> {
        return ApiResponse.success(personalizationService.getAllBanners())
    }

    @PostMapping("/banner")
    fun updateBanner(@RequestBody request: UpdateBannerRequest): ApiResponse<BannerDto> {
        return ApiResponse.success(personalizationService.saveBanner(request))
    }

    @PostMapping("/banner/upload")
    fun uploadBanner(@RequestParam("file") file: MultipartFile): ApiResponse<BannerDto> {
        return ApiResponse.success(personalizationService.uploadAndSaveBanner(file))
    }

    @PatchMapping("/banner/{id}/status")
    fun toggleBannerStatus(@PathVariable id: Long, @RequestParam active: Boolean): ApiResponse<BannerDto> {
        return ApiResponse.success(personalizationService.toggleBannerStatus(id, active))
    }

    @DeleteMapping("/banner/{id}")
    fun deleteBanner(@PathVariable id: Long): ApiResponse<String> {
        personalizationService.deleteBanner(id)
        return ApiResponse.success("Banner deleted successfully")
    }

    @GetMapping("/home-preview")
    fun getHomePreview(): ApiResponse<HomeResponse> {
        return ApiResponse.success(personalizationService.getHomeData())
    }
}
