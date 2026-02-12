package com.iotabuild.zippyhand_Admin.common.dashboard

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/stats")
    fun getDashboardStats(): ResponseEntity<ApiResponse<DashboardStats>> {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboardStats()))
    }
}
