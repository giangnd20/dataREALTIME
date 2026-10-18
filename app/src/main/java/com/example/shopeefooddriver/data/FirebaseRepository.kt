package com.example.shopeefooddriver.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Nguyên tắc để chạy nhanh với hàng chục nghìn bản ghi:
 *  1. KHÔNG BAO GIỜ lắng nghe/đọc cả một node lớn -> luôn dùng query + limit.
 *  2. Lọc ở server (orderByChild + equalTo), không lọc ở client.
 *  3. Parse JSON -> object ở luồng nền, UI chỉ nhận danh sách nhỏ.
 *  4. Phân trang bằng cách tăng limit (PAGE_SIZE mỗi lần "Xem thêm").
 *
 * Lưu ý: orderByKey() chỉ cho thứ tự "mới nhất" đúng khi key được tạo bằng push().key
 * (Firebase push ID tăng dần theo thời gian) và không cần khai báo index.
 */
object FirebaseRepository {

    const val PAGE_SIZE = 30
    const val ORDERS_LIMIT = 50

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

    /**
     * Chỉ tạo hồ sơ tài xế (nếu chưa có). Ví, giao dịch, thu nhập, thông báo và đơn hàng
     * do server/admin tạo – rules không cho client tự ghi (tránh tự cộng tiền vào ví).
     */
    suspend fun ensureDriverProfile(uid: String, email: String) {
        val ref = db.child("drivers").child(uid)
        if (!ref.get().await().exists()) {
            val driver = Driver(
                uid = uid,
                name = email.substringBefore("@").ifBlank { "Tài xế" },
                employeeCode = "SF-" + uid.take(6).uppercase(),
                email = email
            )
            ref.setValue(driver).await()
        }
    }

    // ---------- helpers ----------

    private fun Query.snapshots(): Flow<DataSnapshot> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        addValueEventListener(listener)
        awaitClose { removeEventListener(listener) }
    }.conflate() // nếu UI chậm, bỏ các snapshot trung gian

    /** Parse ở luồng nền để không làm giật main thread. */
    private fun <T> Query.observe(parse: (DataSnapshot) -> T): Flow<T> =
        snapshots()
            .map(parse)
            .flowOn(Dispatchers.Default)
            .catch { Log.w("FirebaseRepository", "Listener lỗi: ${it.message}") }

    private inline fun <reified T : Any> DataSnapshot.parseChildren(
        crossinline withKey: (T, String) -> T
    ): List<T> = children.mapNotNull { child ->
        val key = child.key ?: return@mapNotNull null
        child.getValue(T::class.java)?.let { withKey(it, key) }
    }

    // ---------- observers ----------

    fun observeDriver(uid: String): Flow<Driver?> =
        db.child("drivers").child(uid).observe { it.getValue(Driver::class.java) }

    /** Chỉ lấy tối đa ORDERS_LIMIT đơn đang chờ, lọc tại server. Cần .indexOn "status". */
    fun observeOrders(): Flow<List<Order>> =
        db.child("orders")
            .orderByChild("status")
            .equalTo("pending")
            .limitToLast(ORDERS_LIMIT)
            .observe { snap ->
                snap.parseChildren<Order> { o, key -> o.copy(id = key) }
                    .sortedByDescending { it.createdAt }
            }

    fun observeWallet(uid: String): Flow<Wallet> =
        db.child("wallets").child(uid)
            .observe { it.getValue(Wallet::class.java) ?: Wallet() }

    /** Chỉ lấy [limit] giao dịch mới nhất. */
    fun observeTransactions(uid: String, limit: Int = PAGE_SIZE): Flow<List<Transaction>> =
        db.child("transactions").child(uid)
            .orderByKey()
            .limitToLast(limit)
            .observe { snap ->
                snap.parseChildren<Transaction> { t, key -> t.copy(id = key) }
                    .sortedWith(
                        compareByDescending<Transaction> { it.createdAt }
                            .thenByDescending { it.id }
                    )
            }

    fun observeIncome(uid: String): Flow<IncomeSummary> =
        db.child("income").child(uid)
            .observe { it.getValue(IncomeSummary::class.java) ?: IncomeSummary() }

    /** Chỉ lấy [limit] thông báo mới nhất. */
    fun observeNotifications(uid: String, limit: Int = PAGE_SIZE): Flow<List<NotificationItem>> =
        db.child("notifications").child(uid)
            .orderByKey()
            .limitToLast(limit)
            .observe { snap ->
                snap.parseChildren<NotificationItem> { n, key -> n.copy(id = key) }
                    .sortedByDescending { it.timestamp }
            }

    // ---------- writes ----------

    /**
     * Ghi status + driverId trong MỘT lần updateChildren (atomic).
     * Rules chỉ cho phép khi đơn còn "pending", nên nếu tài xế khác nhận trước
     * thì lệnh này sẽ bị từ chối (PERMISSION_DENIED) thay vì ghi đè.
     */
    suspend fun acceptOrder(orderId: String, uid: String) {
        db.child("orders").child(orderId)
            .updateChildren(mapOf("status" to "accepted", "driverId" to uid))
            .await()
    }

    suspend fun setOnlineStatus(uid: String, online: Boolean) {
        db.child("drivers").child(uid).child("isOnline").setValue(online).await()
    }
}
