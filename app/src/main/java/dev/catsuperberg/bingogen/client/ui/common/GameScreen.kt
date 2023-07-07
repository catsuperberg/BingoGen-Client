package dev.catsuperberg.bingogen.client.ui.single

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel

@Composable
fun GameScreen(viewModel: IGameViewModel) {
    Column {
        Text(text = "Game placeholder")
        Spacer(modifier = Modifier.height(30.dp))
    }
}
