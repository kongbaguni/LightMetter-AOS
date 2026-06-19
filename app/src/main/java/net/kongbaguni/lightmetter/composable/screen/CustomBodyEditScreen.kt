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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.utill.DataStore

@Composable
fun CustomBodyEditScreen(
    dataStore: DataStore,
    bodyToEdit: BodyModel? = null,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var brand by remember { mutableStateOf(bodyToEdit?.brand ?: "") }
    var name by remember { mutableStateOf(bodyToEdit?.name ?: "") }
    var shutterSpeedsText by remember { 
        mutableStateOf(bodyToEdit?.shutterSpeeds?.joinToString(", ") ?: "") 
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (bodyToEdit == null) "Add Custom Body" else "Edit Custom Body",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Brand") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Model Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = shutterSpeedsText,
            onValueChange = { shutterSpeedsText = it },
            label = { Text("Shutter Speeds (comma separated)") },
            placeholder = { Text("1, 1/2, 1/125, 1/1000") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val speeds = shutterSpeedsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                scope.launch {
                    if (bodyToEdit == null) {
                        dataStore.repository.insertBody(brand, name, speeds)
                    } else {
                        dataStore.repository.updateBody(bodyToEdit.id, brand, name, speeds)
                    }
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = brand.isNotBlank() && name.isNotBlank() && shutterSpeedsText.isNotBlank()
        ) {
            Text("Save")
        }
    }
}
