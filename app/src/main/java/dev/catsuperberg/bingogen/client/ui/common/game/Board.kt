package dev.catsuperberg.bingogen.client.ui.common.game

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.catsuperberg.bingogen.client.common.TaskStatus
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
            .padding(all = 2.dp)
    ) {
        Button(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(all = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = tile.state.color()
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = tile.title,
                style = TextStyle(fontSize = 13.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TaskStatus.color(): Color = when(this) {
    TaskStatus.FAILED -> MaterialTheme.colorScheme.error
    TaskStatus.UNKEPT, TaskStatus.FINISHED_UNKEPT -> MaterialTheme.colorScheme.tertiary
    TaskStatus.UNDONE, TaskStatus.FINISHED_UNDONE -> MaterialTheme.colorScheme.primary
    TaskStatus.DONE, TaskStatus.FINISHED_KEPT -> MaterialTheme.colorScheme.secondary
    TaskStatus.COUNTDOWN, TaskStatus.KEPT_COUNTDOWN, TaskStatus.FINISHED_COUNTDOWN -> MaterialTheme.colorScheme.primaryContainer
    TaskStatus.KEPT -> MaterialTheme.colorScheme.secondaryContainer
    TaskStatus.INACTIVE -> MaterialTheme.colorScheme.background
}
