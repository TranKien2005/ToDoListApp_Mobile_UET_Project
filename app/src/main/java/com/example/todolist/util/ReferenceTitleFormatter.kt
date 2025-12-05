package com.example.todolist.util

import com.example.todolist.domain.usecase.StatsGranularity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// Shared helper to format the top title (used by Mission and Analysis screens)
fun formatReferenceTitle(ref: LocalDate, gran: StatsGranularity): String {
    return when (gran) {
        StatsGranularity.DAY_OF_WEEK -> {
            val start = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            val startMonth = start.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            val endMonth = end.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

            if (start.year == end.year) {
                if (start.month == end.month) {
                    "$startMonth ${start.year}"
                } else {
                    "$startMonth - $endMonth, ${start.year}"
                }
            } else {
                "$startMonth ${start.year} - $endMonth ${end.year}"
            }
        }
        StatsGranularity.WEEK_OF_MONTH -> {
            val month = ref.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            "$month ${ref.year}"
        }
        StatsGranularity.MONTH_OF_YEAR -> {
            ref.year.toString()
        }
    }
}

