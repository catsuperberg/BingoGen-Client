package dev.catsuperberg.bingogen.client.ui.common.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.catsuperberg.bingogen.client.common.TaskStatus
import dev.catsuperberg.bingogen.client.ui.theme.extendedColors
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel

@Composable
fun Board(viewModel: IGameViewModel) {
    val taskGrid = viewModel.state.board.collectAsState()

    taskGrid.value?.rows?.also { rows ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            Column {
                rows.forEachIndexed { rowIndex, row ->
                    Row {
                        row.forEachIndexed { columnIndex, tile ->
                            val gridIndex = rowIndex * row.size + columnIndex
                            Tile(
                                modifier = Modifier.weight(1f),
                                tile,
                                onClick = { viewModel.onViewDetails(gridIndex) }
                            )
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(30.dp))
}

@Composable
fun Tile(modifier: Modifier, tile: IGameViewModel.BoardTile, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(all = 1.5.dp)
    ) {
        val colors = tile.state.colors()
        val border = colors.border?.let { BorderStroke(width = 3.dp, it) }
        OutlinedButton(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(all = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.fill,
                contentColor = colors.content
            ),
            border = border,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = tile.title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

data class TileColors(val fill: Color, val content: Color, val border: Color? = null)

@Composable
private fun TaskStatus.colors(): TileColors = when(this) {
    TaskStatus.FAILED ->
        TileColors(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
    TaskStatus.UNKEPT, TaskStatus.FINISHED_UNKEPT ->
        TileColors(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, border = MaterialTheme.colorScheme.tertiary)
    TaskStatus.UNDONE, TaskStatus.FINISHED_UNDONE ->
        TileColors(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    TaskStatus.DONE, TaskStatus.FINISHED_KEPT ->
        TileColors(MaterialTheme.extendedColors.successContainer, MaterialTheme.extendedColors.onSuccessContainer)
    TaskStatus.COUNTDOWN, TaskStatus.KEPT_COUNTDOWN, TaskStatus.FINISHED_COUNTDOWN ->
        TileColors(MaterialTheme.extendedColors.elevatedTertiaryContainer, MaterialTheme.extendedColors.onElevatedTertiaryContainer)
    TaskStatus.KEPT ->
        TileColors(MaterialTheme.extendedColors.successContainer, MaterialTheme.extendedColors.onSuccessContainer, border = MaterialTheme.colorScheme.tertiary)
    TaskStatus.INACTIVE ->
        TileColors(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
}
