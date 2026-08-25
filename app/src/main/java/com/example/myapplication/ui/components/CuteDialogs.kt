package com.example.myapplication.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CuteInfoDialog(title: String, content: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White,
        title = { Text(title, fontWeight = FontWeight.Black, color = Color(0xFF1E1B4B)) },
        text = { Text(content, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understood 🌸", fontWeight = FontWeight.Bold, color = Color(0xFF7E57C2))
            }
        }
    )
}
