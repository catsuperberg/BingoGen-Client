package dev.catsuperberg.bingogen.client.node.helper

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node

internal fun screenNode(buildContext: BuildContext, content: @Composable () -> Unit): Node =
    node(buildContext) { modifier ->
        Box(
            modifier = modifier
        ) {
            content()
        }
    }
