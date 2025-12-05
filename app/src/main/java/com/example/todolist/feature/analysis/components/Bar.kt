package com.example.todolist.feature.analysis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

@Composable
fun Bar(color: Color, contentColor: Color, heightDp: Dp, label: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(18.dp)) {
        // Show number inside bar if tall enough, otherwise show below
        Box(modifier = Modifier
            .height(heightDp)
            .width(18.dp)
            .background(color, shape = RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            if (label > 0 && heightDp >= 24.dp) {
                Text(text = label.toString(), color = contentColor, fontSize = 10.sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (label > 0 && heightDp < 24.dp) {
            Text(text = label.toString(), fontSize = 10.sp)
        }
    }
}

