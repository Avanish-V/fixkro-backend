package com.iotabuild.zippyhand_Admin.jobs.controller

import com.google.firebase.auth.FirebaseToken
import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.jobs.dto.JobDetailsResponse
import com.iotabuild.zippyhand_Admin.jobs.dto.JobListItemResponse
import com.iotabuild.zippyhand_Admin.jobs.service.PartnerJobService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/partners/jobs")
class PartnerJobController(private val jobService: PartnerJobService) {

    @GetMapping("/assigned")
    fun getAssignedJobs(
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<List<JobListItemResponse>> {
        return ApiResponse.success(jobService.getAssignedJobs(token.uid))
    }

    @GetMapping("/pending")
    fun getPendingJobs(
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<List<JobListItemResponse>> {
        return ApiResponse.success(jobService.getPendingJobs(token.uid))
    }

    @GetMapping("/history")
    fun getHistory(
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<List<JobListItemResponse>> {
        return ApiResponse.success(jobService.getCompletedJobs(token.uid))
    }
    
    @GetMapping("/{id}/details")
    fun getJobDetails(
        @PathVariable id: Long,
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<JobDetailsResponse> {
        return ApiResponse.success(jobService.getJobDetails(id, token.uid))
    }

    @PostMapping("/{id}/accept")
    fun acceptJob(
        @PathVariable id: Long,
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<JobDetailsResponse> {
        return ApiResponse.success(jobService.acceptJob(id, token.uid))
    }

    @PostMapping("/{id}/reject")
    fun rejectJob(
        @PathVariable id: Long,
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<JobDetailsResponse> {
        return ApiResponse.success(jobService.rejectJob(id, token.uid))
    }

    @PostMapping("/{id}/send-otp")
    fun sendOtp(
        @PathVariable id: Long,
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<Map<String, String>> {
        val result = jobService.sendCompletionOtp(id, token.uid)
        return ApiResponse.success(mapOf("message" to result))
    }

    @PostMapping("/{id}/complete")
    fun completeJob(
        @PathVariable id: Long,
        @RequestParam otp: String,
        @AuthenticationPrincipal token: FirebaseToken
    ): ApiResponse<JobDetailsResponse> {
        return ApiResponse.success(jobService.completeJob(id, otp, token.uid))
    }
}
