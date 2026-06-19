package net.kongbaguni.lightmetter.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.kongbaguni.lightmetter.data.entity.BodyEntity

@Dao
interface BodyDao {
    @Query("SELECT * FROM bodies ORDER BY name ASC")
    fun getAllBodies(): Flow<List<BodyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBody(body: BodyEntity)

    @Update
    suspend fun updateBody(body: BodyEntity)

    @Delete
    suspend fun deleteBody(body: BodyEntity)

    @Query("SELECT * FROM bodies WHERE id = :id")
    suspend fun getBodyById(id: Int): BodyEntity?
}
