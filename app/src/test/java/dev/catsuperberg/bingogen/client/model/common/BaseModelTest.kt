package dev.catsuperberg.bingogen.client.model.common

import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import kotlin.test.fail

class BaseModelTest {
    private class TestModel : BaseModel(defaultScope) {
        private val firstEmitInterval = 1L
        private val secondEmitInterval = firstEmitInterval*2
        private val _firstFlow = MutableSharedFlow<Long>()
        private val _secondFlow = MutableSharedFlow<Long>()
        val jobs: List<Job> = listOf(
            scope.launch {
                var counter = 0L
                while(true) {
                    delay(firstEmitInterval)
                    _firstFlow.emit(counter++)
                }
            },
            scope.launch {
                var counter = 0L
                while(true) {
                    delay(secondEmitInterval)
                    _secondFlow.emit(counter++)
                }
            }
        )
    }

    @Before
    fun setUp() {
    }

    @Test
    fun testCloseCancelsCoroutines() = runBlocking {
        val model = TestModel()
        model.close()
        try {
            withTimeout(200) {
                model.jobs.forEach { it.join() }
            }
        } catch (e: TimeoutCancellationException) {
            fail("Coroutines weren't stopped: \n $e")
        }
    }
}
