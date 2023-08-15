package dev.catsuperberg.bingogen.client

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeActivity
import dev.catsuperberg.bingogen.client.node.start.StartNode
import dev.catsuperberg.bingogen.client.ui.theme.BingogenTheme

class MainActivity : NodeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BingogenTheme {
                Surface(tonalElevation = 0.2.dp) {
                    NodeHost(integrationPoint = appyxIntegrationPoint) {
                        StartNode(buildContext = it)
                    }
                }
            }
        }
    }
}
