package com.example.todolist.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun WeekDaysRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        for (d in daysOfWeek) {
            val textColor =  MaterialTheme.colorScheme.onSurface
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = d, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, color = textColor)
            }
        }
    }
}
