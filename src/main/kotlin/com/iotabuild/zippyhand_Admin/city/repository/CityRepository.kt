package com.iotabuild.zippyhand_Admin.city.repository

import com.iotabuild.zippyhand_Admin.city.entity.City
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CityRepository : JpaRepository<City, Long> {
    fun findByIsActiveTrue(): List<City>
}
