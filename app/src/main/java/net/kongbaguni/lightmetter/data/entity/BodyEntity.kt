package net.kongbaguni.lightmetter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bodies")
data class BodyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String,
    val name: String,
    val shutterSpeeds: List<String>,
    val isCustom: Boolean = true
)
