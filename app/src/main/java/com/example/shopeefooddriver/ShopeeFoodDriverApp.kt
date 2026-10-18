package com.example.shopeefooddriver

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ShopeeFoodDriverApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase with the provided config (works even without google-services processing edge cases)
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBfb2pNqpzTQSjky-O14HAV_lPcwTGpIX4")
                .setApplicationId("1:900670400298:android:2fdbd0c924228ebc8a6053")
                .setDatabaseUrl("https://food6965p-default-rtdb.asia-southeast1.firebasedatabase.app")
                .setProjectId("food6965p")
                .setStorageBucket("food6965p.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
    }
}
