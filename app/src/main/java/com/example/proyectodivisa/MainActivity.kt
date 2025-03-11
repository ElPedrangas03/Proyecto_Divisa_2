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

class MainActivity : ComponentActivity(){

    private val viewModel: DivisasViewModel by viewModels()
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}