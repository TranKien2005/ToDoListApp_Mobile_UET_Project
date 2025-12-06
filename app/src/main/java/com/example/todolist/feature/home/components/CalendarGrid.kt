package com.example.todolist.feature.home.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val firstOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDow = firstOfMonth.dayOfWeek.value // Monday=1..Sunday=7
    val leadingEmpty = firstDow - 1
    val totalCells = leadingEmpty + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp) // Giới hạn chiều cao tối đa
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.04f),
                        secondaryColor.copy(alpha = 0.02f)
                    )
                )
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp) // Giảm spacing
    ) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Giảm spacing
            ) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val dayNumber = cellIndex - leadingEmpty + 1
                    val isInMonth = dayNumber in 1..daysInMonth
                    val cellDate = if (isInMonth) {
                        yearMonth.atDay(dayNumber)
                    } else if (dayNumber < 1) {
                        yearMonth.minusMonths(1).atEndOfMonth().plusDays(dayNumber.toLong())
                    } else {
                        yearMonth.plusMonths(1).atDay(dayNumber - daysInMonth)
                    }

                    val isSelected = (cellDate == selectedDate)
                    val isToday = (cellDate == LocalDate.now())

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .heightIn(max = 42.dp) // Giới hạn chiều cao cell
                            .shadow(
                                elevation = if (isSelected) 8.dp else 0.dp,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor,
                                            secondaryColor
                                        )
                                    )
                                    isToday -> Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.2f),
                                            secondaryColor.copy(alpha = 0.1f)
                                        )
                                    )
                                    else -> Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            )
                            .clickable { onDateSelected(cellDate) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = cellDate.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp, // Giảm font size
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> primaryColor
                                    isInMonth -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )

                            // Today indicator dot
                            if (isToday && !isSelected) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Box(
                                    modifier = Modifier
                                        .size(3.dp) // Giảm size dot
                                        .clip(CircleShape)
                                        .background(primaryColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
