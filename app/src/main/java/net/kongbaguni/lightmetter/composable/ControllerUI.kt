package net.kongbaguni.lightmetter.composable

import DataStore
import DialSelector
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.model.DialModel

@SuppressLint("UnrememberedMutableState")
@Composable
fun ControllerUI() {

    val context = LocalContext.current
    val dataStore = DataStore(context = context)

    val apertureList = remember { mutableStateListOf<DialModel>() }

    val speedList = remember { mutableStateListOf<DialModel>() }

    val isoList = remember { mutableStateListOf<DialModel>() }

    val selectAperture  = remember { mutableStateOf<DialModel?>( null )}

    val selectSpeed = remember { mutableStateOf<DialModel?>( null )}
    val selectIso =  remember { mutableStateOf<DialModel?>( null )}

    val initialIsoList = remember { mutableStateOf<Int?>(null) }
    val initialApertureIndex = remember { mutableStateOf<Int?>(null) }
    val initialSpeedIndex = remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isoList.clear()
        isoList.addAll ( dataStore.isoList.map {
            DialModel(it.title(), it.value)
        })

        val lens = dataStore.selectedLens.first()
        apertureList.clear()
        apertureList.addAll(lens.apertures.map {
            DialModel(it.toString(), it)
        })

        val body = dataStore.selectedBody.first()
        speedList.clear()
        speedList.addAll(body.shutterSpeeds.map {
            DialModel(it, it)
        })

        val savedAperture = dataStore.selectedApertureValue.first()
        initialApertureIndex.value = apertureList.indexOfFirst { it.value == savedAperture }.coerceAtLeast(0)

        val savedSpeed = dataStore.selectedShutterSpeedValue.first()
        initialSpeedIndex.value = speedList.indexOfFirst { it.value == savedSpeed }.coerceAtLeast(0)

        val savedIso = dataStore.selectedIsoValue.first()
        initialIsoList.value = isoList.indexOfFirst { it.value == savedIso }.coerceAtLeast(0)
    }

    Column {

        Text("ISO")
        initialIsoList.value?.let { index ->
            key(index) {
                DialSelector(isoList, initialIndex = index) {
                    Log.d("test", it.title)
                    selectIso.value = it
                    scope.launch {
                        dataStore.saveIso(it.value as Int)
                    }
                }
            }
        }

        Text("lens")
        initialApertureIndex.value?.let { index ->
            key(index) {
                DialSelector(apertureList, initialIndex = index) {
                    Log.d("test", it.title)
                    selectAperture.value = it
                    scope.launch {
                        dataStore.saveAperture(it.value as Double)
                    }
                }
            }
        }
        Text("body")
        initialSpeedIndex.value?.let { index ->
            key(index) {
                DialSelector(speedList, initialIndex = index) {
                    Log.d("test", it.title)
                    selectSpeed.value = it
                    scope.launch {
                        dataStore.saveShutterSpeed(it.value as String)
                    }
                }
            }
        }
    }


}



@Preview
@Composable
fun ControllerUIPreview() {
    ControllerUI()
}
