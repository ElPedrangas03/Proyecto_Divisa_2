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
        if (uri.lastPathSegment == DivisasContract.PATH_DIVISAS) {
            val requiredPermission = "com.example.proyectodivisacontent.permission.ACCESS_DIVISA"
            if (context?.checkCallingOrSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                throw SecurityException("Se requiere el permiso $requiredPermission para acceder a este ContentProvider.")
            }

            // Extraer parámetros de la URI
            val currency = uri.getQueryParameter("currency")
            val change = uri.getQueryParameter("change")
            val startDateStr = uri.getQueryParameter("startDate")
            val endDateStr = uri.getQueryParameter("endDate")

            if (currency.isNullOrEmpty() || change.isNullOrEmpty() || startDateStr.isNullOrEmpty() || endDateStr.isNullOrEmpty()) {
                Log.e("DivisasProvider", "Faltan parámetros: currency, change, startDate o endDate.")
                return null
            }

            // Formatear fechas correctamente
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
            val startDateFormatted = dateFormat.format(dateFormat.parse(startDateStr) ?: return null)
            val endDateFormatted = dateFormat.format(dateFormat.parse(endDateStr) ?: return null)

            Log.d("DivisasProvider", "Consulta SQL con: currency=$currency, change=$change, startDate=$startDateFormatted, endDate=$endDateFormatted")

            // Ejecutar la consulta en Room
            val cursor = divisasDao.getExchangeRatesByCurrencyAndDateRange(
                currency.uppercase(),
                change.uppercase(),
                startDateFormatted,
                endDateFormatted
            )

            // Crear un MatrixCursor para devolver los resultados filtrados
            val matrixCursor = MatrixCursor(
                arrayOf(
                    DivisasContract.COLUMN_ID,
                    DivisasContract.COLUMN_TIME_LAST_UPDATE,
                    DivisasContract.COLUMN_EXCHANGE_RATE
                )
            )

            cursor?.use { c ->
                val colId = c.getColumnIndexOrThrow("id")
                val colTimeUpdate = c.getColumnIndexOrThrow("date")
                val colRates = c.getColumnIndexOrThrow("rate")

                while (c.moveToNext()) {
                    val id = c.getInt(colId)
                    val timeLastUpdateStr = c.getString(colTimeUpdate)
                    val exchangeRate = c.getDouble(colRates)

                    matrixCursor.addRow(arrayOf(id, timeLastUpdateStr, exchangeRate))
                }
            }

            return matrixCursor
        } else {
            return null
        }
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
        return "vnd.android.cursor.dir/vnd.${DivisasContract.AUTHORITY}.divisas"
    }
}