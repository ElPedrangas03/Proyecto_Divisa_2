package com.example.proyectodivisa

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.proyectodivisa.model.ExchangeRate
import com.example.proyectodivisa.provider.DivisasContract
import com.example.proyectodivisa.room.AppDatabase
import com.example.proyectodivisa.ui.theme.ProyectoDivisaTheme
import com.example.proyectodivisa.viewmodel.DivisasViewModel
import com.example.proyectodivisa.workmanager.WorkManagerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate

class MainActivity : ComponentActivity(), OnChartValueSelectedListener {

    private val viewModel: DivisasViewModel by viewModels()
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Crear el LineChart programáticamente
        lineChart = LineChart(this)
        lineChart.layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        // Establecer el LineChart como la vista principal de la actividad
        setContentView(lineChart)

        // Configurar el listener para manejar clics en la gráfica
        lineChart.setOnChartValueSelectedListener(this)

        // Obtén los datos del ViewModel
        viewModel.fetchExchangeRates("MXN", "USD", "2025-03-01", "2025-03-20")

        // Observa los cambios en los datos
        viewModel.exchangeRates.observe(this, { exchangeRates ->
            Log.d("MainActivity", "Datos recibidos: ${exchangeRates.size} registros")
            if (exchangeRates.isNotEmpty()) {
                updateChart(exchangeRates, "MXN", "USD")
            } else {
                Log.e("MainActivity", "No hay datos para mostrar en la gráfica")
            }
        })

        GlobalScope.launch {
            val database = AppDatabase.getDatabase(this@MainActivity)
            val exchangeRates = database.exchangeRateDao().getExchangeRatesByDate(System.currentTimeMillis())
            exchangeRates.forEach {
                Log.d("Database", "${it.currency}: ${it.rate}")
            }
        }

        // Programa la tarea cada hora
        WorkManagerUtils.scheduleHourlyTask(this)

        // Prueba el ContentProvider con parámetros
        //lifecycleScope.launch(Dispatchers.IO) {
        //    testContentProviderWithParams(this@MainActivity, "MXN", "2025-03-01 00:00:00.000", "2025-03-01 00:12:00.000")
        //}
    }
    private fun updateChart(exchangeRates: List<ExchangeRate>, currency: String, change: String) {
        // Crear una lista de entradas para el gráfico de líneas
        val lineEntries = exchangeRates.mapIndexed { index, rate ->
            Entry(index.toFloat(), rate.rate.toFloat()) // Asegúrate de que rate.rate sea un Float
        }

        // Verificar que las entradas no estén vacías
        if (lineEntries.isEmpty()) {
            Log.e("MainActivity", "No hay entradas válidas para la gráfica")
            return
        }

        // Crear un LineDataSet
        val lineDataSet = LineDataSet(lineEntries, "Tipo de Cambio $change/$currency")
        lineDataSet.color = Color.BLUE
        lineDataSet.lineWidth = 2.5f
        lineDataSet.setCircleColor(Color.BLUE)
        lineDataSet.circleRadius = 4f
        lineDataSet.setDrawValues(false)

        // Configurar las líneas como cúbicas
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.cubicIntensity = 0.2f // Suavidad de la curva (0.1f a 1f)

        // Crear un LineData
        val lineData = LineData(lineDataSet)

        // Configurar el LineChart
        lineChart.data = lineData
        lineChart.description.isEnabled = false // Deshabilitar la descripción
        lineChart.setDrawGridBackground(false) // Deshabilitar el fondo de la cuadrícula
        lineChart.setTouchEnabled(true) // Habilitar interacción con la gráfica
        lineChart.isDragEnabled = true // Habilitar arrastre
        lineChart.setScaleEnabled(true) // Habilitar zoom
        lineChart.setPinchZoom(true) // Habilitar zoom con pellizco

        // Configurar el eje X
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        // Configurar el eje Y izquierdo
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f

        // Configurar el eje Y derecho
        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false // Deshabilitar el eje Y derecho

        // Actualizar la gráfica
        lineChart.invalidate()
        lineChart.animateX(1000) // Animación para que la gráfica se muestre suavemente
    }

    // Método llamado cuando se selecciona un valor en la gráfica
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null && h != null) {
            val index = h.x.toInt() // Obtener el índice del punto seleccionado

            // Verificar que el índice sea válido
            if (index >= 0 && index < (viewModel.exchangeRates.value?.size ?: 0)) {
                val rate = viewModel.exchangeRates.value?.get(index)
                val message = "Fecha: ${rate?.date}, Valor: ${rate?.rate}"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar un mensaje si el índice es inválido
                Toast.makeText(this, "Índice inválido: $index", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Mostrar un mensaje si no hay datos seleccionados
            Toast.makeText(this, "Nada seleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    // Método llamado cuando no hay ningún valor seleccionado
    override fun onNothingSelected() {
        Toast.makeText(this, "Nada seleccionado", Toast.LENGTH_SHORT).show()
    }
}


// Pa checar que si este funcionando el content provider
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