# Cờ Tướng Online (Vietnamese Chess Online)

## Giới thiệu / Introduction

Đây là một ứng dụng chơi Cờ Tướng trực tuyến được xây dựng bằng JavaFX và Firebase. Dự án cho phép người chơi tạo phòng, tham gia phòng và chơi Cờ Tướng với nhau qua mạng.

This is an online Vietnamese Chess application built with JavaFX and Firebase. The project allows players to create rooms, join rooms, and play Vietnamese Chess together over the network.

## Tôi có thể làm gì / What I Can Do

### 1. **Phân tích và Hiểu Code / Code Analysis and Understanding**
- Đọc và phân tích cấu trúc dự án Java/JavaFX
- Hiểu các mẫu thiết kế (DAO, MVC) được sử dụng
- Phân tích các phụ thuộc và cấu hình Maven

### 2. **Sửa lỗi / Bug Fixes**
- Sửa lỗi biên dịch (ví dụ: vấn đề phiên bản Java)
- Sửa lỗi logic trong code
- Khắc phục sự cố kết nối Firebase

### 3. **Thêm Tính năng / Add Features**
- Thêm tính năng mới cho game
- Cải thiện giao diện người dùng
- Thêm chức năng lưu/tải trò chơi

### 4. **Tối ưu hóa / Optimization**
- Cải thiện hiệu suất code
- Tối ưu hóa kết nối mạng
- Giảm sử dụng bộ nhớ

### 5. **Kiểm thử / Testing**
- Viết unit tests
- Tạo integration tests
- Kiểm tra lỗi và edge cases

### 6. **Tài liệu / Documentation**
- Viết tài liệu code
- Tạo hướng dẫn sử dụng
- Giải thích kiến trúc hệ thống

### 7. **Cấu hình / Configuration**
- Cập nhật cấu hình Maven
- Quản lý dependencies
- Thiết lập môi trường phát triển

## Cấu trúc Dự án / Project Structure

```
src/main/java/
├── Client/           # Client-side game logic
│   └── GameClient.java
├── Controller/       # JavaFX controllers
│   ├── BoardController.java
│   ├── CreateRoomController.java
│   ├── HomeController.java
│   ├── LoginRegister.java
│   ├── RoomListController.java
│   ├── WaitingRoomController.java
│   └── WinnerDialog.java
├── Dao/             # Data Access Objects
│   ├── FirebaseConnection.java
│   ├── QuickMatchDAO.java
│   ├── RoomDAO.java
│   └── UserDAO.java
├── Model/           # Data models
│   └── Room.java
├── Server/          # Server-side logic
│   └── GameServer.java
└── Main.java        # Application entry point

src/main/resources/
└── View/            # FXML view files
    ├── CreateRoom.fxml
    ├── LoginRegister.fxml
    ├── RoomList.fxml
    ├── WaitingRoom.fxml
    ├── board.fxml
    └── home.fxml
```

## Công nghệ sử dụng / Technologies Used

- **Java 17** - Ngôn ngữ lập trình chính
- **JavaFX 24.0.2** - Framework giao diện đồ họa
- **Firebase Admin SDK 9.2.0** - Backend và database
- **Maven** - Quản lý dự án và dependencies
- **Gson 2.10.1** - Xử lý JSON
- **SLF4J 2.0.12** - Logging

## Yêu cầu hệ thống / System Requirements

- Java Development Kit (JDK) 17 hoặc cao hơn
- Maven 3.6+
- Kết nối Internet (cho Firebase)

## Cài đặt và Chạy / Installation and Running

### 1. Clone repository
```bash
git clone https://github.com/Nguyenzero/CoTuongOnline.git
cd CoTuongOnline
```

### 2. Build project
```bash
mvn clean compile
```

### 3. Chạy ứng dụng / Run application
```bash
mvn javafx:run
```

## Tính năng / Features

- ✅ Đăng nhập/Đăng ký người dùng
- ✅ Tạo phòng chơi
- ✅ Tham gia phòng chơi
- ✅ Chơi Cờ Tướng theo luật chuẩn
- ✅ Kết nối realtime với Firebase
- ✅ Giao diện đồ họa thân thiện

## Đóng góp / Contributing

Mọi đóng góp đều được chào đón! Vui lòng:
1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit thay đổi (`git commit -m 'Add some AmazingFeature'`)
4. Push lên branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## License

[Thêm thông tin license ở đây]

## Liên hệ / Contact

Project Link: [https://github.com/Nguyenzero/CoTuongOnline](https://github.com/Nguyenzero/CoTuongOnline)
