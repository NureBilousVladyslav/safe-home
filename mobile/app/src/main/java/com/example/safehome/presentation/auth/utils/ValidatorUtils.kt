package com.example.safehome.presentation.auth.utils

import com.example.safehome.data.model.DateModel
import java.util.Calendar

object ValidatorUtils {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isPasswordConfirmed(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isNotBlank(vararg values: String): Boolean {
        return values.all { it.isNotBlank() }
    }

    fun isDateSelected(dateModel: DateModel?): Boolean {
        return dateModel != null
    }

    fun isUserAtLeast18(dateModel: DateModel): Boolean {
        val birthCalendar = Calendar.getInstance().apply {
            set(dateModel.year, dateModel.month, dateModel.day)
        }
        val currentCalendar = Calendar.getInstance()

        var age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

        if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age >= 18
    }
}
