package net.kongbaguni.lightmetter.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import net.kongbaguni.lightmetter.data.dao.BodyDao
import net.kongbaguni.lightmetter.data.dao.LensDao
import net.kongbaguni.lightmetter.data.entity.BodyEntity
import net.kongbaguni.lightmetter.data.entity.LensEntity
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.model.LensModel

class LightMetterRepository(
    private val context: Context,
    private val bodyDao: BodyDao,
    private val lensDao: LensDao
) {
    fun getAllBodies(): Flow<List<BodyModel>> {
        val assetBodies = BodyModel.load(context)
        return bodyDao.getAllBodies().map { dbBodies ->
            val mappedDbBodies = dbBodies.map { entity ->
                BodyModel(
                    id = entity.id + 10000, // Offset to avoid ID collision with assets
                    brand = entity.brand,
                    name = entity.name,
                    shutterSpeeds = entity.shutterSpeeds
                )
            }
            assetBodies + mappedDbBodies
        }
    }

    fun getAllLenses(): Flow<List<LensModel>> {
        val assetLenses = LensModel.load(context)
        return lensDao.getAllLenses().map { dbLenses ->
            val mappedDbLenses = dbLenses.map { entity ->
                LensModel(
                    id = entity.id + 10000,
                    brand = entity.brand,
                    name = entity.name,
                    apertures = entity.apertures
                )
            }
            assetLenses + mappedDbLenses
        }
    }

    suspend fun insertBody(brand: String, name: String, shutterSpeeds: List<String>) {
        bodyDao.insertBody(BodyEntity(brand = brand, name = name, shutterSpeeds = shutterSpeeds))
    }

    suspend fun updateBody(id: Int, brand: String, name: String, shutterSpeeds: List<String>) {
        val entityId = id - 10000
        if (entityId >= 0) {
            bodyDao.updateBody(BodyEntity(id = entityId, brand = brand, name = name, shutterSpeeds = shutterSpeeds))
        }
    }

    suspend fun deleteBody(id: Int) {
        val entityId = id - 10000
        if (entityId >= 0) {
            val entity = bodyDao.getBodyById(entityId)
            if (entity != null) {
                bodyDao.deleteBody(entity)
            }
        }
    }

    suspend fun insertLens(brand: String, name: String, apertures: List<Double>) {
        lensDao.insertLens(LensEntity(brand = brand, name = name, apertures = apertures))
    }

    suspend fun updateLens(id: Int, brand: String, name: String, apertures: List<Double>) {
        val entityId = id - 10000
        if (entityId >= 0) {
            lensDao.updateLens(LensEntity(id = entityId, brand = brand, name = name, apertures = apertures))
        }
    }

    suspend fun deleteLens(id: Int) {
        val entityId = id - 10000
        if (entityId >= 0) {
            val entity = lensDao.getLensById(entityId)
            if (entity != null) {
                lensDao.deleteLens(entity)
            }
        }
    }
}
