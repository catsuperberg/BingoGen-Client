package dev.catsuperberg.bingogen.client.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.view.model.start.IStartViewModel

@Composable
fun StartScreen(viewModel: IStartViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
    ) {
        Text("START SCREEN")
        TextButton(onClick = viewModel::requestSinglePlayer) {
            Text(text = "Single Player")
        }
        TextButton(onClick = viewModel::requestMultiplayer) {
            Text(text = "Multiplayer")
        }
    }
}
