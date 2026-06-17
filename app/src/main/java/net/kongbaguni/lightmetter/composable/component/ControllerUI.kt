package net.kongbaguni.lightmetter.composable.component

import DataStore
import DialSelector
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.model.DialModel
import kotlin.math.log2

@SuppressLint("UnrememberedMutableState")
@Composable
fun ControllerUI(
    measuredEv: Double? = null
) {
    val context = LocalContext.current
    val dataStore = DataStore(context = context)

    val apertureList = remember { mutableStateListOf<DialModel>() }
    val speedList = remember { mutableStateListOf<DialModel>() }
    val isoList = remember { mutableStateListOf<DialModel>() }

    val selectAperture = remember { mutableStateOf<DialModel?>(null) }
    val selectSpeed = remember { mutableStateOf<DialModel?>(null) }
    val selectIso = remember { mutableStateOf<DialModel?>(null) }

    val initialIsoList = remember { mutableStateOf<Int?>(null) }
    val initialApertureIndex = remember { mutableStateOf<Int?>(null) }
    val initialSpeedIndex = remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()

    val calculatedEv = remember {
        derivedStateOf {
            val aperture = selectAperture.value?.value as? Double
            val speedString = selectSpeed.value?.value as? String
            val iso = selectIso.value?.value as? Int

            if (aperture != null && speedString != null && iso != null) {
                val t = parseShutterSpeed(speedString)
                val n = aperture
                val s = iso.toDouble()

                log2((n * n) / t) - log2(s / 100.0)
            } else {
                null
            }
        }
    }

    LaunchedEffect(Unit) {
        isoList.clear()
        isoList.addAll(dataStore.isoList.map {
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
        initialApertureIndex.value =
            apertureList.indexOfFirst { it.value == savedAperture }.coerceAtLeast(0)

        val savedSpeed = dataStore.selectedShutterSpeedValue.first()
        initialSpeedIndex.value =
            speedList.indexOfFirst { it.value == savedSpeed }.coerceAtLeast(0)

        val savedIso = dataStore.selectedIsoValue.first()
        initialIsoList.value = isoList.indexOfFirst { it.value == savedIso }.coerceAtLeast(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // EV Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calculated EV
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SETTING EV",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = calculatedEv.value?.let { "%.1f".format(it) } ?: "--.-",
                        fontSize = 32.sp,
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(Color.DarkGray)
                )

                // Measured EV
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MEASURED EV",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = measuredEv?.let { "%.1f".format(it) } ?: "--.-",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // ISO Selector
        SelectorSection(title = "ISO", items = isoList, initialIndex = initialIsoList.value) {
            selectIso.value = it
            scope.launch { dataStore.saveIso(it.value as Int) }
        }

        // Aperture Selector
        SelectorSection(
            title = "APERTURE (f/)",
            items = apertureList,
            initialIndex = initialApertureIndex.value
        ) {
            selectAperture.value = it
            scope.launch { dataStore.saveAperture(it.value as Double) }
        }

        // Shutter Speed Selector
        SelectorSection(
            title = "SHUTTER SPEED (sec)",
            items = speedList,
            initialIndex = initialSpeedIndex.value
        ) {
            selectSpeed.value = it
            scope.launch { dataStore.saveShutterSpeed(it.value as String) }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SelectorSection(
    title: String,
    items: List<DialModel>,
    initialIndex: Int?,
    onValueChanged: (DialModel) -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        initialIndex?.let { index ->
            key(index) {
                DialSelector(
                    items = items,
                    initialIndex = index,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    onValueChanged = onValueChanged
                )
            }
        }
    }
}

private fun parseShutterSpeed(speed: String): Double {
    return if (speed.contains("/")) {
        val parts = speed.split("/")
        parts[0].toDouble() / parts[1].toDouble()
    } else {
        speed.toDouble()
    }
}

@Preview
@Composable
fun ControllerUIPreview() {
    ControllerUI(measuredEv = 13.0)
}
