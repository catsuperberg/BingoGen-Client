package dev.catsuperberg.bingogen.client.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.ui.theme.extendedTypography
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GameSetupScreen(viewModel: IGameSetupViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(true) {
        viewModel.state.snackBarMessage.collect(snackbarHostState::showSnackbar)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(content = { Text(data.visuals.message) })
                },
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 120.dp)
            )
        }
    ) { _ ->
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                SettingSelectors(viewModel)

                Box(contentAlignment = Alignment.BottomCenter) {
                    Button(onClick = viewModel::onDone, modifier = Modifier.padding(bottom = 80.dp)) {
                        Text(
                            text = stringResource(R.string.start_game),
                            style = MaterialTheme.extendedTypography.buttonLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = viewModel::onBack,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 14.dp, vertical = 28.dp)
                    .size(72.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingSelectors(viewModel: IGameSetupViewModel) {
    val gameSelection = viewModel.state.gameSelection.collectAsState()
    val selectedGame = viewModel.state.chosenGame.collectAsState()
    val sheetSelection = viewModel.state.sheetSelection.collectAsState()
    val selectedSheet = viewModel.state.chosenSheet.collectAsState()
    val gameListExtended = rememberSaveable { mutableStateOf(false) }
    val sheetListExtended = rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SelectionMenu(
            stringResource(R.string.game_input),
            gameListExtended,
            selectedGame,
            gameSelection,
            viewModel::onGameChange
        )
        SelectionMenu(
            stringResource(R.string.sheet_input),
            sheetListExtended,
            selectedSheet,
            sheetSelection,
            viewModel::onSheetChange
        )

        StepValueChanger(viewModel)
    }
}

@Composable
private fun StepValueChanger(viewModel: IGameSetupViewModel) {
    val size = viewModel.state.boardSideCount.collectAsState()

    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
            contentColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.side_count_selector),
                style = MaterialTheme.extendedTypography.buttonMedium,
                modifier = Modifier.padding(8.dp),
            )
            ChangerControls(viewModel, size)
        }
    }
}

@Composable
private fun ChangerControls(
    viewModel: IGameSetupViewModel,
    size: State<Int>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = viewModel::onSizeDown) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.size_down)
            )
        }
        Text(
            text = size.value.toString(),
            style = MaterialTheme.extendedTypography.buttonMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.requiredWidth(50.dp)
        )
        IconButton(onClick = viewModel::onSizeUp) {
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = stringResource(R.string.size_up)
            )
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectionMenu(
    label: String,
    listExtended: MutableState<Boolean>,
    selectedItem: State<Int?>,
    selection: State<List<String>>,
    onSelect: (index: Int) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = listExtended.value,
        onExpandedChange = { listExtended.value = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            label = { Text(label) },
            value = selectedItem.value?.let { selection.value[it] } ?: "",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = listExtended.value) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            onValueChange = {}
        )
        ExposedDropdownMenu(
            expanded = listExtended.value,
            onDismissRequest = { listExtended.value = false },
        ) {
            selection.value.forEachIndexed { index, server ->
                DropdownMenuItem(
                    text = { Text(text = server) },
                    onClick = {
                        onSelect(index)
                        listExtended.value = false
                    }
                )
            }
        }
    }
}
