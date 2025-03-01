package com.example.proyectodivisa

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.proyectodivisa.provider.DivisasContract
import com.example.proyectodivisa.room.AppDatabase
import com.example.proyectodivisa.ui.theme.ProyectoDivisaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Programa la tarea cada hora
        WorkManagerUtils.scheduleHourlyTask(this)

        // Establece el contenido de la actividad usando Compose
        setContent {
            ProyectoDivisaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("WorkManager App")
                }
            }
        }

        GlobalScope.launch {
            val database = AppDatabase.getDatabase(this@MainActivity)
            val exchangeRates = database.exchangeRateDao().getExchangeRatesByDate(System.currentTimeMillis())
            exchangeRates.forEach {
                Log.d("Database", "${it.currency}: ${it.rate}")
            }
        }

        // Prueba el ContentProvider con parámetros
        lifecycleScope.launch(Dispatchers.IO) {
            testContentProviderWithParams(this@MainActivity, "USD", "2025-03-01 00:00:00.000", "2025-03-01 00:12:00.000")
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hola, $name")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ProyectoDivisaTheme {
        Greeting("WorkManager App")
    }
}

fun testContentProviderWithParams(context: Context, currency: String, startDate: String, endDate: String) {
    // URI para consultar por moneda y rango de fechas
    val uri = Uri.parse("content://com.example.proyectodivisa.provider/divisas_by_currency_and_date/$currency/$startDate/$endDate")

    // Columnas que quieres recuperar
    val projection = arrayOf(
        DivisasContract.DivisasColumns.ID,
        DivisasContract.DivisasColumns.DATE,
        DivisasContract.DivisasColumns.CURRENCY,
        DivisasContract.DivisasColumns.RATE
    )

    // Realiza la consulta usando ContentResolver
    val cursor: Cursor? = context.contentResolver.query(
        uri,
        projection,
        null, // No hay filtros adicionales (WHERE)
        null, // No hay argumentos para el filtro
        null  // No hay ordenamiento (ORDER BY)
    )

    // Verifica si el cursor tiene datos
    if (cursor != null) {
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DivisasContract.DivisasColumns.ID))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(DivisasContract.DivisasColumns.DATE))
            val currency = cursor.getString(cursor.getColumnIndexOrThrow(DivisasContract.DivisasColumns.CURRENCY))
            val rate = cursor.getDouble(cursor.getColumnIndexOrThrow(DivisasContract.DivisasColumns.RATE))

            // Imprime los datos en Logcat
            Log.d("ContentProviderTest", "ID: $id, Date: $date, Currency: $currency, Rate: $rate")
        }
        cursor.close()
    } else {
        Log.e("ContentProviderTest", "Error: El cursor es nulo")
    }
}