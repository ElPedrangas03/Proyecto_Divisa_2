package com.example.proyectodivisa.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyectodivisa.model.ExchangeRate
import com.example.proyectodivisa.provider.DivisasRepository
import kotlinx.coroutines.launch

class DivisasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DivisasRepository(application)

    private val _exchangeRates = MutableLiveData<List<ExchangeRate>>()
    val exchangeRates: LiveData<List<ExchangeRate>> get() = _exchangeRates

    fun fetchExchangeRates(currency: String, change: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            val rates = repository.fetchExchangeRates(currency, change, startDate, endDate)
            _exchangeRates.value = rates
        }
    }
}