package net.kongbaguni.lightmetter.composable.screen

import DataStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.R.string.lens_list
import net.kongbaguni.lightmetter.composable.component.SwitchListColumnItem
import net.kongbaguni.lightmetter.model.LensUiState

@Composable
fun LensListScreen() {
    val context = LocalContext.current
    val dataStore = DataStore(context = context)
    val scope = rememberCoroutineScope()
    val lensUiState by dataStore.lensUiState.collectAsState(
        initial = LensUiState()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(lens_list),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        if (lensUiState.lensList.isEmpty()) {
            Text(
                "loading lens list...",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(lensUiState.lensList) { item ->
                    SwitchListColumnItem(
                        brand = item.brand,
                        name = item.name,
                        isSelected = item == lensUiState.selected,
                        onClick = {
                            scope.launch {
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
