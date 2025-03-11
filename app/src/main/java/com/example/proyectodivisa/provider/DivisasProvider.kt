package com.example.proyectodivisa.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.example.proyectodivisa.room.AppDatabase
import com.example.proyectodivisa.room.DivisasDao
import com.example.proyectodivisa.provider.Converters
import java.text.SimpleDateFormat
import java.util.*

class DivisasProvider : ContentProvider() {

    private lateinit var divisasDao: DivisasDao

    override fun onCreate(): Boolean {
        // Inicializa la base de datos y el DAO
        val database = AppDatabase.getDatabase(context!!)
        divisasDao = database.exchangeRateDao()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        // Verifica que la aplicación que hace la consulta tenga el permiso necesario
        val requiredPermission = "com.example.proyectodivisacontent.permission.ACCESS_DIVISA"
        if (context?.checkCallingOrSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Se requiere el permiso $requiredPermission para acceder a este ContentProvider.")
        }

        // Extrae los parámetros enviados en la consulta (la moneda y el rango de fechas)
        val currency = uri.getQueryParameter("currency") // Moneda solicitada (ejemplo: "USD")
        val startDateStr = uri.getQueryParameter("startDate") // Fecha de inicio del rango
        val endDateStr = uri.getQueryParameter("endDate") // Fecha de fin del rango

        // Si falta algún parámetro, se muestra un error y se detiene la consulta (para debug)
        if (currency.isNullOrEmpty() || startDateStr.isNullOrEmpty() || endDateStr.isNullOrEmpty()) {
            Log.e("DivisasProvider", "Faltan parámetros: currency, startDate o endDate.")
            return null
        }

        // Convertir las fechas recibidas (en formato String) a objetos Date
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
        val startDate: Date
        val endDate: Date
        try {
            startDate = dateFormat.parse(startDateStr) ?: return null
            endDate = dateFormat.parse(endDateStr) ?: return null
        } catch (e: Exception) {
            Log.e("DivisasProvider", "Error al parsear fechas: ${e.localizedMessage}")
            return null
        }

        // Obtener todos los registros de la base de datos
        val rawCursor = divisasDao.getExchangeRatesCursor()

        // Cursor para devolver los resultados filtrados
        val matrixCursor = MatrixCursor(
            arrayOf(
                DivisasContract.COLUMN_ID, // ID del registro
                DivisasContract.COLUMN_TIME_LAST_UPDATE, // Fecha de la actualización
                DivisasContract.COLUMN_EXCHANGE_RATE // Valor de la tasa de cambio
            )
        )

        // Recorrer todos los registros obtenidos de la base de datos y filtrarlos
        rawCursor?.use { cursor ->
            // Índices de columnas
            val colId = cursor.getColumnIndexOrThrow("id")
            val colTimeUpdate = cursor.getColumnIndexOrThrow("date")
            val colRates = cursor.getColumnIndexOrThrow("rate")

            while (cursor.moveToNext()) {
                val id = cursor.getInt(colId)
                val timeLastUpdateStr = cursor.getString(colTimeUpdate) // Fecha de actualización (String)
                val ratesJson = cursor.getString(colRates) // JSON con las tasas de cambio

                // Convertir la fecha obtenida de la BD a un objeto Date
                val recordDate = try {
                    dateFormat.parse(timeLastUpdateStr)
                } catch (e: Exception) {
                    null
                }

                if (recordDate != null) {
                    // Verificar si la fecha está dentro del rango solicitado por el usuario
                    if (!recordDate.before(startDate) && !recordDate.after(endDate)) {

                        // Convertir los valores JSON en un mapa de tasas de cambio
                        val rateMap = Converters().toRatesMap(ratesJson)
                        val exchangeRate = rateMap[currency] // Obtener la tasa de cambio solicitada

                        // Si la tasa de cambio existe, se agrega al cursor
                        if (exchangeRate != null) {
                            matrixCursor.addRow(arrayOf(id, timeLastUpdateStr, exchangeRate))
                        }
                    }
                }
            }
        }
        // Se devuelven los datos filtrados en el cursor
        return matrixCursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert no está soportado")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("Update no está soportado")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Delete no está soportado")
    }

    override fun getType(uri: Uri): String? {
        return "vnd.android.cursor.dir/vnd.${DivisasContract.AUTHORITY}.exchange_rates"
    }
}