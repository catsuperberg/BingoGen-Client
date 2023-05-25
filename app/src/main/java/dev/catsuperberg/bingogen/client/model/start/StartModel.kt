package dev.catsuperberg.bingogen.client.model.start

import androidx.datastore.core.DataStore
import dev.catsuperberg.bingogen.client.Credentials
import dev.catsuperberg.bingogen.client.model.common.BaseModel
import dev.catsuperberg.bingogen.client.view.model.start.IStartModelReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class StartModel(
    private val receiver: IStartModelReceiver,
    private val dataStore: DataStore<Credentials>,
    modelScope: CoroutineScope = defaultScope,
) : IStartModel, BaseModel(modelScope), KoinComponent {
    init {
        scope.launch {
            dataStore.data
                .map { credentials -> credentials.serversList }
                .map { servers -> servers?.filterNotNull() ?: listOf() }
                .collect(receiver::didStoredServersChange)
        }
    }

    override fun saveServers(servers: List<String>?) {
        scope.launch {
            setServers(servers)
        }
    }

    private suspend fun setServers(servers: List<String>?) =
        withContext(Dispatchers.IO) {
            dataStore.updateData { credentials ->
                credentials.toBuilder()
                    .clearServers()
                    .addAllServers(servers)
                    .build()
            }
        }
}
