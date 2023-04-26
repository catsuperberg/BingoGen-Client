package dev.catsuperberg.bingogen.client.node.start

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dev.catsuperberg.bingogen.client.node.helper.screenNode
import dev.catsuperberg.bingogen.client.node.multiplayer.MultiplayerNode
import dev.catsuperberg.bingogen.client.node.single.player.SinglePlayerNode
import dev.catsuperberg.bingogen.client.ui.start.StartScreen
import dev.catsuperberg.bingogen.client.view.model.start.IStartViewModel
import kotlinx.parcelize.Parcelize
import org.koin.androidx.compose.get
import org.koin.core.parameter.parametersOf

class StartNode(
    buildContext: BuildContext,
    private val backStack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.StartScreen,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<StartNode.NavTarget>(
    navModel = backStack,
    buildContext = buildContext
) {
    sealed class NavTarget : Parcelable {
        @Parcelize
        object StartScreen : NavTarget()

        @Parcelize
        object SinglePlayerScreen : NavTarget()

        @Parcelize
        object MultiplayerScreen : NavTarget()
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            is NavTarget.StartScreen -> screenNode(buildContext) {
                val callbacks = IStartViewModel.NavCallbacks(
                    onSinglePlayer = { backStack.push(NavTarget.SinglePlayerScreen) },
                    onMultiplayer = { backStack.push(NavTarget.MultiplayerScreen) },
                )
                StartScreen(get { parametersOf(callbacks) })
            }
            is NavTarget.SinglePlayerScreen -> SinglePlayerNode(buildContext)
            is NavTarget.MultiplayerScreen -> MultiplayerNode(buildContext)
        }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backStack)
    }

    override fun onChildFinished(child: Node) {
        backStack.singleTop(NavTarget.StartScreen)
    }
}
