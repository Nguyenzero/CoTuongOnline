package Dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UserDAO {

    private static final String COLLECTION = "users"; // tên collection trong Firestore

    // Đăng ký người dùng
    public static boolean addUser(String username, String password, String email) {
        Firestore db = FirebaseConnection.getConnection();

        try {
            // Kiểm tra xem username đã tồn tại chưa
            DocumentReference docRef = db.collection(COLLECTION).document(username);
            if (docRef.get().get().exists()) {
                System.out.println("❌ Tài khoản đã tồn tại!");
                return false;
            }

            // Tạo dữ liệu mới
            Map<String, Object> user = new HashMap<>();
            user.put("username", username);
            user.put("password", password); // 👉 có thể mã hoá sau này
            user.put("email", email);

            // Lưu vào Firestore
            ApiFuture<WriteResult> future = docRef.set(user);
            future.get(); // đợi ghi xong

            System.out.println("✅ Đăng ký thành công: " + username);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra đăng nhập
    public static boolean checkLogin(String username, String password) {
        Firestore db = FirebaseConnection.getConnection();

        try {
            DocumentReference docRef = db.collection(COLLECTION).document(username);
            DocumentSnapshot snapshot = docRef.get().get();

            if (snapshot.exists()) {
                String storedPassword = snapshot.getString("password");
                return storedPassword != null && storedPassword.equals(password);
            }
            return false;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
}