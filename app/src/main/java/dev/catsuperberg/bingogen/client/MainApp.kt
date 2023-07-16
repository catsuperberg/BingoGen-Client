package dev.catsuperberg.bingogen.client

import android.app.Application
import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.catsuperberg.bingogen.client.api.GridMapper
import dev.catsuperberg.bingogen.client.api.IGridMapper
import dev.catsuperberg.bingogen.client.api.ITaskMapper
import dev.catsuperberg.bingogen.client.api.TaskMapper
import dev.catsuperberg.bingogen.client.common.ServerAddress
import dev.catsuperberg.bingogen.client.data.store.credentialsDataStore
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel
import dev.catsuperberg.bingogen.client.model.single.player.gamesetup.GameSetupModel
import dev.catsuperberg.bingogen.client.model.start.StartModel
import dev.catsuperberg.bingogen.client.node.multiplayer.MultiplayerNode
import dev.catsuperberg.bingogen.client.node.single.player.SinglePlayerNode
import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskRetriever
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
import kotlinx.serialization.json.Json
import okhttp3.MediaType
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
import retrofit2.Retrofit
import dev.catsuperberg.bingogen.client.model.multiplayer.game.GameModel as MultiplayerGameModel
import dev.catsuperberg.bingogen.client.model.multiplayer.gamesetup.GameSetupModel as MultiplayerGameSetupModel
import dev.catsuperberg.bingogen.client.model.single.player.game.GameModel as SinglePlayerGameModel
import dev.catsuperberg.bingogen.client.model.single.player.gamesetup.GameSetupModel as SinglePlayerGameSetupModel

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val commonViewModels: ScopeDSL.() -> Unit = {
            factoryOf(::TaskMapper) bind ITaskMapper::class
            factoryOf(::GridMapper) bind IGridMapper::class
            factoryOf(::TaskRetriever) bind ITaskRetriever::class
            factory<IGameSetupModel> { GameSetupModel(get(), get()) }
            factory {
                val server: ServerAddress = get()
                val contentType: MediaType = MediaType.get("application/json")

                Retrofit.Builder()
                    .baseUrl("http://$server/")
                    .addConverterFactory(Json.asConverterFactory(contentType))
                    .build()
            }

            viewModel {
                val state = GameSetupState()
                GameSetupViewModel(
                    navCallbacks = get(),
                    state = state,
                    model = get { parametersOf(state, get()) },
                )
            } bind IGameSetupViewModel::class

            viewModel {
                val state = GameState()
                GameViewModel(
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
