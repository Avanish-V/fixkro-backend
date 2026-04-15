package com.iotabuild.zippyhand_Admin.notification

import com.iotabuild.zippyhand_Admin.notification.model.Channel
import com.iotabuild.zippyhand_Admin.notification.model.Template
import com.iotabuild.zippyhand_Admin.notification.repository.TemplateRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

import org.springframework.jdbc.core.JdbcTemplate

@Component
class NotificationDataInitializer(
    private val templateRepository: TemplateRepository,
    private val jdbcTemplate: JdbcTemplate
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        createShedlockTable()
        seedTemplates()
    }

    private fun createShedlockTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS shedlock(
                name VARCHAR(64) NOT NULL, 
                lock_until TIMESTAMP NOT NULL,
                locked_at TIMESTAMP NOT NULL, 
                locked_by VARCHAR(255) NOT NULL, 
                PRIMARY KEY (name)
            )
        """.trimIndent())
    }

    private fun seedTemplates() {
        val templates = listOf(
            Template(
                id = "NEW_JOB_ASSIGNED_PUSH_en",
                templateId = "NEW_JOB_ASSIGNED",
                locale = "en",
                channel = Channel.PUSH,
                content = "New job assigned: {{productTitle}} for {{customerName}} at {{scheduledAt}}. Address: {{address}}",
                subject = "New Job Assigned"
            ),
            Template(
                id = "NEW_JOB_ASSIGNED_EMAIL_en",
                templateId = "NEW_JOB_ASSIGNED",
                locale = "en",
                channel = Channel.EMAIL,
                content = """
                    <h3>New Job Assigned</h3>
                    <p>Order ID: {{orderId}}</p>
                    <p>Product: {{productTitle}}</p>
                    <p>Customer: {{customerName}}</p>
                    <p>Scheduled At: {{scheduledAt}}</p>
                    <p>Address: {{address}}</p>
                """.trimIndent(),
                subject = "Zippyhand: New Job Assigned #{{orderId}}"
            ),
            Template(
                id = "NEW_JOB_ASSIGNED_SMS_en",
                templateId = "NEW_JOB_ASSIGNED",
                locale = "en",
                channel = Channel.SMS,
                content = "Zippyhand: New job assigned! {{productTitle}} at {{scheduledAt}}. Details: {{orderId}}",
                subject = null
            )
        )

        for (template in templates) {
            if (templateRepository.findTemplate(template.templateId, template.locale, template.channel) == null) {
                templateRepository.save(template)
            }
        }
    }
}
