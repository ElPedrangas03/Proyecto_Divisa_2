package com.example.proyectodivisa

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.example.proyectodivisa.model.Divisa
import com.example.proyectodivisa.room.AppDatabase

class MyWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Obtén los datos de la API con MXN como moneda base
            val response = RetrofitClient.instance.getLatestRates("MXN")

            // Verifica si la respuesta fue exitosa
            if (response.result == "success") {
                // Muestra los datos en la consola
                Log.d("API Response", "Base: ${response.base_code}")
                response.conversion_rates.forEach { (currency, rate) ->
                    Log.d("API Response", "$currency: $rate")
                }

                // Guarda los datos en la base de datos
                val exchangeRates = response.conversion_rates.map { (currency, rate) ->
                    Divisa(currency = currency, rate = rate)
                }
                val database = AppDatabase.getDatabase(applicationContext)
                database.exchangeRateDao().insertAll(exchangeRates)

                Result.success()
            } else {
                Log.e("API Response", "Error en la respuesta: ${response.result}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("API Response", "Error en doWork: ${e.message}")
            Result.failure()
        }
    }
}