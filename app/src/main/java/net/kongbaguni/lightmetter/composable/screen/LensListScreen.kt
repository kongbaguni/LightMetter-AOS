package net.kongbaguni.lightmetter.composable.screen

import net.kongbaguni.lightmetter.utill.DataStore
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import net.kongbaguni.lightmetter.R
import net.kongbaguni.lightmetter.composable.component.SwitchListColumnItem
import net.kongbaguni.lightmetter.model.LensUiState

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import net.kongbaguni.lightmetter.model.LensModel

@Composable
fun LensListScreen(
    onAddLens: () -> Unit = {},
    onEditLens: (LensModel) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dataStore = remember { DataStore(context = context) }
    val scope = rememberCoroutineScope()
    
    val lensUiState by dataStore.lensUiState.collectAsState(
        initial = LensUiState()
    )

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var itemToDeleteId by remember { mutableStateOf<Int?>(null) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDeleteId?.let { id ->
                            scope.launch {
                                dataStore.repository.deleteLens(id)
                            }
                        }
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.lens_list),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
            IconButton(onClick = onAddLens, modifier = Modifier.padding(end = 16.dp)) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_lens))
            }
        }

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
                    label = { Text(stringResource(R.string.filter_all)) }
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
                stringResource(R.string.loading_lens_list),
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
                        isCustom = item.id >= 10000,
                        onClick = {
                            scope.launch {
                                dataStore.saveLens(item)
                            }
                        },
                        onEdit = { onEditLens(item) },
                        onDelete = {
                            itemToDeleteId = item.id
                            showDeleteConfirmDialog = true
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
