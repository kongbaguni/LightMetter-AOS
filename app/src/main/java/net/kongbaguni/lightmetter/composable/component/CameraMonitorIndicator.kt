package net.kongbaguni.lightmetter.composable.component

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.utill.CameraUsageMonitor

@Composable
fun CameraIndicator(
    inUse: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        drawCircle(
            color = if (inUse) Color.Red else Color.Green
        )
    }
}

@Composable
fun CameraMonitorIndicator(context: Context, modifier: Modifier) {
    val isPreview = LocalInspectionMode.current

    if (isPreview) {
        CameraIndicator(false, modifier)
        return
    }

    val monitor = remember { CameraUsageMonitor(context) }
    val inUse by monitor.cameraInUse.collectAsState()

    DisposableEffect(Unit) {
        monitor.start()
        onDispose { monitor.stop() }
    }

    CameraIndicator(
        inUse = inUse,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun CameraMonitorIndicatorPreview() {
    val context = LocalContext.current

    CameraMonitorIndicator(
        context = context,
        modifier = Modifier.size(20.dp)
    )
}