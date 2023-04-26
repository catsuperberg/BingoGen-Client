package dev.catsuperberg.bingogen.client.ui.single

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel

@Composable
fun GameScreen(viewModel: IGameViewModel) {
    Column {
        Text(text = "Game placeholder")
        Spacer(modifier = Modifier.height(30.dp))

        val selectedGame = viewModel.selectedGame
        val selectedSheet = viewModel.selectedSheet
        Text(text = "Selected: ${selectedGame.value} | ${selectedSheet.value}")

        val board = viewModel.board
        if(board.value.isNotEmpty())
            Text(text = "Board: ${board.value.joinToString(" ")}")

        TextButton(onClick = viewModel::requestBack) {
            Text(text = "Back")
        }
    }
}
