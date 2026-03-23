package net.kongbaguni.lightmetter.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
data class BodyModel (
    val id: Int,
    val brand : String,
    val name : String,
    val shutterSpeeds : List<String>
) {

    companion object {
        fun load(context: Context): List<BodyModel> {
            val json = context.assets
                .open("bodyList.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<BodyModel>>() {}.type

            return Gson().fromJson(json, type)
        }
    }
    fun shutterValues() : List<Double> {
        return shutterSpeeds.map {
            shutterStringToDouble(it)
        }
    }

    fun shutterStringToDouble(value: String): Double {
        return try {
            if (value.contains("/")) {
                val (a, b) = value.split("/")
                a.toDouble() / b.toDouble()
            } else {
                value.toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }
}
