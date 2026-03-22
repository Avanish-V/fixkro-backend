package com.iotabuild.zippyhand_Admin.common.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/hello")
class HelloController {

    @GetMapping
    fun sayHello(): ResponseEntity<ApiResponse<String>> {
        println("Hello API Triggered!")
        return ResponseEntity.ok(ApiResponse.success("Hello from Zippyhand Backend!"))
    }
}
