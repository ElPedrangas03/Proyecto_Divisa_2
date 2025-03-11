package com.example.proyectodivisa.provider

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    fun toRatesMap(json: String): Map<String, Double> {
        return try {
            if (json.trim().startsWith("{")) {
                Gson().fromJson(json, object : TypeToken<Map<String, Double>>() {}.type)
            } else {
                Log.w("Converters", "Recibido un número en lugar de JSON: $json")
                mapOf("DEFAULT" to json.toDouble()) // Retorna un mapa con un valor genérico
            }
        } catch (e: Exception) {
            Log.e("Converters", "Error al parsear JSON: ${e.localizedMessage}")
            emptyMap()
        }
    }


}