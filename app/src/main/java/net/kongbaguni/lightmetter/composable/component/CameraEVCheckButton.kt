package net.kongbaguni.lightmetter.composable.component

import android.Manifest
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as ComposeSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.R
import net.kongbaguni.lightmetter.model.LightMetterModel
import net.kongbaguni.lightmetter.model.LightMetterRange
import net.kongbaguni.lightmetter.utill.DataStore
import net.kongbaguni.lightmetter.utill.LightMetterCameraManager

@Composable
fun CameraEVCheckButton(
    measuredEv: Double?,
    calculatedEv: Double?,
    meteringMode: LightMetterRange,
    onMeteringModeChange: (LightMetterRange) -> Unit,
    onEvMeasured: (LightMetterModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var showMeteringSelector by remember { mutableStateOf(false) }
    var showFilterSelector by remember { mutableStateOf(false) }
    var isMeasuring by remember { mutableStateOf(false) }
    var previewSurface by remember { mutableStateOf<Surface?>(null) }

    val dataStore = remember { DataStore(context) }
    val scope = rememberCoroutineScope()
    val selectedFilter by dataStore.selectedFilter.collectAsState(initial = null)
    val showPreview by dataStore.showPreview.collectAsState(initial = true)

    if (showFilterSelector) {
        FilterSelectorDialog(
            filters = dataStore.filterList,
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                scope.launch { dataStore.saveFilter(filter) }
            },
            onDismiss = { showFilterSelector = false }
        )
    }

    val lightMetterCameraManager = remember {
        if (!isPreview) LightMetterCameraManager(context) else null
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && !isPreview) {
                isMeasuring = true
                lightMetterCameraManager?.photometry(
                    range = meteringMode,
                    previewSurface = previewSurface,
                    onChangeEv = { value ->
                        onEvMeasured(value)
                        isMeasuring = false
                    },
                    onRequestCameraPermission = {}
                )
            }
        }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Camera Preview Overlay - Only shown when measurement is active AND preview is enabled
        AnimatedVisibility(
            visible = isMeasuring && showPreview,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.padding(bottom = 160.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).apply {
                            holder.addCallback(object : SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    previewSurface = holder.surface
                                }
                                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                                override fun surfaceDestroyed(holder: SurfaceHolder) {
                                    previewSurface = null
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Metering Area Indicator
                MeteringGuide(mode = meteringMode)
            }
        }

        // Trigger measurement when surface is ready (or immediately if preview is disabled)
        androidx.compose.runtime.LaunchedEffect(isMeasuring, previewSurface, showPreview) {
            if (isMeasuring && !isPreview) {
                if (showPreview && previewSurface == null) return@LaunchedEffect

                lightMetterCameraManager?.photometry(
                    range = meteringMode,
                    previewSurface = if (showPreview) previewSurface else null,
                    onChangeEv = { value ->
                        onEvMeasured(value)
                        isMeasuring = false
                    },
                    onRequestCameraPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                        isMeasuring = false
                    }
                )
            }
        }

        Column(
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
                    MeteringModeItem(LightMetterRange.Default, meteringMode == LightMetterRange.Default) {
                        onMeteringModeChange(LightMetterRange.Default)
                    }
                    MeteringModeItem(LightMetterRange.Spot, meteringMode == LightMetterRange.Spot) {
                        onMeteringModeChange(LightMetterRange.Spot)
                    }
                    MeteringModeItem(LightMetterRange.Center, meteringMode == LightMetterRange.Center) {
                        onMeteringModeChange(LightMetterRange.Center)
                    }

                    // Preview Toggle Button
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp, end = 4.dp)
                            .width(1.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            .align(Alignment.CenterVertically)
                    )

                    IconButton(
                        onClick = {
                            scope.launch { dataStore.saveShowPreview(!showPreview) }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (showPreview) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Preview",
                            tint = if (showPreview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Measured EV Label
            ComposeSurface(
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
                // Filter Selection Button
                IconButton(
                    onClick = { showFilterSelector = true },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = try {
                                    selectedFilter?.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Transparent
                                } catch (e: Exception) {
                                    Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .then(
                                if (selectedFilter == null) Modifier.background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    CircleShape
                                ) else Modifier
                            )
                    )
                }

                Button(
                    onClick = {
                        if (isPreview) return@Button
                        if (!isMeasuring) {
                            isMeasuring = true
                        }
                    },
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EVIndecator(
                            measuredEv = measuredEv,
                            calculatedEv = calculatedEv,
                            isMeasuring = isMeasuring
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("측광", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    MeteringModeIcon(
                        mode = meteringMode,
                        color = if (showMeteringSelector) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MeteringGuide(mode: LightMetterRange) {
    Canvas(modifier = Modifier.size(100.dp)) {
        val strokeWidth = 2.dp.toPx()
        val color = Color.White.copy(alpha = 0.8f)

        when (mode) {
            LightMetterRange.Spot -> {
                drawCircle(
                    color = color,
                    radius = 10.dp.toPx(),
                    style = Stroke(width = strokeWidth)
                )
                // Center dot
                drawCircle(
                    color = color,
                    radius = 2.dp.toPx()
                )
            }
            LightMetterRange.Center -> {
                drawCircle(
                    color = color,
                    radius = 40.dp.toPx(),
                    style = Stroke(width = strokeWidth)
                )
            }
            LightMetterRange.Default -> {
                // Large rectangle or crosshair
                val size = size.width * 0.8f
                drawRect(
                    color = color,
                    size = androidx.compose.ui.geometry.Size(size, size),
                    topLeft = Offset((this.size.width - size) / 2, (this.size.height - size) / 2),
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}

@Composable
fun MeteringModeIcon(
    mode: LightMetterRange,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val width = size.width
        val height = size.height
        val strokeWidth = 1.5.dp.toPx()

        // Outer frame
        drawRect(
            color = color,
            style = Stroke(width = strokeWidth),
            size = size
        )

        when (mode) {
            LightMetterRange.Spot -> {
                drawCircle(
                    color = color,
                    radius = 2.dp.toPx(),
                    center = center
                )
            }
            LightMetterRange.Center -> {
                drawCircle(
                    color = color,
                    radius = 5.dp.toPx(),
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            }
            LightMetterRange.Default -> { // Average / Matrix
                val padding = 4.dp.toPx()
                drawLine(color, Offset(width / 3, padding), Offset(width / 3, height - padding), strokeWidth)
                drawLine(color, Offset(2 * width / 3, padding), Offset(2 * width / 3, height - padding), strokeWidth)
                drawLine(color, Offset(padding, height / 2), Offset(width - padding, height / 2), strokeWidth)
            }
        }
    }
}

@Composable
fun MeteringModeItem(
    mode: LightMetterRange,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = when (mode) {
        LightMetterRange.Default -> stringResource(R.string.metering_average)
        LightMetterRange.Spot -> stringResource(R.string.metering_spot)
        LightMetterRange.Center -> stringResource(R.string.metering_center)
    }

    ComposeSurface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            MeteringModeIcon(
                mode = mode,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CarmeraEvCheckButtonPreview() {
    CameraEVCheckButton(
        measuredEv = 12.5,
        calculatedEv = 12.0,
        meteringMode = LightMetterRange.Default,
        onMeteringModeChange = {},
        onEvMeasured = {}
    )
}
