package dev.catsuperberg.bingogen.client

import android.os.Bundle
import androidx.activity.compose.setContent
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeActivity
import dev.catsuperberg.bingogen.client.node.start.StartNode
import dev.catsuperberg.bingogen.client.ui.theme.BingogenTheme

class MainActivity : NodeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BingogenTheme {
                NodeHost(integrationPoint = appyxIntegrationPoint) {
                    StartNode(buildContext = it)
                }
            }
        }
    }
}
