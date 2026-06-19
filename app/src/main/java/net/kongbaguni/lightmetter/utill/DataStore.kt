package net.kongbaguni.lightmetter.utill

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import net.kongbaguni.lightmetter.data.AppDatabase
import net.kongbaguni.lightmetter.data.LightMetterRepository
import net.kongbaguni.lightmetter.extensions.dataStore
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.model.BodyUiState
import net.kongbaguni.lightmetter.model.IsoModel
import net.kongbaguni.lightmetter.model.LensModel
import net.kongbaguni.lightmetter.model.LensUiState

class DataStore(context: Context) {

    private val dataStore = context.dataStore
    private val database = AppDatabase.getDatabase(context)
    val repository = LightMetterRepository(
        context,
        database.bodyDao(),
        database.lensDao()
    )

    val isoList: List<IsoModel> = listOf(
        6, 12, 25, 32, 40, 50, 64, 80, 100, 125, 160, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 3200, 6400
    ).map { IsoModel(it) }

    companion object {
        private val BODY_ID = intPreferencesKey("body_id")
        private val LENS_ID = intPreferencesKey("lens_id")
        private val APERTURE_VALUE = doublePreferencesKey("aperture_value")
        private val SHUTTER_SPEED_VALUE = stringPreferencesKey("shutter_speed_value")
        private val ISO_VALUE = intPreferencesKey("iso_value")
        private val SELECTED_BRAND = stringPreferencesKey("selected_brand")
    }

    /** 브랜드 필터 저장 */
    suspend fun saveSelectedBrand(brand: String?) {
        dataStore.edit {
            if (brand == null) {
                it.remove(SELECTED_BRAND)
            } else {
                it[SELECTED_BRAND] = brand
            }
        }
    }

    /** 선택된 브랜드 필터 */
    val selectedBrand: Flow<String?> =
        dataStore.data.map { it[SELECTED_BRAND] }

    /** ISO 저장 */
    suspend fun saveIso(value: Int) {
        dataStore.edit {
            it[ISO_VALUE] = value
        }
    }

    /** Aperture 저장 */
    suspend fun saveAperture(value: Double) {
        dataStore.edit {
            it[APERTURE_VALUE] = value
        }
    }

    /** Shutter Speed 저장 */
    suspend fun saveShutterSpeed(value: String) {
        dataStore.edit {
            it[SHUTTER_SPEED_VALUE] = value
        }
    }

    /** 선택된 Aperture value */
    val selectedApertureValue: Flow<Double?> =
        dataStore.data.map { it[APERTURE_VALUE] }

    /** 선택된 Shutter Speed value */
    val selectedShutterSpeedValue: Flow<String?> =
        dataStore.data.map { it[SHUTTER_SPEED_VALUE] }

    /** 선택된 ISO value */
    val selectedIsoValue: Flow<Int?> =
        dataStore.data.map { it[ISO_VALUE] }

    /** Body 저장 */
    suspend fun saveBody(body: BodyModel) {
        dataStore.edit {
            it[BODY_ID] = body.id
        }
    }

    /** Lens 저장 */
    suspend fun saveLens(lens: LensModel) {
        dataStore.edit {
            it[LENS_ID] = lens.id
        }
    }

    /** 선택된 Body */
    val selectedBody: Flow<BodyModel> =
        combine(dataStore.data, repository.getAllBodies()) { prefs, bodyList ->
            val id = prefs[BODY_ID]
            bodyList.firstOrNull { it.id == id } ?: bodyList.firstOrNull() ?: BodyModel(0, "", "", emptyList())
        }

    /** 선택된 Lens */
    val selectedLens: Flow<LensModel> =
        combine(dataStore.data, repository.getAllLenses()) { prefs, lensList ->
            val id = prefs[LENS_ID]
            lensList.firstOrNull { it.id == id } ?: lensList.firstOrNull() ?: LensModel(0, "", "", emptyList())
        }

    val bodyUiState: Flow<BodyUiState> =
        combine(dataStore.data, repository.getAllBodies()) { prefs, bodyList ->
            val id = prefs[BODY_ID]
            val selected = bodyList.find { it.id == id } ?: bodyList.firstOrNull()

            BodyUiState(
                bodies = bodyList,
                selected = selected
            )
        }

    val lensUiState: Flow<LensUiState> =
        combine(dataStore.data, repository.getAllLenses()) { prefs, lensList ->
            val id = prefs[LENS_ID]
            val selected = lensList.find { it.id == id } ?: lensList.firstOrNull()

            LensUiState(
                lensList = lensList,
                selected = selected
            )
        }
}
