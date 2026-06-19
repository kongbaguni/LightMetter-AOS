package net.kongbaguni.lightmetter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lenses")
data class LensEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String,
    val name: String,
    val apertures: List<Double>,
    val isCustom: Boolean = true
)
