package com.iotabuild.zippyhand_Admin.personalization.repository

import com.iotabuild.zippyhand_Admin.personalization.entity.BannerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BannerRepository : JpaRepository<BannerEntity, Long> {
    fun findFirstByIsActiveTrueOrderByDisplayOrderAscUpdatedAtDesc(): BannerEntity?
    fun findAllByOrderByDisplayOrderAscUpdatedAtDesc(): List<BannerEntity>
}
