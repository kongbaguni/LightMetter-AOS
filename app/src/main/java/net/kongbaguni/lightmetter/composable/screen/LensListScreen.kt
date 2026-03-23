package net.kongbaguni.lightmetter.composable.screen

import DataStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import net.kongbaguni.lightmetter.R.string.lens_list
import net.kongbaguni.lightmetter.composable.component.SwitchListColumnItem
import net.kongbaguni.lightmetter.composable.component.ToggleSwitch
import net.kongbaguni.lightmetter.model.LensUiState

@Composable
fun LensListScreen() {
    val context = LocalContext.current
    val dataStore = DataStore(context = context)
    val scop = rememberCoroutineScope()
    val lensUiState by dataStore.lensUiState.collectAsState(
        initial = LensUiState()
    )

    Column {

        Text(
            text = stringResource(lens_list),
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(5.dp)
        )
        if (lensUiState.lensList.isEmpty()) {
            Text("loading lens list...")
        } else {
            LazyColumn {
                items(lensUiState.lensList) { item ->
                    SwitchListColumnItem(
                        brand = item.brand,
                        name = item.name,
                        isSelected = item == lensUiState.selected,
                        onClick = {
                            scop.launch {
                                dataStore.saveLens(item)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LensListScreenPreview() {
    LensListScreen()
}