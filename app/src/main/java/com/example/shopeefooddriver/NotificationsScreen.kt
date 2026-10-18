package com.example.shopeefooddriver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeefooddriver.data.FirebaseRepository
import com.example.shopeefooddriver.data.NotificationItem

@Composable
fun NotificationsScreen() {
    val uid = FirebaseRepository.currentUid()
    val notifications by if (uid != null) {
        FirebaseRepository.observeNotifications(uid).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<NotificationItem>()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Thông báo & Tin nhắn",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFEE4D2D))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Đang tải thông báo...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.read) Color(0xFFF5F5F5) else Color.White
                        )
                    ) {
                        Text(
                            text = item.message,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            fontWeight = if (item.read) FontWeight.Normal else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
