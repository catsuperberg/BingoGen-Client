package dev.catsuperberg.bingogen.client.ui.common.game

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.State
import dev.catsuperberg.bingogen.client.ui.theme.extendedColors
import dev.catsuperberg.bingogen.client.ui.theme.extendedTypography
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(content = { Text(data.visuals.message) } )
                },
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 120.dp)
            )
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Heading(viewModel)
                Board(viewModel)
                BingoIdentifier(viewModel)
                Controls(viewModel)
            }

            ExitButton(viewModel, Modifier.align(Alignment.BottomEnd))
            DetailsOverlay(viewModel)
        }
    }
}

@Composable
private fun ExitButton(viewModel: IGameViewModel, modifier: Modifier) {
    val backState = viewModel.state.backHandlerState.collectAsState()
    val readyToExit = remember { derivedStateOf { backState.value == BackHandlerState.TO_EXIT_GAME } }

    val iconRotation by animateFloatAsState(
        targetValue = if (readyToExit.value) 0f else 180f,
        animationSpec = tween(durationMillis = 300)
    )

    val sizeMultiplier by animateFloatAsState(
        targetValue = if (readyToExit.value) 1f else 0.6f,
        animationSpec = tween(durationMillis = 300)
    )

    val containerColor by animateColorAsState(
        targetValue = if (readyToExit.value)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.tertiaryContainer,
        animationSpec = tween(durationMillis = 300)
    )

    val contentColor by animateColorAsState(
        targetValue = if (readyToExit.value)
            MaterialTheme.colorScheme.onError
        else
            MaterialTheme.colorScheme.onTertiaryContainer,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(width = (72+14*2).dp, height = (72+28*2).dp),
    ) {
        FloatingActionButton(
            containerColor = containerColor,
            contentColor = contentColor,
            onClick = viewModel::onBack,
            shape = CircleShape,
            modifier = Modifier
                .size((sizeMultiplier * 72).dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier
                    .size((sizeMultiplier * 32).dp)
                    .rotate(iconRotation)
            )
        }
    }
}

@Composable
private fun Heading(viewModel: IGameViewModel) {
    val boardInfo = viewModel.state.boardInfo.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(vertical = 12.dp, horizontal = 6.dp),
    ) {
        Text(
            text = "${boardInfo.value.game}: ${boardInfo.value.sheet}",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BingoIdentifier(viewModel: IGameViewModel) {
    val state = viewModel.state.state.collectAsState()
    val zeroUnlessBingo = if (state.value == State.BINGO) 1f else 0f
    Text(
        text = stringResource(R.string.bingo),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.extendedColors.brightRed,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 12.dp).alpha(zeroUnlessBingo)
    )
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
    }
}


@Composable
private fun StartButtonAndTimer(
    onStart: () -> Unit,
    state: androidx.compose.runtime.State<State>,
    time: androidx.compose.runtime.State<String>
) {
    val startState = state.value in State.NonGame
    val text = if (startState) stringResource(R.string.start) else time.value

    Button(
        enabled = startState,
        colors = ButtonDefaults
            .buttonColors(disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledContentColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onStart,
        modifier = Modifier.requiredWidth(200.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.extendedTypography.buttonLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}
