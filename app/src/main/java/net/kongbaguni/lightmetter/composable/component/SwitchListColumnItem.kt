package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SwitchListColumnItem(
    brand: String,
    name: String,
    isSelected: Boolean,
    onClick: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = if (isSelected) Color(0xFFE0E0E0) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        onClick = { onClick(!isSelected) }
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = brand,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = if (isSelected) Color.Black else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.Black else Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            ToggleSwitch(
                checked = isSelected,
                onCheckedChange = onClick,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Preview
@Composable
fun SwitchListColumnItemPreview() {
    Column() {
        SwitchListColumnItem(
            brand = "Leica",
            name = "I",
            isSelected = true,
            onClick = {}
        )
        SwitchListColumnItem(
            brand = "Leica",
            name = "M3",
            isSelected = false,
            onClick = {}
        )

    }
}
