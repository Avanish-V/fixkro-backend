package com.iotabuild.zippyhand_Admin.common.time

import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SystemClockProvider : ClockProvider {
    override fun now(): Instant = Instant.now()
}
