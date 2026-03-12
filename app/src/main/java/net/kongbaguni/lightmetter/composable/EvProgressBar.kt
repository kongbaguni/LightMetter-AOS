package net.kongbaguni.lightmetter.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun EvProgressBar(
    ev: Double,
    minEv: Double = 0.0,
    maxEv: Double = 16.0
) {

    val clampedEv = max(minEv, min(ev, maxEv))
    val progress = ((clampedEv - minEv) / (maxEv - minEv)).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(text = "EV %.2f".format(ev))

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}