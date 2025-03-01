package com.example.proyectodivisa.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.proyectodivisa.room.AppDatabase
import com.example.proyectodivisa.room.DivisasDao

class DivisasProvider : ContentProvider() {

    private lateinit var divisasDao: DivisasDao

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(DivisasContract.AUTHORITY, DivisasContract.PATH_DIVISAS, CODE_DIVISAS)
        addURI(DivisasContract.AUTHORITY, "${DivisasContract.PATH_DIVISAS_BY_CURRENCY_AND_CHANGE_AND_DATE}/*/*/*/*", CODE_DIVISAS_BY_CURRENCY_AND_DATE)
    }

    companion object {
        private const val CODE_DIVISAS = 100 // Código para identificar la tabla de divisas
        private const val CODE_DIVISAS_BY_CURRENCY_AND_DATE = 101 // Código para consultas con parámetros
    }

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
        return when (uriMatcher.match(uri)) {
            CODE_DIVISAS -> {
                // Consulta general (sin parámetros)
                Log.d("DivisasProvider", "Consultando la tabla de divisas")
                val cursor = divisasDao.getExchangeRatesCursor()
                cursor.setNotificationUri(context?.contentResolver, uri)
                cursor
            }
            CODE_DIVISAS_BY_CURRENCY_AND_DATE -> {
                // Consulta con parámetros (moneda y rango de fechas)
                val currency = uri.pathSegments[1] // Moneda (ej. "USD")
                val change = uri.pathSegments[2] // Cambio de moneda
                val startDate = uri.pathSegments[3] // Fecha inicial (ej. "2023-10-25")
                val endDate = uri.pathSegments[4] // Fecha final (ej. "2023-10-26")

                Log.d("DivisasProvider", "Consultando divisas para $currency con cambio en $change entre $startDate y $endDate")

                // Realiza la consulta con parámetros
                val cursor = divisasDao.getExchangeRatesByCurrencyAndDateRange(currency, change, startDate, endDate)
                cursor.setNotificationUri(context?.contentResolver, uri)
                cursor
            }
            else -> throw IllegalArgumentException("URI desconocida: $uri")
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
        return when (uriMatcher.match(uri)) {
            CODE_DIVISAS -> "vnd.android.cursor.dir/vnd.com.example.proyectodivisa.divisas"
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }
}