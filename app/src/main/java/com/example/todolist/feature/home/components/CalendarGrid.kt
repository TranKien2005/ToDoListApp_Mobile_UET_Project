package com.example.todolist.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDow = firstOfMonth.dayOfWeek.value // Monday=1..Sunday=7
    val leadingEmpty = firstDow - 1
    val totalCells = leadingEmpty + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier) {
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val dayNumber = cellIndex - leadingEmpty + 1
                    val isInMonth = dayNumber in 1..daysInMonth
                    val cellDate = if (isInMonth) yearMonth.atDay(dayNumber) else if (dayNumber < 1) {
                        // previous month day
                        yearMonth.minusMonths(1).atEndOfMonth().plusDays(dayNumber.toLong())
                    } else {
                        // next month day
                        yearMonth.plusMonths(1).atDay(dayNumber - daysInMonth)
                    }

                    // For selected date use primaryContainer/onPrimaryContainer so it matches Color.kt containers
                    val isSelected = (cellDate == selectedDate)
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val textColor = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isInMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = true) { onDateSelected(cellDate) }
                            .background(bgColor),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = cellDate.dayOfMonth.toString(),
                            modifier = Modifier.padding(6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
