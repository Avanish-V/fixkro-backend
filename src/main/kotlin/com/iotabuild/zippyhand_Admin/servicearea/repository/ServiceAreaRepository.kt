package com.iotabuild.zippyhand_Admin.servicearea.repository

import com.iotabuild.zippyhand_Admin.servicearea.entity.ServiceArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ServiceAreaRepository : JpaRepository<ServiceArea, Long> {

    fun findByPincode(pincode: String): List<ServiceArea>

    fun findByPincodeAndIsActiveTrue(pincode: String): List<ServiceArea>

    fun findByCityNameIgnoreCase(cityName: String): List<ServiceArea>

    fun findByCityNameIgnoreCaseAndIsActiveTrue(cityName: String): List<ServiceArea>

    fun findByIsActiveTrue(): List<ServiceArea>

    fun existsByPincodeAndLocalityIgnoreCase(pincode: String, locality: String): Boolean

    @Query("SELECT COUNT(DISTINCT s.pincode) FROM ServiceArea s WHERE s.isActive = true")
    fun countDistinctActivePincodes(): Long

    @Query("SELECT COUNT(DISTINCT s.pincode) FROM ServiceArea s")
    fun countDistinctPincodes(): Long

    @Query("SELECT COUNT(DISTINCT LOWER(s.cityName)) FROM ServiceArea s")
    fun countDistinctCities(): Long

    fun countByIsActiveTrue(): Long

    @Query("SELECT DISTINCT s.pincode FROM ServiceArea s WHERE s.isActive = true ORDER BY s.pincode ASC")
    fun findAllActivePincodes(): List<String>
}
