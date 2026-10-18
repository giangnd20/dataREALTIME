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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopeefooddriver.data.FirebaseRepository
import com.example.shopeefooddriver.data.Transaction
import com.example.shopeefooddriver.data.Wallet
import kotlinx.coroutines.flow.emptyFlow
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WalletScreen() {
    val uid = FirebaseRepository.currentUid()
    // Số giao dịch đang hiển thị; bấm "Xem thêm" để tăng thêm PAGE_SIZE
    var limit by rememberSaveable { mutableIntStateOf(FirebaseRepository.PAGE_SIZE) }

    val walletFlow = remember(uid) {
        if (uid != null) FirebaseRepository.observeWallet(uid) else emptyFlow()
    }
    // Flow<Wallet> → initialValue Wallet (không null)
    val wallet by walletFlow.collectAsStateWithLifecycle(initialValue = Wallet())

    val txFlow = remember(uid, limit) {
        if (uid != null) FirebaseRepository.observeTransactions(uid, limit) else emptyFlow()
    }
    // Flow<List<Transaction>> → initialValue emptyList
    val transactions by txFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val w = wallet
    val txs = transactions

    // LazyColumn cho cả màn hình: chỉ compose các dòng đang nhìn thấy, và cuộn được
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                "Ví của tôi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Số dư khả dụng", color = Color.Gray)
                    Text(
                        formatMoney(w.balance),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tiền ký quỹ: ${formatMoney(w.deposit)}", fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Nạp tiền - demo */ },
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
        }

        when {
            txs.isEmpty() -> item {
                Text("Chưa có giao dịch", color = Color.Gray, fontSize = 14.sp)
            }

            else -> {
                items(txs, key = { it.id }, contentType = { "tx" }) { tx ->
                    val amountStr =
                        if (tx.amount >= 0) "+${formatMoney(tx.amount)}" else formatMoney(tx.amount)
                    TransactionItem(tx.title, amountStr, tx.date)
                }
                if (txs.size >= limit) {
                    item {
                        OutlinedButton(
                            onClick = { limit += FirebaseRepository.PAGE_SIZE },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Text("Xem thêm")
                        }
                    }
                }
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

// Tạo 1 lần, dùng lại cho mọi dòng (tránh khởi tạo NumberFormat cho từng item)
private val moneyFormatter: NumberFormat = NumberFormat.getInstance(Locale("vi", "VN"))

private fun formatMoney(amount: Long): String = "${moneyFormatter.format(amount)}đ"
