package dev.catsuperberg.bingogen.client.ui.single

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupViewModel

@Composable
fun GameSetupScreen(viewModel: IGameSetupViewModel) {
    Column {
        val gameSelection by viewModel.state.gameSelection.collectAsState()
        Text(text = "Game selection:")
        gameSelection.forEach {
            Text(text = "- $it")
        }

        val sheetSelection by viewModel.state.sheetSelection.collectAsState()
        Text(text = "Sheet selection:")
        sheetSelection.forEach {
            Text(text = "- $it")
        }


        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "Game setup placeholder")
        TextButton(onClick = viewModel::onDone) {
            Text(text = "Start game")
        }
        TextButton(onClick = viewModel::onBack) {
            Text(text = "Back")
        }
    }
}
