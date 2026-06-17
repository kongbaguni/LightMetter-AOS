package net.kongbaguni.lightmetter.model

data class IsoModel (
    val value:Int
) {
    fun title(): String {
        return "$value"
    }
}