package com.example.proyectodivisa.provider

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.proyectodivisa.model.ExchangeRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DivisasRepository(private val context: Context) {

    suspend fun fetchExchangeRates(currency: String, change: String, startDate: String, endDate: String): List<ExchangeRate> {
        return withContext(Dispatchers.IO) {
            val uri = Uri.parse("content://com.example.proyectodivisa.provider/divisas_by_currency_and_change_and_date/$currency/$change/$startDate/$endDate")
            val cursor = context.contentResolver.query(uri, null, null, null, null)

            val exchangeRates = mutableListOf<ExchangeRate>()
            cursor?.use {
                Log.d("DivisasRepository", "Número de registros en el cursor: ${cursor.count}")
                while (it.moveToNext()) {
                    val date = it.getString(it.getColumnIndexOrThrow(DivisasContract.DivisasColumns.DATE))
                    val rate = it.getDouble(it.getColumnIndexOrThrow(DivisasContract.DivisasColumns.RATE))
                    val cambio = it.getString(it.getColumnIndexOrThrow(DivisasContract.DivisasColumns.CHANGE))
                    exchangeRates.add(ExchangeRate(date, rate, cambio))
                }
            }
            Log.d("DivisasRepository", "Datos obtenidos desde el ContentProvider: ${exchangeRates.size} registros")
            exchangeRates
        }
    }
}