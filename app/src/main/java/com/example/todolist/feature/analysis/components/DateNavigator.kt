package com.example.todolist.feature.analysis.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.usecase.StatsGranularity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun DateNavigator(
    referenceDate: LocalDate,
    granularity: StatsGranularity,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSetGranularity: (StatsGranularity) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        IconButton(onClick = onPrev) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(text = formatReferenceTitle(referenceDate, granularity), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onNext) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        FilterChip(
            selected = granularity == StatsGranularity.DAY_OF_WEEK,
            onClick = { onSetGranularity(StatsGranularity.DAY_OF_WEEK) },
            label = { Text("Day") },
            leadingIcon = { Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = "Day") },
            modifier = Modifier.padding(end = 8.dp)
        )

        FilterChip(
            selected = granularity == StatsGranularity.WEEK_OF_MONTH,
            onClick = { onSetGranularity(StatsGranularity.WEEK_OF_MONTH) },
            label = { Text("Week") },
            leadingIcon = { Icon(imageVector = Icons.Filled.ViewWeek, contentDescription = "Week") },
            modifier = Modifier.padding(end = 8.dp)
        )

        FilterChip(
            selected = granularity == StatsGranularity.MONTH_OF_YEAR,
            onClick = { onSetGranularity(StatsGranularity.MONTH_OF_YEAR) },
            label = { Text("Month") },
            leadingIcon = { Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Month") }
        )
    }
}

private fun formatReferenceTitle(ref: LocalDate, gran: StatsGranularity): String {
    return when (gran) {
        StatsGranularity.DAY -> {
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
        StatsGranularity.MONTH_OF_YEAR -> ref.year.toString()
        StatsGranularity.YEAR -> "${ref.year - 2} - ${ref.year + 2}"
    }
}

