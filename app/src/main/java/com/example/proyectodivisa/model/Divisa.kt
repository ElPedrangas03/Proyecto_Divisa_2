package com.example.proyectodivisa.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "Divisas")
data class Divisa(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String = formatDate(System.currentTimeMillis()),
    val currency: String,
    val rate: Double
)

fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}