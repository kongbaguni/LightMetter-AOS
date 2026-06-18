package net.kongbaguni.lightmetter.composable.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.composable.component.CameraEVCheckButton
import net.kongbaguni.lightmetter.composable.component.ControllerUI
import net.kongbaguni.lightmetter.model.LightMetterRange

@Composable
fun MainScreen() {
    var measuredEv by remember { mutableStateOf<Double?>(null) }
    var meteringMode by remember { mutableStateOf(LightMetterRange.Default) }

    Box(modifier = Modifier.fillMaxSize()) {
        ControllerUI(measuredEv = measuredEv)
        
        CameraEVCheckButton(
            measuredEv = measuredEv,
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
