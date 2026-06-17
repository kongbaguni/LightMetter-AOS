import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.kongbaguni.lightmetter.extensions.dataStore
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.model.BodyUiState
import net.kongbaguni.lightmetter.model.LensModel
import net.kongbaguni.lightmetter.model.LensUiState

class DataStore(context: Context) {

    private val dataStore = context.dataStore

    val bodyList: List<BodyModel> = BodyModel.load(context)
    val lensList: List<LensModel> = LensModel.load(context)

    companion object {
        private val BODY_ID = intPreferencesKey("body_id")
        private val LENS_ID = intPreferencesKey("lens_id")
        private val APERTURE_VALUE = doublePreferencesKey("aperture_value")
        private val SHUTTER_SPEED_VALUE = stringPreferencesKey("shutter_speed_value")
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
        dataStore.data.map { prefs ->
            val id = prefs[BODY_ID]
            bodyList.firstOrNull { it.id == id } ?: bodyList.first()
        }

    /** 선택된 Lens */
    val selectedLens: Flow<LensModel> =
        dataStore.data.map { prefs ->
            val id = prefs[LENS_ID]
            lensList.firstOrNull { it.id == id } ?: lensList.first()
        }

    val bodyUiState: Flow<BodyUiState> =
        dataStore.data.map { prefs ->
            val id = prefs[BODY_ID]

            val selected = bodyList.find { it.id == id }
                ?: bodyList.firstOrNull()

            BodyUiState(
                bodies = bodyList,
                selected = selected
            )
        }

    val lensUiState: Flow<LensUiState> =
        dataStore.data.map { prefs ->
            val id = prefs[LENS_ID]

            val selected = lensList.find { it.id == id }
                ?: lensList.firstOrNull()

            LensUiState(
                lensList = lensList,
                selected = selected
            )
        }

}