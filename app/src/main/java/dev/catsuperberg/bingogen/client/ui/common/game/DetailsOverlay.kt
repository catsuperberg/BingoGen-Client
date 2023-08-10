package dev.catsuperberg.bingogen.client.ui.common.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.common.TaskStatus
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun DetailsOverlay(viewModel: IGameViewModel) {
    val isActive = remember { mutableStateOf(false) }
    val details = remember { mutableStateOf(IGameViewModel.TaskDetails.Empty) }

    LaunchedEffect(true) {
        viewModel.state.details.onEach { raw ->
            raw?.also { details.value = it }
            (raw != null).also { if (it != isActive.value) isActive.value = it }
        }.launchIn(this)
    }

    val timeRemaining = remember { derivedStateOf { details.value.timeRemaining } }
    val detailsWithoutTimer = remember { derivedStateOf { details.value.copy(timeRemaining = null) } }
    val timerRunning = remember { derivedStateOf { detailsWithoutTimer.value.status in TaskStatus.WithActiveTimer } }
    val doneToggleIsActive =
        remember { derivedStateOf { detailsWithoutTimer.value.keptFromStart == null && timeRemaining.value == null } }
    val suppressButtons = remember { derivedStateOf { detailsWithoutTimer.value.status in TaskStatus.Finished } }

    if (isActive.value) {
        Box() {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.onCloseDetails() },
                color = Color.White.copy(alpha = 0.8f),
            ) {}
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                DescriptionText(detailsWithoutTimer.value.description)
                KeptCheckbox(detailsWithoutTimer) { viewModel.onToggleKeptFromStart(detailsWithoutTimer.value.gridId) }
                TaskTimer(
                    timeRemaining,
                    timerRunning,
                    suppressButtons,
                    { viewModel.onStartTaskTimer(detailsWithoutTimer.value.gridId) },
                    { viewModel.onStopTaskTimer(detailsWithoutTimer.value.gridId) },
                    { viewModel.onRestartTaskTimer(detailsWithoutTimer.value.gridId) },
                )

                if (doneToggleIsActive.value) {
                    TextButton(onClick = {
                        viewModel.onToggleDone(detailsWithoutTimer.value.gridId)
                        viewModel.onCloseDetails()
                    }) {
                        Text(
                            text = if (detailsWithoutTimer.value.status == TaskStatus.DONE) stringResource(R.string.undone) else stringResource(
                                R.string.done
                            ),
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                TextButton(onClick = viewModel::onCloseDetails) {
                    Text(
                        text = stringResource(R.string.back),
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeptCheckbox(
    detailsWithoutTimer: State<IGameViewModel.TaskDetails>,
    onToggle: (Boolean) -> Unit,
) {
    detailsWithoutTimer.value.keptFromStart?.also {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                text = stringResource(R.string.kept_task_checkbox),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.padding(8.dp),
            )
            Checkbox(
                checked = it,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun DescriptionText(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
    )
    {
        Text(
            text = text,
            style = TextStyle(fontSize = 20.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}

@Composable
private fun TaskTimer(
    timeRemaining: State<String?>,
    running: State<Boolean>,
    suppressButtons: State<Boolean>,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
) {
    val timerPresent = remember { derivedStateOf { timeRemaining.value != null } }
    if (timerPresent.value) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.DarkGray,
            modifier = Modifier
                .widthIn(min = 240.dp)
                .height(56.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TimerDisplay(timeRemaining)
                if (suppressButtons.value.not()) {
                    Row(
                        Modifier.width(100.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        if (running.value) {
                            TimerButton(
                                onStop,
                                painterResource(R.drawable.ic_stop),
                                stringResource(R.string.stop_task_countdown)
                            )
                            TimerButton(
                                onReset,
                                rememberVectorPainter(Icons.Filled.Refresh),
                                stringResource(R.string.reset_task_countdown)
                            )
                        } else {
                            TimerButton(
                                onStart,
                                rememberVectorPainter(Icons.Filled.PlayArrow),
                                stringResource(R.string.start_task_countdown)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerDisplay(timeRemaining: State<String?>) {
    Text(
        text = timeRemaining.value ?: "",
        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(8.dp),
    )
}

@Composable
private fun TimerButton(onStop: () -> Unit, icon: Painter, description: String) {
    IconButton(
        onClick = onStop,
        modifier = Modifier.size(48.dp),
    ) {
        Icon(
            icon,
            description,
            tint = Color.White,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
        )
    }
}
