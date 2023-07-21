package dev.catsuperberg.bingogen.client.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupViewModel

@Composable
fun GameSetupScreen(viewModel: IGameSetupViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(true) {
        viewModel.state.snackBarMessage.collect(snackbarHostState::showSnackbar)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val gameSelection = viewModel.state.gameSelection.collectAsState()
            val selectedGame = viewModel.state.chosenGame.collectAsState()
            val sheetSelection = viewModel.state.sheetSelection.collectAsState()
            val selectedSheet = viewModel.state.chosenSheet.collectAsState()
            val gameListExtended = rememberSaveable { mutableStateOf(false) }
            val sheetListExtended = rememberSaveable { mutableStateOf(false) }

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

            val size = viewModel.state.boardSideCount.collectAsState()

            Surface(
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium,
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
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
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

            Spacer(modifier = Modifier.height(36.dp))
            TextButton(onClick = viewModel::onDone) {
                Text(
                    text = stringResource(R.string.start_game),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
            TextButton(onClick = viewModel::onBack) {
                Text(
                    text = stringResource(R.string.back),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
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
            modifier = Modifier.menuAnchor(),
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
