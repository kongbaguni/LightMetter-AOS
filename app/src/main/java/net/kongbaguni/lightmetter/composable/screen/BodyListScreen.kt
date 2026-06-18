package net.kongbaguni.lightmetter.composable.screen

import DataStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val selectedBrand by dataStore.selectedBrand.collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    val brands = remember(bodyUiState.bodies) {
        bodyUiState.bodies.map { it.brand }.distinct().sorted()
    }

    val filteredBodies = remember(bodyUiState.bodies, selectedBrand) {
        if (selectedBrand == null) {
            bodyUiState.bodies
        } else {
            bodyUiState.bodies.filter { it.brand == selectedBrand }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(R.string.body_list),
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

        LazyColumn(
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(filteredBodies) { item ->
                SwitchListColumnItem(
                    brand = item.brand,
                    name = item.name,
                    isSelected = item == bodyUiState.selected,
                    onClick = {
                        scope.launch {
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
