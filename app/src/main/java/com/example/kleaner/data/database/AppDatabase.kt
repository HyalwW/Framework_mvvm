package com.example.kleaner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kleaner.data.models.Bean
import com.example.utils.Utils

@Database(entities = [Bean::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object Static {
        private var database: AppDatabase = getDatabase()

        fun getDatabase(): AppDatabase {
            return getDatabase(Utils.context)
        }

        fun getDatabase(context: Context): AppDatabase {
            if (database == null) {
                database = Room.databaseBuilder(context, AppDatabase::class.java, "db_main")
                    .allowMainThreadQueries()
                    .build()
            }
            return database
        }

    }

    abstract fun beanDao(): BeanDao
}