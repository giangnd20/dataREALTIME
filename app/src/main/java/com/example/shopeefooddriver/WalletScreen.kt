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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.shopeefooddriver.data.Transaction
import com.example.shopeefooddriver.data.Wallet
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WalletScreen() {
    val uid = FirebaseRepository.currentUid()
    val wallet by if (uid != null) {
        FirebaseRepository.observeWallet(uid).collectAsState(initial = null)
    } else {
        remember { mutableStateOf<Wallet?>(null) }
    }
    val transactions by if (uid != null) {
        FirebaseRepository.observeTransactions(uid).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Transaction>()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Ví của tôi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (wallet == null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFEE4D2D))
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Số dư khả dụng", color = Color.Gray)
                    Text(
                        formatMoney(wallet!!.balance),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tiền ký quỹ: ${formatMoney(wallet!!.deposit)}", fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* Nạp tiền - demo, có thể mở dialog sau */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A651))
            ) {
                Text("Nạp tiền")
            }
            OutlinedButton(
                onClick = { /* Rút tiền - demo */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Rút tiền")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Giao dịch gần đây", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Text("Chưa có giao dịch", color = Color.Gray, fontSize = 14.sp)
        } else {
            transactions.forEach { tx ->
                val amountStr = if (tx.amount >= 0) "+${formatMoney(tx.amount)}" else formatMoney(tx.amount)
                TransactionItem(tx.title, amountStr, tx.date)
            }
        }
    }
}

@Composable
fun TransactionItem(title: String, amount: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title)
            Text(date, fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            amount,
            fontWeight = FontWeight.Bold,
            color = if (amount.startsWith("+")) Color(0xFF00A651) else Color.Red
        )
    }
    HorizontalDivider()
}

private fun formatMoney(amount: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}
