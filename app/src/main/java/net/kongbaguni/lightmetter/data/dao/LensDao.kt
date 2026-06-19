package net.kongbaguni.lightmetter.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.kongbaguni.lightmetter.data.entity.LensEntity

@Dao
interface LensDao {
    @Query("SELECT * FROM lenses ORDER BY name ASC")
    fun getAllLenses(): Flow<List<LensEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLens(lens: LensEntity)

    @Update
    suspend fun updateLens(lens: LensEntity)

    @Delete
    suspend fun deleteLens(lens: LensEntity)

    @Query("SELECT * FROM lenses WHERE id = :id")
    suspend fun getLensById(id: Int): LensEntity?
}
