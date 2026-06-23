package net.kongbaguni.lightmetter.composable.component

import net.kongbaguni.lightmetter.utill.DataStore
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.model.DialModel
import net.kongbaguni.lightmetter.model.FilterModel
import net.kongbaguni.lightmetter.model.LensModel
import androidx.compose.runtime.collectAsState
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

@SuppressLint("UnrememberedMutableState")
@Composable
fun ControllerUI(
    measuredEv: Double? = null,
    onCalculatedEvChange: (Double?) -> Unit = {},
    onLensClick: () -> Unit = {},
    onBodyClick: () -> Unit = {}
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

    val selectedLens = remember { mutableStateOf<LensModel?>(null) }
    val selectedBody = remember { mutableStateOf<BodyModel?>(null) }
    val selectedFilter = dataStore.selectedFilter.collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    val autoShutterValue = remember(selectAperture.value, selectIso.value, measuredEv, speedList.size, selectedFilter.value) {
        val aperture = selectAperture.value?.value as? Double
        val iso = selectIso.value?.value as? Int
        val mEv = measuredEv
        val filterStop = selectedFilter.value?.stop ?: 0.0

        if (aperture != null && iso != null && mEv != null) {
            val s = iso.toDouble()
            val n = aperture
            // 필터의 stop 만큼 노출 시간을 늘려야 함 (EV를 낮추는 효과)
            val effectiveEv = mEv - filterStop
            val targetT = (n * n) / (2.0.pow(effectiveEv) * (s / 100.0))

            val availableSpeeds = speedList.filter { it.value != "AUTO" }
                .map { it.value as String to parseShutterSpeed(it.value as String) }

            if (availableSpeeds.isEmpty()) {
                formatShutterSpeed(targetT)
            } else {
                val slowestAvailable = availableSpeeds.maxByOrNull { it.second }

                if (slowestAvailable != null && targetT > slowestAvailable.second + 1e-6) {
                    // 요구되는 노출 시간이 바디가 지원하는 가장 느린 셔터속도보다 길 때 (벌브 모드 권장)
                    "B ${formatShutterSpeed(targetT)}"
                } else {
                    // 바디가 지원하는 범위 내에서 "필름이니까 올림" (더 느린 속도 중 가장 가까운 것 선택)
                    val bestMatch = availableSpeeds
                        .filter { it.second >= targetT - 1e-6 }
                        .minByOrNull { it.second }
                        ?: slowestAvailable

                    bestMatch?.first ?: formatShutterSpeed(targetT)
                }
            }
        } else {
            null
        }
    }

    LaunchedEffect(autoShutterValue) {
        val index = speedList.indexOfFirst { it.value == "AUTO" }
        if (index != -1) {
            val newLabel = if (autoShutterValue != null) "A: $autoShutterValue" else "AUTO"
            speedList[index] = DialModel(newLabel, "AUTO")
        }
    }

    val calculatedEv = remember {
        derivedStateOf {
            val aperture = selectAperture.value?.value as? Double
            val speedString = selectSpeed.value?.value as? String
            val iso = selectIso.value?.value as? Int
            val filterStop = selectedFilter.value?.stop ?: 0.0

            if (aperture != null && speedString != null && iso != null) {
                if (speedString == "AUTO") {
                    measuredEv?.let { it - filterStop }
                } else {
                    val t = parseShutterSpeed(speedString)
                    val n = aperture
                    val s = iso.toDouble()

                    val ev = log2((n * n) / t) - log2(s / 100.0)
                    // 필터가 장착된 상태에서의 설정된 EV 값
                    ev
                }
            } else {
                null
            }
        }
    }

    LaunchedEffect(calculatedEv.value) {
        onCalculatedEvChange(calculatedEv.value)
    }

    LaunchedEffect(Unit) {
        isoList.clear()
        val rawIsoList = dataStore.isoList.map {
            DialModel(it.title(), it.value)
        }
        isoList.addAll(rawIsoList.reversed())

        val lens = dataStore.selectedLens.first()
        selectedLens.value = lens
        apertureList.clear()
        apertureList.addAll(lens.apertures.map {
            DialModel(it.toString(), it)
        })

        val body = dataStore.selectedBody.first()
        selectedBody.value = body
        speedList.clear()
        speedList.addAll(body.shutterSpeeds.map {
            DialModel(it, it)
        })
        speedList.add(DialModel("AUTO", "AUTO"))

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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // EV Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = calculatedEv.value?.let { "%.1f".format(it) } ?: "--.-",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )

                // Measured EV
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val filter = selectedFilter.value
                    val filterStop = filter?.stop ?: 0.0
                    Text(
                        text = if (filter != null) "FILTERED EV" else "MEASURED EV",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = measuredEv?.let { "%.1f".format(it - filterStop) } ?: "--.-",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    if (filter != null) {
                        Text(
                            text = filter.name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Aperture Selector
        SelectorSection(
            title = "APERTURE (f/)",
            subTitle = selectedLens.value?.name,
            brand = selectedLens.value?.brand,
            items = apertureList,
            initialIndex = initialApertureIndex.value,
            onClick = onLensClick
        ) {
            selectAperture.value = it
            scope.launch { dataStore.saveAperture(it.value as Double) }
        }

        // Shutter Speed Selector
        SelectorSection(
            title = "SHUTTER SPEED (sec)",
            subTitle = selectedBody.value?.name,
            brand = selectedBody.value?.brand,
            items = speedList,
            initialIndex = initialSpeedIndex.value,
            onClick = onBodyClick
        ) {
            selectSpeed.value = it
            scope.launch { dataStore.saveShutterSpeed(it.value as String) }
        }

        // ISO Selector
        SelectorSection(title = "ISO", items = isoList, initialIndex = initialIsoList.value) {
            selectIso.value = it
            scope.launch { dataStore.saveIso(it.value as Int) }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SelectorSection(
    title: String,
    subTitle: String? = null,
    brand: String? = null,
    items: List<DialModel>,
    initialIndex: Int?,
    onClick: (() -> Unit)? = null,
    onValueChanged: (DialModel) -> Unit
) {
    Column {

        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        if (subTitle != null) {
            Row(
                modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (brand != null) {
                    val brandIcon = brand.lowercase().replace(" ", "")
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file:///android_asset/brand_icons/$brandIcon.svg")
                            .decoderFactory(SvgDecoder.Factory())
                            .build(),
                        contentDescription = brand,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = subTitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

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
    if (speed == "AUTO") return 1.0 // Should not be called for AUTO in EV calc
    return if (speed.contains("/")) {
        val parts = speed.split("/")
        parts[0].toDouble() / parts[1].toDouble()
    } else {
        speed.toDouble()
    }
}

private fun formatShutterSpeed(seconds: Double): String {
    return if (seconds >= 1.0) {
        "%.1f\"".format(seconds)
    } else {
        val reciprocal = 1.0 / seconds
        if (reciprocal >= 1.0) {
            "1/${reciprocal.roundToInt()}"
        } else {
            "%.2f".format(seconds)
        }
    }
}

@Preview
@Composable
fun ControllerUIPreview() {
    ControllerUI(measuredEv = 13.0)
}
