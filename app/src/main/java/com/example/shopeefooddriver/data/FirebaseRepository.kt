package com.example.shopeefooddriver.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance(
        "https://food6965p-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    fun currentUid(): String? = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    /** Seed demo data for a newly logged-in driver (only if not exists). */
    suspend fun ensureDriverProfile(uid: String, email: String) {
        val snapshot = db.child("drivers").child(uid).get().await()
        if (!snapshot.exists()) {
            val driver = Driver(
                uid = uid,
                name = "Nguyễn Văn Tài",
                employeeCode = "SF-DRIVER-88421",
                phone = "0901234567",
                email = email,
                isOnline = true,
                rating = 4.9,
                totalOrders = 1280
            )
            db.child("drivers").child(uid).setValue(driver).await()

            // Wallet
            val wallet = Wallet(balance = 850_000L, deposit = 500_000L)
            db.child("wallets").child(uid).setValue(wallet).await()

            // Sample transactions
            val txs = listOf(
                Transaction("tx1", "Rút về Vietcombank", -300_000L, "18/10/2026", "withdraw"),
                Transaction("tx2", "Thưởng tuần", 150_000L, "17/10/2026", "bonus"),
                Transaction("tx3", "Thu nhập đơn SF240917", 45_000L, "17/10/2026", "income"),
                Transaction("tx4", "Thu nhập đơn SF240916", 38_000L, "16/10/2026", "income")
            )
            txs.forEach { db.child("transactions").child(uid).child(it.id).setValue(it).await() }

            // Income summary
            val income = IncomeSummary(
                weekTotal = 1_285_000L,
                orderCount = 42,
                bonus = 150_000L,
                days = listOf(
                    IncomeDay("Thứ 2", 8, 185_000L),
                    IncomeDay("Thứ 3", 11, 265_000L),
                    IncomeDay("Thứ 4", 9, 210_000L),
                    IncomeDay("Thứ 5", 7, 175_000L),
                    IncomeDay("Thứ 6", 7, 450_000L, "có thưởng")
                )
            )
            db.child("income").child(uid).setValue(income).await()

            // Notifications
            val notifs = listOf(
                NotificationItem("n1", "Bạn vừa nhận được đơn mới từ Cơm tấm Cali - Nguyễn Trãi", System.currentTimeMillis(), false),
                NotificationItem("n2", "Khách hàng đã xác nhận nhận hàng - Đơn SF240917045", System.currentTimeMillis() - 3600000, false),
                NotificationItem("n3", "Thưởng tuần này đã được cộng vào ví (+150.000đ)", System.currentTimeMillis() - 7200000, false),
                NotificationItem("n4", "Cập nhật chính sách mới về phí giao hàng từ 20/10/2026", System.currentTimeMillis() - 86400000, true),
                NotificationItem("n5", "Hệ thống sẽ bảo trì từ 02:00 - 04:00 ngày 19/10", System.currentTimeMillis() - 90000000, true),
                NotificationItem("n6", "Bạn đã hoàn thành 40 đơn tuần này - Sắp nhận thưởng mốc!", System.currentTimeMillis() - 100000000, false)
            )
            notifs.forEach { db.child("notifications").child(uid).child(it.id).setValue(it).await() }
        }

        // Always ensure there are available orders (shared pool)
        val ordersSnap = db.child("orders").get().await()
        if (!ordersSnap.exists() || ordersSnap.childrenCount < 2) {
            val orders = listOf(
                Order("SF240918001", "Cơm tấm Cali - Nguyễn Trãi", "123 Nguyễn Trãi, Q.1", "45 Lê Lợi, Q.1", "2.1 km", "35.000đ", "0đ", "pending", System.currentTimeMillis()),
                Order("SF240918002", "Phở 24 - Trần Hưng Đạo", "56 Trần Hưng Đạo, Q.1", "12 Nguyễn Huệ, Q.1", "1.8 km", "28.000đ", "85.000đ", "pending", System.currentTimeMillis()),
                Order("SF240918003", "Trà sữa Gong Cha", "78 Hai Bà Trưng, Q.1", "99 Đồng Khởi, Q.1", "1.2 km", "22.000đ", "0đ", "pending", System.currentTimeMillis()),
                Order("SF240918004", "Bún chả Hà Nội", "22 Pasteur, Q.1", "15 Nam Kỳ Khởi Nghĩa, Q.3", "3.4 km", "42.000đ", "120.000đ", "pending", System.currentTimeMillis())
            )
            orders.forEach { db.child("orders").child(it.id).setValue(it).await() }
        }
    }

    fun observeDriver(uid: String): Flow<Driver?> = callbackFlow {
        val ref = db.child("drivers").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Driver::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeOrders(): Flow<List<Order>> = callbackFlow {
        val ref = db.child("orders")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                    .filter { it.status == "pending" }
                    .sortedByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun acceptOrder(orderId: String, uid: String) {
        db.child("orders").child(orderId).child("status").setValue("accepted").await()
        db.child("orders").child(orderId).child("driverId").setValue(uid).await()
    }

    fun observeWallet(uid: String): Flow<Wallet?> = callbackFlow {
        val ref = db.child("wallets").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Wallet::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeTransactions(uid: String): Flow<List<Transaction>> = callbackFlow {
        val ref = db.child("transactions").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeIncome(uid: String): Flow<IncomeSummary?> = callbackFlow {
        val ref = db.child("income").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(IncomeSummary::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeNotifications(uid: String): Flow<List<NotificationItem>> = callbackFlow {
        val ref = db.child("notifications").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(NotificationItem::class.java) }
                    .sortedByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun setOnlineStatus(uid: String, online: Boolean) {
        db.child("drivers").child(uid).child("isOnline").setValue(online).await()
    }
}
