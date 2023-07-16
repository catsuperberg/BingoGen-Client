package dev.catsuperberg.bingogen.client.ui.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.catsuperberg.bingogen.client.R
import dev.catsuperberg.bingogen.client.view.model.multiplayer.lobby.selector.ILobbySelectorViewModel

@Composable
fun LobbySelectorScreen(viewModel: ILobbySelectorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.under_construction),
            style = TextStyle(fontSize = 42.sp, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextButton(onClick = viewModel::requestBack) {
                Text(
                    text = stringResource(R.string.back),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
