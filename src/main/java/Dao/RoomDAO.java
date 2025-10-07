package Dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class RoomDAO {
    private final Firestore db;

    public RoomDAO(Firestore db) {
        this.db = db;
    }

    // ✅ Tạo phòng mới (giống MySQL createRoom)
    public void createRoom(String roomName, String host, String password) throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("room_name", roomName);
        data.put("host", host);
        data.put("guest", "");
        data.put("password", (password != null ? password : ""));
        data.put("status", "waiting");

        String docId = roomName + "_" + host; // đảm bảo duy nhất
        db.collection("rooms").document(docId).set(data).get();
    }

    // ✅ Lấy danh sách phòng chờ
    public List<Map<String, Object>> getWaitingRooms() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = db.collection("rooms")
                .whereEqualTo("status", "waiting")
                .get();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            result.add(doc.getData());
        }
        return result;
    }

    // ✅ Guest vào phòng (kiểm tra mật khẩu, cập nhật status)
    public boolean joinRoom(String roomName, String host, String guest, String password)
            throws ExecutionException, InterruptedException {

        String docId = roomName + "_" + host;
        DocumentReference ref = db.collection("rooms").document(docId);
        DocumentSnapshot snap = ref.get().get();

        if (!snap.exists()) return false;

        String realPassword = snap.getString("password");
        String guestNow = snap.getString("guest");
        String status = snap.getString("status");

        if (!"waiting".equals(status)) return false;

        if (Objects.equals(realPassword, password == null ? "" : password)
                && (guestNow == null || guestNow.isEmpty())) {

            ref.update("guest", guest, "status", "matched").get();
            return true;
        }
        return false;
    }

    // ✅ Lấy host và guest trong phòng
    public String[] getPlayers(String roomName, String host) throws ExecutionException, InterruptedException {
        String docId = roomName + "_" + host;
        DocumentSnapshot snap = db.collection("rooms").document(docId).get().get();
        if (snap.exists()) {
            return new String[]{snap.getString("host"), snap.getString("guest")};
        }
        return null;
    }

    // ✅ Xóa phòng
    public void deleteRoom(String roomName, String host) throws ExecutionException, InterruptedException {
        String docId = roomName + "_" + host;
        db.collection("rooms").document(docId).delete().get();
    }

    // ✅ Lấy guest hiện tại của phòng (dùng cho WaitingRoomController)
    public String getGuest(String roomName, String host) throws ExecutionException, InterruptedException {
        String docId = roomName + "_" + host;
        DocumentSnapshot snap = db.collection("rooms").document(docId).get().get();
        return snap.exists() ? snap.getString("guest") : null;
    }
}
