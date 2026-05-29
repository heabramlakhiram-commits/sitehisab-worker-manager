package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.SiteHisabDao
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker

@Database(entities = [Worker::class, Attendance::class, Payment::class], version = 1, exportSchema = false)
abstract class SiteHisabDatabase : RoomDatabase() {
    abstract fun siteHisabDao(): SiteHisabDao

    companion object {
        @Volatile
        private var INSTANCE: SiteHisabDatabase? = null

        fun getDatabase(context: Context): SiteHisabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SiteHisabDatabase::class.java,
                    "site_hisab_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
