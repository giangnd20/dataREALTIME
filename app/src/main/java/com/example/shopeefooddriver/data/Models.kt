package com.example.shopeefooddriver.data

data class Driver(
    val uid: String = "",
    val name: String = "",
    val employeeCode: String = "",
    val phone: String = "",
    val email: String = "",
    val isOnline: Boolean = false,
    val rating: Double = 5.0,
    val totalOrders: Int = 0
)

data class Order(
    val id: String = "",
    val restaurant: String = "",
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val distance: String = "",
    val fee: String = "",
    val cod: String = "",
    val status: String = "pending", // pending, accepted, delivering, completed
    val createdAt: Long = 0L
)

data class Wallet(
    val balance: Long = 0L,       // VND
    val deposit: Long = 0L,       // ký quỹ
    val currency: String = "VND"
)

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Long = 0L,        // positive = income, negative = withdraw
    val date: String = "",
    val type: String = ""         // income, withdraw, bonus, deposit
)

data class IncomeDay(
    val day: String = "",
    val orders: Int = 0,
    val amount: Long = 0L,
    val note: String = ""
)

data class IncomeSummary(
    val weekTotal: Long = 0L,
    val orderCount: Int = 0,
    val bonus: Long = 0L,
    val days: List<IncomeDay> = emptyList()
)

data class NotificationItem(
    val id: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
)
