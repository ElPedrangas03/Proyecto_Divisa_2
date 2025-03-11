package com.example.proyectodivisa.room

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.proyectodivisa.model.Divisa

@Dao
interface DivisasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exchangeRates: List<Divisa>)

    @Query("SELECT * FROM Divisas WHERE date = :date")
    suspend fun getExchangeRatesByDate(date: Long): List<Divisa>

    @RawQuery
    fun getExchangeRatesCursor(query: SupportSQLiteQuery): Cursor

    // Método para obtener un Cursor de todas las divisas
    fun getExchangeRatesCursor(): Cursor {
        val query = SimpleSQLiteQuery("SELECT * FROM Divisas")
        return getExchangeRatesCursor(query)
    }

    @Query("SELECT * FROM Divisas")
    fun getAllExchangeRates(): Cursor

    @Query("SELECT * FROM Divisas WHERE currency = :currency AND change = :change AND date BETWEEN :startDate AND :endDate")
    fun getExchangeRatesByCurrencyAndDateRange(currency: String, change: String, startDate: String, endDate: String): Cursor
}