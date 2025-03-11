package com.example.proyectodivisa.provider

import android.net.Uri
import com.example.proyectodivisa.model.Divisa

object DivisasContract
{
    // Autoridad del ContentProvider (debe ser única en el dispositivo)
    const val AUTHORITY = "com.example.proyectodivisacontent.provider"

    const val PATH_DIVISAS = "divisas"
    //const val PATH_DIVISAS_BY_CURRENCY_AND_CHANGE_AND_DATE = "divisas_by_currency_and_change_and_date"

    // URI base para acceder al ContentProvider
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_DIVISAS")

    // URI para consultar por moneda y rango de fechas (Esto es la manera a llamar desde fuera del proyecto o dentro del mismo)
    //val CONTENT_URI_BY_CURRENCY_AND_CHANGE_AND_DATE: Uri = Uri.parse("content://$AUTHORITY/$PATH_DIVISAS_BY_CURRENCY_AND_CHANGE_AND_DATE")

    // const val PATH_EXCHANGE_RATES = "exchange_rates"

    const val COLUMN_ID = "id"
    const val COLUMN_TIME_LAST_UPDATE = "date"
    const val COLUMN_EXCHANGE_RATE = "rate"
}