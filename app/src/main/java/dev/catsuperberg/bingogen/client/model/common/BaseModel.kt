package dev.catsuperberg.bingogen.client.model.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

abstract class BaseModel(
    protected val scope: CoroutineScope
) : IBaseModel {
    companion object {
        val defaultScope
            get() = CoroutineScope(Job() + Dispatchers.IO)
    }

    override fun close() {
        scope.cancel()
    }
}
