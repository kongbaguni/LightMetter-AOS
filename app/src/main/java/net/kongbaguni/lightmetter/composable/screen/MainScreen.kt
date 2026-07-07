package net.kongbaguni.lightmetter.composable.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.composable.component.CameraEVCheckButton
import net.kongbaguni.lightmetter.composable.component.ControllerUI
import net.kongbaguni.lightmetter.model.LightMetterRange
import net.kongbaguni.lightmetter.utill.BillingManager
import net.kongbaguni.lightmetter.utill.DataStore

@Composable
fun MainScreen(
    billingManager: BillingManager,
    onNavigate: (Page) -> Unit
) {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val selectedFilter by dataStore.selectedFilter.collectAsState(initial = null)
    
    var measuredEv by remember { mutableStateOf<Double?>(null) }
    var calculatedEv by remember { mutableStateOf<Double?>(null) }
    var meteringMode by remember { mutableStateOf(LightMetterRange.Default) }

    val effectiveMeasuredEv = measuredEv?.let { it - (selectedFilter?.stop ?: 0.0) }

    Box(modifier = Modifier.fillMaxSize()) {
        ControllerUI(
            measuredEv = measuredEv,
            billingManager = billingManager,
            onCalculatedEvChange = { calculatedEv = it },
            onLensClick = { onNavigate(Page.LENSLIST) },
            onBodyClick = { onNavigate(Page.BODYLIST) }
        )
        
        CameraEVCheckButton(
            measuredEv = effectiveMeasuredEv,
            calculatedEv = calculatedEv,
            meteringMode = meteringMode,
            onMeteringModeChange = { meteringMode = it },
            onEvMeasured = { model ->
                measuredEv = model.getEv()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
