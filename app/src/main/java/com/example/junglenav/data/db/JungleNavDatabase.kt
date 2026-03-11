package com.example.junglenav.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.junglenav.data.db.dao.MapPackageDao
import com.example.junglenav.data.db.dao.TrackDao
import com.example.junglenav.data.db.dao.WaypointDao
import com.example.junglenav.data.db.entity.MapPackageEntity
import com.example.junglenav.data.db.entity.TrackPointEntity
import com.example.junglenav.data.db.entity.TrackSessionEntity
import com.example.junglenav.data.db.entity.WaypointEntity

@Database(
    entities = [
        WaypointEntity::class,
        TrackSessionEntity::class,
        TrackPointEntity::class,
        MapPackageEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class JungleNavDatabase : RoomDatabase() {
    abstract fun waypointDao(): WaypointDao
    abstract fun trackDao(): TrackDao
    abstract fun mapPackageDao(): MapPackageDao

    companion object {
        fun create(context: Context): JungleNavDatabase {
            return Room.databaseBuilder(
                context,
                JungleNavDatabase::class.java,
                "junglenav.db",
            ).fallbackToDestructiveMigration(dropAllTables = true).build()
        }

        fun inMemory(context: Context): JungleNavDatabase {
            return Room.inMemoryDatabaseBuilder(
                context,
                JungleNavDatabase::class.java,
            ).allowMainThreadQueries().build()
        }
    }
}
