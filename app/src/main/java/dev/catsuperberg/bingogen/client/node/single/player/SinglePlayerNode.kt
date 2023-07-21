package dev.catsuperberg.bingogen.client.node.single.player

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.catsuperberg.bingogen.client.common.ServerAddress
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.node.helper.screenNode
import dev.catsuperberg.bingogen.client.ui.common.GameScreen
import dev.catsuperberg.bingogen.client.ui.common.GameSetupScreen
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupViewModel
import kotlinx.parcelize.Parcelize
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

class SinglePlayerNode (
    buildContext: BuildContext,
    private val server: ServerAddress,
    private val backStack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.GameSetup,
        savedStateMap = buildContext.savedStateMap,
    )
) : ParentNode<SinglePlayerNode.NavTarget>(
    navModel = backStack,
    buildContext = buildContext,
), KoinScopeComponent {
    override val scope: Scope by lazy { createScope(this) }
    override fun updateLifecycleState(state: Lifecycle.State) {
        if (state == Lifecycle.State.DESTROYED)
            scope.close()
        super.updateLifecycleState(state)
    }

    sealed class NavTarget : Parcelable {
        @Parcelize
        object GameSetup : NavTarget()

        @Parcelize
        class Game(val selection: IGameModel.Selection) : NavTarget()
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            is NavTarget.GameSetup -> screenNode(buildContext) {
                val callbacks = IGameSetupViewModel.NavCallbacks(
                    onStartGame = { game: String, sheet: String, sideCount: Int ->
                        backStack.push(NavTarget.Game(IGameModel.Selection(game, sheet, sideCount)))
                    },
                    onBack = ::finish,
                )
                GameSetupScreen(get { parametersOf(callbacks, server) } )
            }
            is NavTarget.Game -> screenNode(buildContext) {
                val callbacks = IGameViewModel.NavCallbacks(onBack = { backStack.pop() } )
                GameScreen(get { parametersOf(navTarget.selection, callbacks, server) } )
            }
        }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backStack)
    }
}
