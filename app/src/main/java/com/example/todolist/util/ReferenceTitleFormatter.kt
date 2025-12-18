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
        StatsGranularity.DAY -> {
            // Single day - show full date: "18 December 2024"
            val month = ref.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            "${ref.dayOfMonth} $month ${ref.year}"
        }
        StatsGranularity.DAY_OF_WEEK -> {
            val start = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            val startMonth = start.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val endMonth = end.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            if (start.month == end.month) {
                "${start.dayOfMonth} - ${end.dayOfMonth} $startMonth ${start.year}"
            } else {
                "${start.dayOfMonth} $startMonth - ${end.dayOfMonth} $endMonth ${start.year}"
            }
        }
        StatsGranularity.WEEK_OF_MONTH -> {
            val month = ref.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            "$month ${ref.year}"
        }
        StatsGranularity.MONTH_OF_YEAR -> {
            ref.year.toString()
        }
        StatsGranularity.YEAR -> {
            // Multi-year view - show decade or range
            "${ref.year - 2} - ${ref.year + 2}"
        }
    }
}

