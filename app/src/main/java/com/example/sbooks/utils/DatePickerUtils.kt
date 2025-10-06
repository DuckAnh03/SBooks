package com.example.sbooks.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.*

class DatePickerUtils {

    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        /**
         * Show date picker dialog
         */
        fun showDatePicker(
            context: Context,
            button: Button,
            initialDate: Calendar? = null,
            onDateSelected: (Calendar) -> Unit
        ) {
            val calendar = initialDate ?: Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                context,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, month)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // Update button text
                    button.text = displayFormat.format(selectedDate.time)

                    // Callback
                    onDateSelected(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Optional: Set min/max dates
            // datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            datePickerDialog.show()
        }

        /**
         * Format date for display
         */
        fun formatDate(calendar: Calendar): String {
            return displayFormat.format(calendar.time)
        }

        /**
         * Parse date string to Calendar
         */
        fun parseDate(dateString: String): Calendar? {
            return try {
                val date = dateFormat.parse(dateString)
                val calendar = Calendar.getInstance()
                if (date != null) {
                    calendar.time = date
                }
                calendar
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Get start of day
         */
        fun getStartOfDay(calendar: Calendar): Calendar {
            val result = calendar.clone() as Calendar
            result.set(Calendar.HOUR_OF_DAY, 0)
            result.set(Calendar.MINUTE, 0)
            result.set(Calendar.SECOND, 0)
            result.set(Calendar.MILLISECOND, 0)
            return result
        }

        /**
         * Get end of day
         */
        fun getEndOfDay(calendar: Calendar): Calendar {
            val result = calendar.clone() as Calendar
            result.set(Calendar.HOUR_OF_DAY, 23)
            result.set(Calendar.MINUTE, 59)
            result.set(Calendar.SECOND, 59)
            result.set(Calendar.MILLISECOND, 999)
            return result
        }
    }
}