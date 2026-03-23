package net.kongbaguni.lightmetter.composable.screen

import DataStore
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.R
import net.kongbaguni.lightmetter.composable.component.SwitchListColumnItem
import net.kongbaguni.lightmetter.model.BodyUiState

@Composable
fun BodyListScreen() {
    val context = LocalContext.current
    val dataStore = DataStore(context = context)
    val bodyUiState by dataStore.bodyUiState.collectAsState(
        initial = BodyUiState()
    )

    val scop = rememberCoroutineScope()

    val selectedBody by DataStore(LocalContext.current).selectedBody.collectAsState(
        initial = bodyUiState
    )
    Column {
        Text(
            text = stringResource(R.string.body_list),
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(5.dp)
        )

        LazyColumn {
            items(bodyUiState.bodies) { item ->
                SwitchListColumnItem(
                    brand = item.brand,
                    name = item.name,
                    isSelected = item == selectedBody,
                    onClick = {
                        scop.launch {
                            dataStore.saveBody(item)
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun BodyListScreenPreview() {
    BodyListScreen()
}
