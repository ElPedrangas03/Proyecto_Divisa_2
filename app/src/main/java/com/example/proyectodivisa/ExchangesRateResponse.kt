package com.example.proyectodivisa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Data class para la respuesta de la API
data class ExchangeRatesResponse(
    val result: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

// Interfaz de la API
interface ExchangeRateApiService {
    @GET("latest/{base}")
    suspend fun getLatestRates(@Path("base") baseCurrency: String): ExchangeRatesResponse
}

// Objeto singleton para Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://v6.exchangerate-api.com/v6/bf27be13817027736e76efb7/" // Reemplaza YOUR_API_KEY

    val instance: ExchangeRateApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApiService::class.java)
    }
}
