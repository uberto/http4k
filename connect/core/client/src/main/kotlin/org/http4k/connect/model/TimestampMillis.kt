package org.http4k.connect.model

import dev.forkhandles.values.LongValue
import dev.forkhandles.values.LongValueFactory
import dev.forkhandles.values.minValue
import java.time.Instant

/**
 * Note: This timestamp is measured to the epoch milli
 */
class TimestampMillis private constructor(value: Long) : LongValue(value) {
    fun toInstant(): Instant = Instant.ofEpochMilli(value)

    companion object : LongValueFactory<TimestampMillis>(::TimestampMillis, 0L.minValue) {
        fun of(value: Instant) = TimestampMillis(value.toEpochMilli())
    }
}
