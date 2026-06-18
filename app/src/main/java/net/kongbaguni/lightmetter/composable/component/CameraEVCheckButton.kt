package net.kongbaguni.lightmetter.composable.component

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import net.kongbaguni.lightmetter.model.LightMetterRange
import net.kongbaguni.lightmetter.utill.LightMetterCameraManager

@Composable
fun CameraEVCheckButton(
    measuredEv: Double?,
    meteringMode: LightMetterRange,
    onMeteringModeChange: (LightMetterRange) -> Unit,
    onEvMeasured: (LightMetterModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var showMeteringSelector by remember { mutableStateOf(false) }

    val lightMetterCameraManager = remember {
        if (!isPreview) LightMetterCameraManager(context) else null
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && !isPreview) {
                lightMetterCameraManager?.photometry(
                    range = meteringMode,
                    onChangeEv = { value -> onEvMeasured(value) },
                    onRequestCameraPermission = {}
                )
            }
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Metering Mode Selector
        AnimatedVisibility(
            visible = showMeteringSelector,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MeteringModeItem("AVERAGE", meteringMode == LightMetterRange.Default) {
                    onMeteringModeChange(LightMetterRange.Default)
                }
                MeteringModeItem("SPOT", meteringMode == LightMetterRange.Spot) {
                    onMeteringModeChange(LightMetterRange.Spot)
                }
                MeteringModeItem("CENTER", meteringMode == LightMetterRange.Center) {
                    onMeteringModeChange(LightMetterRange.Center)
                }
            }
        }

        // Measured EV Label
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = measuredEv?.let { "MEASURED EV: ${"%.1f".format(it)}" } ?: "TAP TO MEASURE",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Invisible spacer to balance the IconButton on the right
            Box(modifier = Modifier.size(48.dp))

            Button(
                onClick = {
                    if (isPreview) return@Button
                    lightMetterCameraManager?.photometry(
                        range = meteringMode,
                        onChangeEv = { value -> onEvMeasured(value) },
                        onRequestCameraPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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

            // Metering Mode Toggle Button
            IconButton(
                onClick = { showMeteringSelector = !showMeteringSelector },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(48.dp)
                    .background(
                        if (showMeteringSelector) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Metering Mode",
                    tint = if (showMeteringSelector) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MeteringModeItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CarmeraEvCheckButtonPreview() {
    CameraEVCheckButton(
        measuredEv = 12.5,
        meteringMode = LightMetterRange.Default,
        onMeteringModeChange = {},
        onEvMeasured = {}
    )
}
