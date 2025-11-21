package com.example.sbooks.utils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    private val defaultFormat = SimpleDateFormat(Constants.DATE_FORMAT_DEFAULT, Locale.getDefault())
    private val displayFormat = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())
    private val simpleFormat = SimpleDateFormat(Constants.DATE_FORMAT_SIMPLE, Locale.getDefault())

    fun getCurrentDateTime(): String {
        return defaultFormat.format(Date())
    }

    fun getCurrentDate(): String {
        return simpleFormat.format(Date())
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = defaultFormat.parse(dateString)
            date?.let { displayFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateSimple(dateString: String): String {
        return try {
            val date = defaultFormat.parse(dateString)
            date?.let { simpleFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun getTimeAgo(dateString: String): String {
        return try {
            val date = defaultFormat.parse(dateString)
            date?.let {
                val now = Date()
                val diff = now.time - it.time

                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24

                when {
                    days > 0 -> "${days} ngày trước"
                    hours > 0 -> "${hours} giờ trước"
                    minutes > 0 -> "${minutes} phút trước"
                    else -> "Vừa xong"
                }
            } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun isToday(dateString: String): Boolean {
        return try {
            val date = defaultFormat.parse(dateString)
            date?.let {
                val today = Calendar.getInstance()
                val dateCalendar = Calendar.getInstance().apply { time = it }

                today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                        today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun isThisWeek(dateString: String): Boolean {
        return try {
            val date = defaultFormat.parse(dateString)
            date?.let {
                val today = Calendar.getInstance()
                val dateCalendar = Calendar.getInstance().apply { time = it }

                today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                        today.get(Calendar.WEEK_OF_YEAR) == dateCalendar.get(Calendar.WEEK_OF_YEAR)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun isThisMonth(dateString: String): Boolean {
        return try {
            val date = defaultFormat.parse(dateString)
            date?.let {
                val today = Calendar.getInstance()
                val dateCalendar = Calendar.getInstance().apply { time = it }

                today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun getDateRange(days: Int): Pair<String, String> {
        val endDate = Calendar.getInstance()
        val startDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -days)
        }

        return Pair(
            simpleFormat.format(startDate.time),
            simpleFormat.format(endDate.time)
        )
    }

    fun parseDate(dateString: String, format: String): Date? {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(date: Date, format: String): String {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).format(date)
        } catch (e: Exception) {
            ""
        }
    }
    fun getRelativeTime(dateString: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString

            val now = Calendar.getInstance().time
            val diff = now.time - date.time

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                seconds < 60 -> "Vừa xong"
                minutes < 60 -> "$minutes phút trước"
                hours < 24 -> "$hours giờ trước"
                days < 7 -> "$days ngày trước"
                days < 30 -> "${days / 7} tuần trước"
                days < 365 -> "${days / 30} tháng trước"
                else -> "${days / 365} năm trước"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return dateString
        }
    }
}