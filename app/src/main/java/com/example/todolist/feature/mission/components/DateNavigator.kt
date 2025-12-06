package com.example.todolist.feature.mission.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.domain.usecase.StatsGranularity
import java.time.LocalDate
import com.example.todolist.util.formatReferenceTitle

@Composable
fun DateNavigator(
    referenceDate: LocalDate,
    granularity: StatsGranularity,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSetGranularity: (StatsGranularity) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(modifier = modifier.fillMaxWidth()) {
        // Header với navigation buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.08f),
                                secondaryColor.copy(alpha = 0.04f),
                                primaryColor.copy(alpha = 0.08f)
                            )
                        )
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = formatReferenceTitle(referenceDate, granularity),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = primaryColor
                    )

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Granularity selector chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = granularity == StatsGranularity.DAY_OF_WEEK,
                onClick = { onSetGranularity(StatsGranularity.DAY_OF_WEEK) },
                label = { Text("📅 Day") },
                modifier = Modifier.weight(1f),
                leadingIcon = if (granularity == StatsGranularity.DAY_OF_WEEK) {
                    { Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            FilterChip(
                selected = granularity == StatsGranularity.WEEK_OF_MONTH,
                onClick = { onSetGranularity(StatsGranularity.WEEK_OF_MONTH) },
                label = { Text("📆 Week") },
                modifier = Modifier.weight(1f),
                leadingIcon = if (granularity == StatsGranularity.WEEK_OF_MONTH) {
                    { Icon(imageVector = Icons.Filled.ViewWeek, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            FilterChip(
                selected = granularity == StatsGranularity.MONTH_OF_YEAR,
                onClick = { onSetGranularity(StatsGranularity.MONTH_OF_YEAR) },
                label = { Text("🗓️ Month") },
                modifier = Modifier.weight(1f),
                leadingIcon = if (granularity == StatsGranularity.MONTH_OF_YEAR) {
                    { Icon(imageVector = Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
