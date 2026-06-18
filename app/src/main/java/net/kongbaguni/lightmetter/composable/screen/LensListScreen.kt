package net.kongbaguni.lightmetter.composable.screen

import DataStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
    val dataStore = DataStore(context = androidx.compose.ui.platform.LocalContext.current)
    val scope = rememberCoroutineScope()
    
    val lensUiState by dataStore.lensUiState.collectAsState(
        initial = LensUiState()
    )

    val selectedBrand by dataStore.selectedBrand.collectAsState(initial = null)

    val brands = remember(lensUiState.lensList) {
        lensUiState.lensList.map { it.brand }.distinct().sorted()
    }

    val filteredLenses = remember(lensUiState.lensList, selectedBrand) {
        if (selectedBrand == null) {
            lensUiState.lensList
        } else {
            lensUiState.lensList.filter { it.brand == selectedBrand }
        }
    }

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

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedBrand == null,
                    onClick = { 
                        scope.launch { dataStore.saveSelectedBrand(null) }
                    },
                    label = { Text("All") }
                )
            }
            items(brands) { brand ->
                FilterChip(
                    selected = selectedBrand == brand,
                    onClick = {
                        scope.launch {
                            dataStore.saveSelectedBrand(if (selectedBrand == brand) null else brand)
                        }
                    },
                    label = { Text(brand) }
                )
            }
        }

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
                items(filteredLenses) { item ->
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
