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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.emptyFlow
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
    var limit by rememberSaveable { mutableIntStateOf(FirebaseRepository.PAGE_SIZE) }
    val flow = remember(uid, limit) {
        if (uid != null) FirebaseRepository.observeNotifications(uid, limit) else emptyFlow()
    }
    // Flow<List<NotificationItem>> → initialValue emptyList (không null)
    val notifications by flow.collectAsStateWithLifecycle(initialValue = emptyList())

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
                Text("Chưa có thông báo", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications, key = { it.id }, contentType = { "notification" }) { item ->
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
                if (notifications.size >= limit) {
                    item {
                        OutlinedButton(
                            onClick = { limit += FirebaseRepository.PAGE_SIZE },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Xem thêm")
                        }
                    }
                }
            }
        }
    }
}
