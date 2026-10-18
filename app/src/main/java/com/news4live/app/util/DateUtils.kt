package com.news4live.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    private val hindiMonths = arrayOf(
        "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
        "जुलाई", "अगस्त", "सितम्बर", "अक्टूबर", "नवम्बर", "दिसम्बर"
    )

    /**
     * Parses ISO-8601 string from WordPress into relative or formatted Hindi date
     * Examples: "5 मिनट पहले", "2 घंटे पहले", "कल", "8 सितम्बर 2026"
     */
    fun formatHindiDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""

        val date = parseIsoDate(isoString) ?: return isoString

        val now = System.currentTimeMillis()
        val timeDiff = now - date.time

        val seconds = timeDiff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "अभी-अभी"
            minutes < 60 -> "$minutes मिनट पहले"
            hours < 24 -> "$hours घंटे पहले"
            days == 1L -> "कल"
            days in 2..6 -> "$days दिन पहले"
            else -> formatAbsoluteHindiDate(date)
        }
    }

    fun formatAbsoluteHindiDate(date: Date): String {
        val cal = java.util.Calendar.getInstance().apply { time = date }
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val month = cal.get(java.util.Calendar.MONTH)
        val year = cal.get(java.util.Calendar.YEAR)
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = String.format(Locale.ENGLISH, "%02d", cal.get(java.util.Calendar.MINUTE))

        val amPm = if (hour < 12) "पूर्वाह्न" else "अपराह्न"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12

        val monthName = if (month in hindiMonths.indices) hindiMonths[month] else ""
        return "$day $monthName $year, $displayHour:$minute $amPm"
    }

    private fun parseIsoDate(isoString: String): Date? {
        return try {
            // Trim any milliseconds or timezone offsets if needed
            val cleaned = isoString.split(".")[0].replace("Z", "")
            isoFormat.parse(cleaned)
        } catch (e: Exception) {
            try {
                val fallbackFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                fallbackFormat.parse(isoString)
            } catch (e2: Exception) {
                null
            }
        }
    }
}
