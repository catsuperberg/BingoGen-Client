package dev.catsuperberg.bingogen.client.model.interfaces

import dev.catsuperberg.bingogen.client.model.common.IBaseModel

interface IGameSetupModel : IBaseModel {
    fun requestGameList()
    fun requestSheetList(gameName: String)
}
