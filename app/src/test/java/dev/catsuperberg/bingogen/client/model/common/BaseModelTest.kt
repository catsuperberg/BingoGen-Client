package dev.catsuperberg.bingogen.client.model.common

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class BaseModelTest {
    private class TestModel : BaseModel(defaultScope) {
        private val emitInterval = 1L
        private val _firstFlow = MutableSharedFlow<Long>()
        val firstFlow: SharedFlow<Long> = _firstFlow
        private val _secondFlow = MutableSharedFlow<Long>()
        val secondFlow: SharedFlow<Long> = _secondFlow

        init {
            scope.launch {
                var counter = 0L
                while(true) {
                    delay(emitInterval*1)
                    _firstFlow.emit(counter++)
                }
            }
            scope.launch {
                var counter = 0L
                while(true) {
                    delay(emitInterval*2)
                    _secondFlow.emit(counter++)
                }
            }
        }
    }

    @Before
    fun setUp() {
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testCloseCancelsCoroutines() {
        val model = TestModel()
        val firstConsumer: (Long) -> Unit = mock()
        val secondConsumer: (Long) -> Unit = mock()

        GlobalScope.launch { model.firstFlow.collect(firstConsumer) }
        GlobalScope.launch { model.secondFlow.collect(secondConsumer) }

        verify(firstConsumer, timeout(100).atLeastOnce())(any())
        verify(secondConsumer, timeout(100).atLeastOnce())(any())

        model.close()
        verifyNoMoreInteractions(firstConsumer)
        verifyNoMoreInteractions(secondConsumer)
    }
}
