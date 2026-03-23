package net.kongbaguni.lightmetter.extensions

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "lightmeter_settings")