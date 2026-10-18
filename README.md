# ShopeeFood - Đối tác Tài xế (v2.0)

Ứng dụng quản lý dành cho tài xế ShopeeFood viết bằng **Kotlin + Jetpack Compose**, sử dụng **Firebase Realtime Database** làm cơ sở dữ liệu và **Firebase Authentication** cho đăng nhập.

## Tính năng mới (v2.0)

- **Đăng nhập** bằng Email / Mật khẩu (Firebase Auth)
  - **Không có chức năng đăng ký** – chỉ tài xế đã được cấp tài khoản mới đăng nhập được
  - Nút **Đăng xuất** trong màn hình Tôi
- **Firebase Realtime Database** lưu trữ chi tiết & chính xác:
  - Hồ sơ tài xế (`drivers/{uid}`)
  - Đơn hàng (`orders`)
  - Ví & ký quỹ (`wallets/{uid}`)
  - Lịch sử giao dịch (`transactions/{uid}`)
  - Thu nhập theo tuần (`income/{uid}`)
  - Thông báo (`notifications/{uid}`)
- Dữ liệu realtime: màn hình tự cập nhật khi DB thay đổi
- Nhận đơn sẽ cập nhật status trên Firebase

## Tài khoản demo

Bạn cần tạo tài khoản trong **Firebase Console** (Authentication → Users → Add user) vì app **không cho đăng ký**.

Ví dụ tạo sẵn:
- Email: `driver@shopeefood.demo`
- Password: `Driver@123`

Sau lần đăng nhập đầu tiên, app sẽ tự seed dữ liệu demo (hồ sơ, ví, đơn, thu nhập, thông báo) vào Realtime Database nếu chưa có.

## Cấu trúc Firebase

```
food6965p (Realtime Database)
├── drivers/{uid}
│   ├── name, employeeCode, phone, email, isOnline, rating, totalOrders
├── wallets/{uid}
│   ├── balance, deposit
├── transactions/{uid}/{txId}
├── income/{uid}
│   ├── weekTotal, orderCount, bonus, days[]
├── notifications/{uid}/{nId}
└── orders/{orderId}
    ├── restaurant, pickupAddress, deliveryAddress, distance, fee, cod, status, createdAt
```

## Yêu cầu

- Android Studio Ladybug (2024.2+) hoặc mới hơn
- JDK 17
- minSdk 24 / targetSdk 35
- Kết nối Internet (Firebase)

## Cách mở project

1. Giải nén file ZIP
2. Mở Android Studio → **Open** → chọn thư mục `ShopeeFoodDriver`
3. Đợi Gradle sync xong (có thể cần thêm SHA-1 vào Firebase Console nếu dùng Auth nâng cao)
4. Chạy trên emulator hoặc thiết bị thật (cần Internet)

## Build APK

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Hoặc dùng GitHub Actions (workflow Build APK).

## Lưu ý quan trọng

1. **Firebase Auth**: Phải bật Email/Password trong Firebase Console → Authentication → Sign-in method.
2. **Realtime Database Rules**: Tạm thời có thể để test mode hoặc rules cho phép đọc/ghi cho user đã đăng nhập:
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```
3. Package name: `com.example.shopeefooddriver` (đã khớp với google-services.json).
4. Đây là bản demo – không kết nối backend thật của ShopeeFood.
