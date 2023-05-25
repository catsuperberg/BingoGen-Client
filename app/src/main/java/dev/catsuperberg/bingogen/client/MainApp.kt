package dev.catsuperberg.bingogen.client

import android.app.Application
import android.content.Context
import dev.catsuperberg.bingogen.client.data.store.credentialsDataStore
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel
import dev.catsuperberg.bingogen.client.model.start.StartModel
import dev.catsuperberg.bingogen.client.node.multiplayer.MultiplayerNode
import dev.catsuperberg.bingogen.client.node.single.player.SinglePlayerNode
import dev.catsuperberg.bingogen.client.view.model.common.game.GameState
import dev.catsuperberg.bingogen.client.view.model.common.game.GameViewModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.GameSetupState
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.GameSetupViewModel
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupViewModel
import dev.catsuperberg.bingogen.client.view.model.multiplayer.lobby.selector.ILobbySelectorViewModel
import dev.catsuperberg.bingogen.client.view.model.multiplayer.lobby.selector.LobbySelectorViewModel
import dev.catsuperberg.bingogen.client.view.model.start.IStartViewModel
import dev.catsuperberg.bingogen.client.view.model.start.StartState
import dev.catsuperberg.bingogen.client.view.model.start.StartViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.ScopeDSL
import org.koin.dsl.bind
import org.koin.dsl.module
import dev.catsuperberg.bingogen.client.model.multiplayer.game.GameModel as MultiplayerGameModel
import dev.catsuperberg.bingogen.client.model.multiplayer.gamesetup.GameSetupModel as MultiplayerGameSetupModel
import dev.catsuperberg.bingogen.client.model.single.player.game.GameModel as SinglePlayerGameModel
import dev.catsuperberg.bingogen.client.model.single.player.gamesetup.GameSetupModel as SinglePlayerGameSetupModel

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val commonViewModels: ScopeDSL.() -> Unit = {
            viewModel {
                val state = GameSetupState()
                GameSetupViewModel(
                    navCallbacks = get(),
                    state = state,
                    model = get { parametersOf(state) },
                )
            } bind IGameSetupViewModel::class

            viewModel {
                val state = GameState()
                GameViewModel(
                    selection = get(),
                    navCallbacks = get(),
                    state = state,
                    model = get { parametersOf(state) },
                )
            } bind IGameViewModel::class
        }

        val dataStoreModule = module {
            single { get<Context>().credentialsDataStore }
        }

        val appModule = module {
            viewModel {
                val state = StartState()
                StartViewModel(
                    navCallbacks = get(),
                    state = state,
                    model = StartModel(state, get()),
                )
            } bind IStartViewModel::class
        }
        val singlePlayerModule = module {
            scope<SinglePlayerNode> {
                factoryOf(::SinglePlayerGameSetupModel) bind IGameSetupModel::class
                factoryOf(::SinglePlayerGameModel) bind IGameModel::class
            }
            scope<SinglePlayerNode>(commonViewModels)
        }
        val multiplayerModule = module {
            scope<MultiplayerNode> {
                factoryOf(::MultiplayerGameSetupModel) bind IGameSetupModel::class
                factoryOf(::MultiplayerGameModel) bind IGameModel::class
            }
            scope<MultiplayerNode>(commonViewModels)

            viewModelOf(::LobbySelectorViewModel) bind ILobbySelectorViewModel::class
        }

        startKoin {
            androidLogger()
            androidContext(this@MainApp)
            modules(
                dataStoreModule,
                appModule,
                singlePlayerModule,
                multiplayerModule,
            )
        }
    }
}
