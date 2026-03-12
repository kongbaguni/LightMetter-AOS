package net.kongbaguni.lightmetter.composable

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import net.kongbaguni.lightmetter.utill.CameraUsageMonitor

@Composable
fun CameraMonitorUI(context: Context, modifier: Modifier) {
    val monitor = remember { CameraUsageMonitor(context) }
    val inUse by monitor.cameraInUse.collectAsState()

    Canvas(modifier = modifier.size(20.dp)) {
        drawCircle(
            color = if (inUse) Color.Red else Color.Green
        )
    }

    DisposableEffect(Unit) {
        monitor.start()
        onDispose { monitor.stop() }
    }
}
