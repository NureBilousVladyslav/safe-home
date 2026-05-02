package com.example.safehome.presentation.common.viewModel

import androidx.lifecycle.ViewModel
import com.example.safehome.data.model.DateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DatePickerViewModel  : ViewModel() {
    private val _selectedDate = MutableStateFlow<DateModel?>(null)
    val selectedDate: StateFlow<DateModel?> get() = _selectedDate

    fun setDate(month: Int, day: Int, year: Int) {
        _selectedDate.value = DateModel(month, day, year)
    }
}