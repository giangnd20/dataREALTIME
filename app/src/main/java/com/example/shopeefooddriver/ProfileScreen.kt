package com.example.shopeefooddriver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.emptyFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeefooddriver.data.Driver
import com.example.shopeefooddriver.data.FirebaseRepository

@Composable
fun ProfileScreen(onLogout: () -> Unit = {}) {
    val uid = FirebaseRepository.currentUid()
    val flow = remember(uid) {
        if (uid != null) FirebaseRepository.observeDriver(uid) else emptyFlow<Driver?>()
    }
    val driver by flow.collectAsStateWithLifecycle<Driver?, Driver?>(null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (driver == null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFEE4D2D))
            }
        } else {
            val d = driver!!
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEE4D2D),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            d.name.take(2).uppercase().ifBlank { "TX" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        d.name.ifBlank { "Tài xế" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Mã NV: ${d.employeeCode}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        if (d.isOnline) "Đang online" else "Offline",
                        color = if (d.isOnline) Color(0xFF00A651) else Color.Gray,
                        fontSize = 13.sp
                    )
                    if (d.email.isNotBlank()) {
                        Text(d.email, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuItem(
            title = "Điểm đến của tôi",
            subtitle = "Chọn điểm đến trong bán kính 1km để nhận đơn phù hợp"
        )
        ProfileMenuItem(
            title = "Trung tâm hỗ trợ",
            subtitle = "Hướng dẫn xử lý sự cố trong quá trình giao hàng"
        )
        ProfileMenuItem(
            title = "Chương trình tài xế",
            subtitle = "Xem các chương trình đang tham gia"
        )
        ProfileMenuItem(
            title = "Thông tin hợp đồng",
            subtitle = "Xem hợp đồng đối tác với ShopeeFood"
        )
        ProfileMenuItem(
            title = "Cài đặt",
            subtitle = "Thông báo, ngôn ngữ, bảo mật..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Đăng xuất")
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = { /* Demo - chưa có action */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        }
    }
}
