package com.iotabuild.zippyhand_Admin.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import jakarta.annotation.PostConstruct
import com.twilio.Twilio

@Configuration
@ConfigurationProperties(prefix = "twilio")
class TwilioProperties {
    var accountSid: String = "AC6d011fe4732387256fea1bcdb661d999"
    var authToken: String = "a94aa3a822ec3b06f25cacfa0df104b5"
    var fromPhoneNumber: String = "+918787040661"
}

@Configuration
class TwilioConfig(private val properties: TwilioProperties) {
    
    @PostConstruct
    fun twilioInitializer(): Unit {
        if (properties.accountSid.isNotEmpty() && properties.authToken.isNotEmpty()) {
            Twilio.init(properties.accountSid, properties.authToken)
            println("Twilio initialized successfully")
        } else {
            println("Twilio credentials missing - SMS functionality will be disabled")
        }
    }
}
