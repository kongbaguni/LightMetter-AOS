package net.kongbaguni.lightmetter.composable

import DataStore
import DialSelector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import net.kongbaguni.lightmetter.model.DialModel

@Composable
fun ControllerUI() {

    val context = LocalContext.current
    val dataStore = DataStore(context = context)
    dataStore.selectedBody
    dataStore.selectedLens


    val list = listOf<DialModel>(
        DialModel("1", 1.0),
        DialModel("2", 2.0),
        DialModel("3", 3.0),
        DialModel("4", 4.0),
        DialModel("5", 5.0),
        DialModel("6", 6.0),
    )
    val current = mutableStateOf<DialModel>(list[0])
    DialSelector(list) {
        current.value = it
    }
}