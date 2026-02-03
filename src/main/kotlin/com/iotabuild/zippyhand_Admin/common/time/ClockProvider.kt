package com.iotabuild.zippyhand_Admin.common.time

import java.time.Instant

interface ClockProvider {
    fun now(): Instant
}
