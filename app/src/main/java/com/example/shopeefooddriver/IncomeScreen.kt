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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopeefooddriver.data.IncomeSummary
import kotlinx.coroutines.flow.emptyFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeefooddriver.data.FirebaseRepository
import java.text.NumberFormat
import java.util.Locale

@Composable
fun IncomeScreen() {
    val uid = FirebaseRepository.currentUid()
    val flow = remember(uid) {
        if (uid != null) FirebaseRepository.observeIncome(uid) else emptyFlow()
    }
    // Flow<IncomeSummary> → initialValue phải cùng kiểu (không null)
    val income by flow.collectAsStateWithLifecycle(initialValue = IncomeSummary())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Thu nhập tuần này",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEE4D2D))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Tổng thu nhập", color = Color.White)
                Text(
                    formatMoney(income.weekTotal),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${income.orderCount} đơn • Thưởng: ${formatMoney(income.bonus)}",
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Chi tiết theo ngày", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (income.days.isEmpty()) {
            Text("Chưa có dữ liệu thu nhập", color = Color.Gray, fontSize = 14.sp)
        } else {
            income.days.forEach { day ->
                IncomeItem(
                    day = day.day,
                    orders = "${day.orders} đơn",
                    amount = if (day.note.isNotBlank()) {
                        "${formatMoney(day.amount)} (${day.note})"
                    } else {
                        formatMoney(day.amount)
                    }
                )
            }
        }
    }
}

@Composable
fun IncomeItem(day: String, orders: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(day, fontWeight = FontWeight.Medium)
            Text(orders, fontSize = 13.sp, color = Color.Gray)
        }
        Text(amount, fontWeight = FontWeight.Bold, color = Color(0xFF00A651))
    }
    HorizontalDivider()
}

private val moneyFormatter: NumberFormat = NumberFormat.getInstance(Locale("vi", "VN"))

private fun formatMoney(amount: Long): String = "${moneyFormatter.format(amount)}đ"
