package net.kongbaguni.lightmetter.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.R
import net.kongbaguni.lightmetter.model.LensModel
import net.kongbaguni.lightmetter.utill.DataStore

@Composable
fun CustomLensEditScreen(
    dataStore: DataStore,
    lensToEdit: LensModel? = null,
    onBack: () -> Unit,
    onSave: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var brand by remember { mutableStateOf(lensToEdit?.brand ?: "") }
    var name by remember { mutableStateOf(lensToEdit?.name ?: "") }
    var aperturesText by remember { 
        mutableStateOf(lensToEdit?.apertures?.joinToString(", ") ?: "") 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = if (lensToEdit == null) stringResource(R.string.add_custom_lens) else stringResource(R.string.edit_custom_lens),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text(stringResource(R.string.brand)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.model_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = aperturesText,
            onValueChange = { aperturesText = it },
            label = { Text(stringResource(R.string.apertures_label)) },
            placeholder = { Text("1.4, 2, 2.8, 4, 5.6") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val apertures = aperturesText.split(",")
                    .mapNotNull { it.trim().toDoubleOrNull() }
                scope.launch {
                    if (lensToEdit == null) {
                        dataStore.repository.insertLens(brand, name, apertures)
                    } else {
                        dataStore.repository.updateLens(lensToEdit.id, brand, name, apertures)
                    }
                    onSave()
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = brand.isNotBlank() && name.isNotBlank() && aperturesText.isNotBlank()
        ) {
            Text(stringResource(R.string.save))
        }
    }
}
