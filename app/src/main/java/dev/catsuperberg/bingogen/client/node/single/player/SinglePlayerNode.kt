package dev.catsuperberg.bingogen.client.node.single.player

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.catsuperberg.bingogen.client.node.helper.screenNode
import dev.catsuperberg.bingogen.client.ui.single.GameScreen
import dev.catsuperberg.bingogen.client.ui.single.GameSetupScreen
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
    private val backStack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.GameSetup,
        savedStateMap = buildContext.savedStateMap,
    )
) : ParentNode<SinglePlayerNode.NavTarget>(
    navModel = backStack,
    buildContext = buildContext,
), KoinScopeComponent {
    override val scope: Scope by lazy { createScope(this) }
    private fun onFinish() {
        scope.close()
        finish()
    }

    sealed class NavTarget : Parcelable {
        @Parcelize
        object GameSetup : NavTarget()

        @Parcelize
        class Game(val selection: IGameViewModel.Selection) : NavTarget()
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            is NavTarget.GameSetup -> screenNode(buildContext) {
                val callbacks = IGameSetupViewModel.NavCallbacks(
                    onStartGame = { game: String, sheet: String, sideCount: Int ->
                        backStack.push(NavTarget.Game(IGameViewModel.Selection(game, sheet, sideCount)))
                    },
                    onBack = ::onFinish,
                )
                GameSetupScreen(get { parametersOf(callbacks) })
            }
            is NavTarget.Game -> screenNode(buildContext) {
                val callbacks = IGameViewModel.NavCallbacks(onBack = { backStack.pop() })
                GameScreen(get { parametersOf(navTarget.selection, callbacks) })
            }
        }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backStack)
    }
}
