package com.iotabuild.zippyhand_Admin.user.config

import com.iotabuild.zippyhand_Admin.user.entity.UserRole
import com.iotabuild.zippyhand_Admin.user.entity.UserStatus
import com.iotabuild.zippyhand_Admin.user.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.slf4j.LoggerFactory

@Configuration
class UserDataMigrationConfig {
    private val logger = LoggerFactory.getLogger(UserDataMigrationConfig::class.java)

    @Bean
    fun migrateUserData(userRepository: UserRepository): CommandLineRunner {
        return CommandLineRunner {
            val users = userRepository.findAll()
            var updatedCount = 0
            users.forEach { user ->
                var updated = false
                if (user.role == null) {
                    user.role = UserRole.USER
                    updated = true
                }
                if (user.status == null) {
                    user.status = UserStatus.ACTIVE
                    updated = true
                }
                if (updated) {
                    userRepository.save(user)
                    updatedCount++
                }
            }
            if (updatedCount > 0) {
                logger.info("Migrated $updatedCount users with missing role/status data.")
            }
        }
    }
}
