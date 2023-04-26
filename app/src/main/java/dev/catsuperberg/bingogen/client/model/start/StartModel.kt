package dev.catsuperberg.bingogen.client.model.start

import dev.catsuperberg.bingogen.client.view.model.start.IStartModelReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartModel(private val receiver: IStartModelReceiver) : IStartModel {
    override fun requestServers() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Api call
            receiver.didLoadServers(
                listOf("localhost:1111", "0.0.0.0:2222", "127.0.0.1:3333", "192.168.1.1:4444"),
                0,
            )
        }
    }
}
