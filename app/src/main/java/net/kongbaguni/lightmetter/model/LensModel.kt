package net.kongbaguni.lightmetter.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LensModel(
    val id: Int,
    val brand: String,
    val name: String,
    val apertures: List<Double>
) {
    companion object {
        fun load(context: Context): List<LensModel> {
            val json = context.assets
                .open("lensList.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<LensModel>>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}

