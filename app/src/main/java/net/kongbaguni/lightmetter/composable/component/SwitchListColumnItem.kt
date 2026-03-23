package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SwitchListColumnItem(
    brand: String,
    name: String,
    isSelected: Boolean,
    onClick: (Boolean) -> Unit
) {
    Row(
        Modifier
            .background(Color.White)
            .fillMaxWidth()
    ) {
        ToggleSwitch(isSelected, onClick)

        Text(
            brand,
            Modifier.padding(5.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )

        Text(
            name,
            Modifier.padding(5.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )


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
