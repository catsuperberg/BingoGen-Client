package dev.catsuperberg.bingogen.client.ui.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.view.model.multiplayer.lobby.selector.ILobbySelectorViewModel

@Composable
fun LobbySelectorScreen(viewModel: ILobbySelectorViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
    ) {
        Text("Lobby selector")
        TextButton(onClick = viewModel::requestSetup) {
            Text(text = "Proceed to Game Setup")
        }
        TextButton(onClick = viewModel::requestBack) {
            Text(text = "Back")
        }
    }
}
