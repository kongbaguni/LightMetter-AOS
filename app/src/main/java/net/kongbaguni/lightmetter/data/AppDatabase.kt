package net.kongbaguni.lightmetter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.kongbaguni.lightmetter.data.dao.BodyDao
import net.kongbaguni.lightmetter.data.dao.LensDao
import net.kongbaguni.lightmetter.data.entity.BodyEntity
import net.kongbaguni.lightmetter.data.entity.LensEntity

@Database(entities = [BodyEntity::class, LensEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bodyDao(): BodyDao
    abstract fun lensDao(): LensDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lightmetter_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
