package net.kongbaguni.lightmetter.model

import kotlin.math.log2

data class LightMetterModel(
    val iso: Double,
    val shutter: Double,
    val aperture: Double
) {
    fun getEv(): Double {
        val ev100 = log2((aperture * aperture) / shutter)
        val isoCorrection = log2(iso / 100.0)
        return ev100 - isoCorrection
    }
}