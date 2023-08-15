package dev.catsuperberg.bingogen.client.ui.start

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.ui.helper.pxToDp
import dev.catsuperberg.bingogen.client.ui.theme.extendedTypography
import dev.catsuperberg.bingogen.client.view.model.start.IStartViewModel

@Composable
fun StartScreen(viewModel: IStartViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.app_name_stylized),
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, start = 30.dp, end = 30.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_grid_4x4_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(228.dp)
            )

            Controls(viewModel)
        }
    }
}

@Composable
private fun Controls(viewModel: IStartViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ModeCard(
            onClick = viewModel::onSinglePlayer,
            stringResource(R.string.start_single_player),
            painterResource(id = R.drawable.ic_singleplayer),
        )
        ModeCard(
            onClick = viewModel::onMultiplayer,
            stringResource(R.string.start_multiplayer),
            painterResource(id = R.drawable.ic_multiplayer),
        )
        ServerSelector(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeCard(
    onClick: () -> Unit,
    text: String,
    icon: Painter
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.6f)
    ) {
        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 16.dp)
                    .size(48.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.extendedTypography.buttonLarge,
                modifier = Modifier.padding(20.dp, 20.dp)
            )
        }
    }
}

@Composable
private fun ServerSelector(viewModel: IStartViewModel) {
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
                .rotate(if (isServerListVisible.value) 0f else 180f)
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
}
