package dev.catsuperberg.bingogen.client.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.Instant

class IncrementTimeGetter(scope: CoroutineScope, val tickInterval: Long = defaultTickInterval) {
    companion object {
        const val defaultTickInterval = 100L
    }
    private var time = Instant.now()

    private var tickJob = scope.launch { while(true) {
        delay(tickInterval); time = time.plus(tickInterval) } }
    fun now() = time
    fun close() = tickJob.cancel()
}
