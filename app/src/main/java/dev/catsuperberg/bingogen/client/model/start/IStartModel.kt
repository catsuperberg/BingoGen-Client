package dev.catsuperberg.bingogen.client.model.start

import dev.catsuperberg.bingogen.client.model.common.IBaseModel

interface IStartModel : IBaseModel {
    fun saveServers(servers: List<String>?)
}
