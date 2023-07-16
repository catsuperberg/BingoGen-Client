package dev.catsuperberg.bingogen.client.ui.start

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.ui.helper.pxToDp
import dev.catsuperberg.bingogen.client.view.model.start.IStartViewModel

@Composable
fun StartScreen(viewModel: IStartViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.app_name_stylized),
            style = TextStyle(fontSize = 42.sp, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val servers = viewModel.state.serverList.collectAsState()
            val serverString = viewModel.state.serverString.collectAsState()
            val badInput = viewModel.state.indicateBadInput.collectAsState()
            val isServerListVisible = rememberSaveable { mutableStateOf(false) }
            val textBoxSize = remember { mutableStateOf(IntSize.Zero) }
            val icon = @Composable {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    stringResource(R.string.server_saved_list),
                    Modifier
                        .clickable { isServerListVisible.value = !isServerListVisible.value }
                        .rotate(if (isServerListVisible.value) 180f else 0f)
                )
            }

            Box {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.server_input)) },
                    value = serverString.value,
                    onValueChange = viewModel::onServerStringChange,
                    placeholder = { Text(stringResource(R.string.server_address_example)) },
                    trailingIcon = if (servers.value.isNotEmpty()) icon else null,
                    isError = badInput.value,
                    modifier = Modifier.onGloballyPositioned { coords -> textBoxSize.value = coords.size },
                )
                DropdownMenu(
                    expanded = isServerListVisible.value,
                    onDismissRequest = { isServerListVisible.value = false },
                    modifier = Modifier.width(textBoxSize.value.width.pxToDp())
                ) {
                    servers.value.forEachIndexed { index, server ->
                        DropdownMenuItem(
                            text = { Text(text = server) },
                            onClick = {
                                viewModel.onSelectServerFromList(index)
                                isServerListVisible.value = false
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    stringResource(R.string.server_delete),
                                    Modifier.clickable { viewModel.onDeleteServer(index) },
                                )
                            },
                        )
                    }
                }
            }

            TextButton(onClick = viewModel::onSinglePlayer) {
                Text(
                    text = stringResource(R.string.start_single_player),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
            TextButton(onClick = viewModel::onMultiplayer) {
                Text(
                    text = stringResource(R.string.start_multiplayer),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
