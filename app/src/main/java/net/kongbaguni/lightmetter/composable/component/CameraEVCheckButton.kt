package net.kongbaguni.lightmetter.composable.component

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kongbaguni.lightmetter.model.LightMetterModel
import net.kongbaguni.lightmetter.utill.LightMetterCameraManager

@Composable
fun CameraEVCheckButton(
    measuredEv: Double?,
    onEvMeasured: (LightMetterModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val lightMetterCameraManager = remember {
        if (!isPreview) LightMetterCameraManager(context) else null
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && !isPreview) {
                lightMetterCameraManager?.photometry(
                    { value -> onEvMeasured(value) },
                    {}
                )
            }
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Measured EV Label
        Surface(
            color = Color(0xFF333333),
            shape = CircleShape,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = measuredEv?.let { "MEASURED EV: ${"%.1f".format(it)}" } ?: "TAP TO MEASURE",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                if (isPreview) return@Button
                lightMetterCameraManager?.photometry(
                    { value -> onEvMeasured(value) },
                    { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            },
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD54F),
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("측광", fontWeight = FontWeight.Bold)
                CameraMonitorIndicator(
                    modifier = Modifier.size(20.dp),
                    context = context
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CarmeraEvCheckButtonPreview() {
    CameraEVCheckButton(measuredEv = 12.5, onEvMeasured = {})
}
