package com.iotabuild.zippyhand_Admin.personalization.service

import com.iotabuild.zippyhand_Admin.common.service.StorageService
import com.iotabuild.zippyhand_Admin.personalization.dto.*
import com.iotabuild.zippyhand_Admin.personalization.entity.BannerEntity
import com.iotabuild.zippyhand_Admin.personalization.repository.BannerRepository
import com.iotabuild.zippyhand_Admin.products.service.CustomerProductService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class PersonalizationService(
    private val bannerRepository: BannerRepository,
    private val storageService: StorageService
) {

    fun getActiveBannerUrl(): String {
        return bannerRepository.findFirstByIsActiveTrueOrderByDisplayOrderAscUpdatedAtDesc()?.imageUrl ?: ""
    }

    @Transactional
    fun saveBanner(request: UpdateBannerRequest): BannerDto {
        val banner = BannerEntity(
            imageUrl = request.imageUrl,
            title = request.title,
            linkUrl = request.linkUrl,
            isActive = request.isActive,
            displayOrder = request.displayOrder,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val saved = bannerRepository.save(banner)
        return toDto(saved)
    }

    @Transactional
    fun uploadAndSaveBanner(file: MultipartFile): BannerDto {
        val publicUrl = storageService.uploadFile(file, "banners")
        val banner = BannerEntity(
            imageUrl = publicUrl,
            isActive = true,
            displayOrder = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val saved = bannerRepository.save(banner)
        return toDto(saved)
    }

    @Transactional
    fun toggleBannerStatus(id: Long, active: Boolean): BannerDto {
        val banner = bannerRepository.findById(id)
            .orElseThrow { NoSuchElementException("Banner not found with id: $id") }
        banner.isActive = active
        banner.updatedAt = LocalDateTime.now()
        val saved = bannerRepository.save(banner)
        return toDto(saved)
    }

    @Transactional
    fun deleteBanner(id: Long) {
        val banner = bannerRepository.findById(id)
            .orElseThrow { NoSuchElementException("Banner not found with id: $id") }
        bannerRepository.delete(banner)
    }

    private fun toDto(entity: BannerEntity): BannerDto {
        return BannerDto(
            id = entity.id,
            imageUrl = entity.imageUrl,
            title = entity.title,
            linkUrl = entity.linkUrl,
            isActive = entity.isActive,
            displayOrder = entity.displayOrder,
            createdAt = entity.createdAt.toString(),
            updatedAt = entity.updatedAt.toString()
        )
    }
}
