package com.iotabuild.zippyhand_Admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.iotabuild"])
@EnableJpaRepositories(basePackages = ["com.iotabuild"])
@EntityScan(basePackages = ["com.iotabuild"])
class ZippyhandAdminApplication

fun main(args: Array<String>) {
	runApplication<ZippyhandAdminApplication>(*args)
}
