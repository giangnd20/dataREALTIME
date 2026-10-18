package com.example.shopeefooddriver

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase

class ShopeeFoodDriverApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase with the provided config (works even without google-services processing edge cases)
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBfb2pNqpzTQSjky-O14HAV_lPcwTGpIX4")
                .setApplicationId("1:900670400298:android:2fdbd0c924228ebc8a6053")
                .setDatabaseUrl(DB_URL)
                .setProjectId("food6965p")
                .setStorageBucket("food6965p.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(this, options)
        }

        // Cache offline: mở app/chuyển tab hiển thị ngay từ bộ nhớ máy, sau đó mới đồng bộ.
        // PHẢI gọi trước mọi truy cập FirebaseDatabase khác (FirebaseRepository khởi tạo lazy nên an toàn).
        try {
            FirebaseDatabase.getInstance(DB_URL).apply {
                setPersistenceEnabled(true)
                setPersistenceCacheSizeBytes(50L * 1024 * 1024) // 50 MB
            }
        } catch (_: Exception) {
            // Đã được gọi trước đó – bỏ qua
        }
    }

    private companion object {
        const val DB_URL = "https://food6965p-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}
