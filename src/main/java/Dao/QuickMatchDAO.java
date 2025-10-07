package Dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class QuickMatchDAO {

    private final Firestore db;

    public QuickMatchDAO() {
        this.db = FirebaseConnection.getConnection();
    }

    // Thêm người chơi vào hàng chờ
    public void addWaitingPlayer(String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("status", "waiting");
        data.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.put("opponent", null);
        db.collection("quick_match").document(username).set(data);
    }

    // Tìm người chơi đang chờ (không phải mình)
    public String findWaitingPlayer(String currentUser) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("quick_match")
                .whereEqualTo("status", "waiting")
                .get();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            String username = doc.getString("username");
            if (!username.equals(currentUser)) return username;
        }
        return null;
    }

    // Cập nhật đối thủ khi ghép thành công
    public void setOpponent(String username, String opponent) {
        db.collection("quick_match").document(username)
                .update("status", "matched", "opponent", opponent);
    }

    // Lấy đối thủ của người chơi
    public String getOpponent(String username) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("quick_match").document(username).get().get();
        if (doc.exists() && "matched".equals(doc.getString("status"))) {
            return doc.getString("opponent");
        }
        return null;
    }

    // Xóa người chơi khỏi hàng chờ
    public void removeWaitingPlayer(String username) {
        db.collection("quick_match").document(username).delete();
    }

    // Ghép hoặc thêm vào hàng chờ
    public String tryMatchOrEnqueue(String currentUser) throws ExecutionException, InterruptedException {
        String opponent = findWaitingPlayer(currentUser);
        if (opponent != null) {
            // Cập nhật đối thủ
            setOpponent(opponent, currentUser);

            // Lưu bản thân
            Map<String, Object> data = new HashMap<>();
            data.put("username", currentUser);
            data.put("status", "matched");
            data.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            data.put("opponent", opponent);
            db.collection("quick_match").document(currentUser).set(data);
        } else {
            // Không có ai -> thêm vào hàng chờ
            addWaitingPlayer(currentUser);
        }
        return opponent; // null nếu đang chờ
    }

    // Hủy chờ nhanh
    public void cancelWaiting(String username) {
        removeWaitingPlayer(username);
    }
}