package com.example.proyectodivisa

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerUtils {
    fun scheduleHourlyTask(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<MyWorker>(
            1, TimeUnit.HOURS // Intervalo de 1 hora
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "HourlyTask",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}