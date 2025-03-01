package com.example.proyectodivisa.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyectodivisa.model.Divisa

@Database(entities = [Divisa::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exchangeRateDao(): DivisasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "DivisasDB"
                )//.fallbackToDestructiveMigration()    <-- Borra los datos anteriores permitiendo cambios
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

