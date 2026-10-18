package com.example.shopeefooddriver

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.example.shopeefooddriver.data.FirebaseRepository
import com.example.shopeefooddriver.data.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen() {
    // remember: nếu không, mỗi lần recompose sẽ tạo Flow mới -> hủy & đăng ký lại listener (tải lại dữ liệu)
    val ordersFlow = remember { FirebaseRepository.observeOrders() }
    val orders by ordersFlow.collectAsStateWithLifecycle<List<Order>, List<Order>?>(null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var acceptingId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Đơn hàng đang chờ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = orders?.let {
                val count = if (it.size >= FirebaseRepository.ORDERS_LIMIT) "${it.size}+" else "${it.size}"
                "Bạn đang online • Có $count đơn gần bạn"
            } ?: "Bạn đang online",
            color = Color(0xFF00A651)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val list = orders
        if (list == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFEE4D2D))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Đang tải đơn hàng...", color = Color.Gray)
                }
            }
        } else if (list.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Hiện chưa có đơn nào đang chờ", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(list, key = { it.id }, contentType = { "order" }) { order ->
                    OrderCard(
                        order = order,
                        isAccepting = acceptingId == order.id,
                        onAccept = {
                            val uid = FirebaseRepository.currentUid() ?: return@OrderCard
                            acceptingId = order.id
                            scope.launch {
                                try {
                                    FirebaseRepository.acceptOrder(order.id, uid)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    // Thường do đơn đã có tài xế khác nhận (rules từ chối) hoặc mất mạng
                                    Toast.makeText(
                                        context,
                                        "Không nhận được đơn – có thể đã có người nhận trước.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    acceptingId = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    isAccepting: Boolean = false,
    onAccept: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = order.fee,
                    color = Color(0xFFEE4D2D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color(0xFFEE4D2D)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(order.restaurant, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Lấy: ${order.pickupAddress}", fontSize = 13.sp)
                    Text("Giao: ${order.deliveryAddress}", fontSize = 13.sp)
                    Text(
                        "Khoảng cách: ${order.distance} • COD: ${order.cod}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAccepting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE4D2D))
            ) {
                if (isAccepting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Nhận đơn")
                }
            }
        }
    }
}
