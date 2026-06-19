package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kongbaguni.lightmetter.model.FilterModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSelectorDialog(
    filters: List<FilterModel>,
    selectedFilter: FilterModel?,
    onFilterSelected: (FilterModel?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Filter",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterItem(
                        name = "None",
                        stop = 0.0,
                        colorCode = "#00000000",
                        isSelected = selectedFilter == null,
                        onClick = {
                            onFilterSelected(null)
                            onDismiss()
                        }
                    )
                }
                items(filters) { filter ->
                    FilterItem(
                        name = filter.name,
                        stop = filter.stop,
                        colorCode = filter.color,
                        isSelected = filter.id == selectedFilter?.id,
                        onClick = {
                            onFilterSelected(filter)
                            onDismiss()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FilterItem(
    name: String,
    stop: Double,
    colorCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = try { Color(android.graphics.Color.parseColor(colorCode)) } catch (e: Exception) { Color.Gray },
                        shape = CircleShape
                    )
                    .then(
                        if (colorCode == "#00000000") Modifier.background(Color.LightGray, CircleShape) else Modifier
                    )
            )
            
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (stop > 0) {
                    Text(
                        text = "+${stop} stops",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelected) {
                RadioButton(selected = true, onClick = null)
            }
        }
    }
}
