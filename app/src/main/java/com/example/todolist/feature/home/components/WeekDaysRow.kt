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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.todolist.R

@Composable
fun WeekDaysRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val daysOfWeek = listOf(
            stringResource(R.string.monday),
            stringResource(R.string.tuesday),
            stringResource(R.string.wednesday),
            stringResource(R.string.thursday),
            stringResource(R.string.friday),
            stringResource(R.string.saturday),
            stringResource(R.string.sunday)
        )
        for (d in daysOfWeek) {
            val textColor = MaterialTheme.colorScheme.onSurface
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = d, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, color = textColor)
            }
        }
    }
}
