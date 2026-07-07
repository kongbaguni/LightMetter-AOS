package net.kongbaguni.lightmetter.utill

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.kongbaguni.lightmetter.data.AppDatabase
import net.kongbaguni.lightmetter.data.LightMetterRepository
import net.kongbaguni.lightmetter.extensions.dataStore
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.model.BodyUiState
import net.kongbaguni.lightmetter.model.FilterModel
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

    val filterList: List<FilterModel> = try {
        val json = context.assets.open("filterList.json").bufferedReader().use { it.readText() }
        Gson().fromJson(json, object : TypeToken<List<FilterModel>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }

    companion object {
        private val BODY_ID = intPreferencesKey("body_id")
        private val LENS_ID = intPreferencesKey("lens_id")
        private val APERTURE_VALUE = doublePreferencesKey("aperture_value")
        private val SHUTTER_SPEED_VALUE = stringPreferencesKey("shutter_speed_value")
        private val ISO_VALUE = intPreferencesKey("iso_value")
        private val SELECTED_BRAND = stringPreferencesKey("selected_brand")
        private val FILTER_ID = intPreferencesKey("filter_id")
        private val SHOW_PREVIEW = booleanPreferencesKey("show_preview")
        private val AD_FREE_UNTIL = longPreferencesKey("ad_free_until")
        private val IS_SUBSCRIPTION_ACTIVE = booleanPreferencesKey("is_subscription_active")
    }

    /** 현재 광고 제거 만료 시간 가져오기 */
    suspend fun getAdFreeUntil(): Long {
        return dataStore.data.map { it[AD_FREE_UNTIL] ?: 0L }.first()
    }

    /** 광고 제거 만료 시간 저장 (일회성 구매용 - 기존 기간이 남았으면 연장) */
    suspend fun addAdFreeDays(days: Int) {
        dataStore.edit {
            val currentExpiry = it[AD_FREE_UNTIL] ?: 0L
            val startTime = if (currentExpiry > System.currentTimeMillis()) currentExpiry else System.currentTimeMillis()
            val addMillis = days.toLong() * 24 * 60 * 60 * 1000
            it[AD_FREE_UNTIL] = startTime + addMillis
        }
    }

    /** 복구용: 특정 타임스탬프로 만료일 강제 설정 (더 먼 미래일 때만) */
    suspend fun restoreAdFreeUntil(timestamp: Long) {
        dataStore.edit {
            val currentExpiry = it[AD_FREE_UNTIL] ?: 0L
            if (timestamp > currentExpiry) {
                it[AD_FREE_UNTIL] = timestamp
            }
        }
    }

    /** 정기 구독 활성화 여부 저장 */
    suspend fun saveSubscriptionActive(active: Boolean) {
        dataStore.edit {
            it[IS_SUBSCRIPTION_ACTIVE] = active
        }
    }

    /** 정기 구독 중인지 여부 */
    val isSubscriptionActive: Flow<Boolean> =
        dataStore.data.map { it[IS_SUBSCRIPTION_ACTIVE] ?: false }

    /** 광고 제거 여부 (정기 구독 중이거나 일회성 구매 만료 전인지 체크) */
    val isAdFree: Flow<Boolean> =
        dataStore.data.map { 
            val expiry = it[AD_FREE_UNTIL] ?: 0L
            val isSubActive = it[IS_SUBSCRIPTION_ACTIVE] ?: false
            isSubActive || expiry > System.currentTimeMillis()
        }

    /** 미리보기 표시 여부 저장 */
    suspend fun saveShowPreview(show: Boolean) {
        dataStore.edit {
            it[SHOW_PREVIEW] = show
        }
    }

    /** 미리보기 표시 여부 */
    val showPreview: Flow<Boolean> =
        dataStore.data.map { it[SHOW_PREVIEW] ?: true }

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
    val selectedApertureValue: Flow<Double> =
        dataStore.data.map { it[APERTURE_VALUE] ?: 5.6 }

    /** 선택된 Shutter Speed value */
    val selectedShutterSpeedValue: Flow<String> =
        dataStore.data.map { it[SHUTTER_SPEED_VALUE] ?: "1/125" }

    /** 선택된 ISO value */
    val selectedIsoValue: Flow<Int> =
        dataStore.data.map { it[ISO_VALUE] ?: 200 }

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

    /** Filter 저장 */
    suspend fun saveFilter(filter: FilterModel?) {
        dataStore.edit {
            if (filter == null) {
                it.remove(FILTER_ID)
            } else {
                it[FILTER_ID] = filter.id
            }
        }
    }

    /** 선택된 Body */
    val selectedBody: Flow<BodyModel> =
        combine(dataStore.data, repository.getAllBodies()) { prefs, bodyList ->
            val id = prefs[BODY_ID]
            bodyList.firstOrNull { it.id == id } 
                ?: bodyList.firstOrNull { it.id == 10 } 
                ?: bodyList.firstOrNull() 
                ?: BodyModel(0, "", "", emptyList())
        }

    /** 선택된 Lens */
    val selectedLens: Flow<LensModel> =
        combine(dataStore.data, repository.getAllLenses()) { prefs, lensList ->
            val id = prefs[LENS_ID]
            lensList.firstOrNull { it.id == id } 
                ?: lensList.firstOrNull { it.id == 16 } 
                ?: lensList.firstOrNull() 
                ?: LensModel(0, "", "", emptyList())
        }

    /** 선택된 Filter */
    val selectedFilter: Flow<FilterModel?> =
        dataStore.data.map { prefs ->
            val id = prefs[FILTER_ID]
            filterList.find { it.id == id }
        }

    val bodyUiState: Flow<BodyUiState> =
        combine(dataStore.data, repository.getAllBodies()) { prefs, bodyList ->
            val id = prefs[BODY_ID]
            val selected = bodyList.find { it.id == id } 
                ?: bodyList.find { it.id == 10 } 
                ?: bodyList.firstOrNull()

            BodyUiState(
                bodies = bodyList,
                selected = selected
            )
        }

    val lensUiState: Flow<LensUiState> =
        combine(dataStore.data, repository.getAllLenses()) { prefs, lensList ->
            val id = prefs[LENS_ID]
            val selected = lensList.find { it.id == id } 
                ?: lensList.find { it.id == 16 } 
                ?: lensList.firstOrNull()

            LensUiState(
                lensList = lensList,
                selected = selected
            )
        }
}
