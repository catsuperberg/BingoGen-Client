package dev.catsuperberg.bingogen.client.ui.common.game

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.State
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BackHandlerState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GameScreen(viewModel: IGameViewModel) {
    val surePrompt = stringResource(R.string.sure_exit_game_prompt)
    val snackbarHostState = remember { SnackbarHostState() }
    BackHandler(true, viewModel::onBack)

    LaunchedEffect(true) {
        viewModel.state.snackBarMessage.onEach(snackbarHostState::showSnackbar).launchIn(this)
        viewModel.state.backHandlerState.filter { it == BackHandlerState.TO_EXIT_GAME }.onEach {
            snackbarHostState.showSnackbar(surePrompt)
        }.launchIn(this)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Heading(viewModel)
                Spacer(modifier = Modifier.height(28.dp))
                Board(viewModel)
                BingoIdentifier(viewModel)
                Controls(viewModel)
            }
            DetailsOverlay(viewModel)
        }
    }
}

@Composable
private fun Heading(viewModel: IGameViewModel) {
    val boardInfo = viewModel.state.boardInfo.collectAsState()

    Text(
        text = "${boardInfo.value.game}: ${boardInfo.value.sheet}",
        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    )
}

@Composable
private fun BingoIdentifier(viewModel: IGameViewModel) {
    val state = viewModel.state.state.collectAsState()
    if (state.value == State.BINGO) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.bingo),
            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            color = Color.Red,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun Controls(viewModel: IGameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val time = viewModel.state.time.collectAsState()
        val state = viewModel.state.state.collectAsState()

        StartButtonAndTimer(viewModel::onStartBoard, state, time)

        TextButton(onClick = viewModel::onBack) {
            Text(
                text = stringResource(R.string.exit_game),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
        }
    }
}


@Composable
private fun StartButtonAndTimer(
    onStart: () -> Unit,
    state: androidx.compose.runtime.State<State>,
    time: androidx.compose.runtime.State<String>
) {
    when (state.value) {
        State.UNINITIALIZED, State.PREGAME -> {
            TextButton(onClick = onStart , Modifier.requiredWidth(140.dp)) {
                Text(
                    text = stringResource(R.string.start),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
        }
        State.ACTIVE, State.BINGO -> {
            TextButton(
                enabled = false,
                onClick = { },
                colors = ButtonDefaults.textButtonColors(
                    disabledContainerColor  = Color.DarkGray,
                    disabledContentColor  = Color.White
                ),
                modifier = Modifier.requiredWidth(140.dp),
            ) {
                Text(
                    text = time.value,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
