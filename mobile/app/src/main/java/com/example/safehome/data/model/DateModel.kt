package com.example.safehome.data.model

data class DateModel(
    val month: Int,
    val day: Int,
    val year: Int,
) {
    companion object {
        val DEFAULT = DateModel(month = 0, day = 1, year = 2000)
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    fun toFormattedString(): String = "${months[month]} $day $year"
}