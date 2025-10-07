package Controller;

import Dao.FirebaseConnection;
import Dao.RoomDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.google.cloud.firestore.Firestore;
import java.util.concurrent.*;

public class WaitingRoomController {

    @FXML private Label roomNameLabel;
    @FXML private Label hostLabel;
    @FXML private Label passwordLabel;
    @FXML private Label statusLabel;
    @FXML private Button cancelButton;

    private String roomName, host, password;
    private ScheduledExecutorService scheduler;
    private final RoomDAO roomDAO;

    public WaitingRoomController() {
        Firestore db = FirebaseConnection.getConnection();
        roomDAO = new RoomDAO(db);
    }

    public void init(String roomName, String password, String host) {
        this.roomName = roomName;
        this.password = password;
        this.host = host;

        roomNameLabel.setText(roomName);
        hostLabel.setText(host);
        passwordLabel.setText((password == null || password.isEmpty()) ? "(không mật khẩu)" : "********");
        statusLabel.setText("Tạo phòng thành công. Đang chờ người chơi khác...");

        startPolling();
    }

    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String guest = roomDAO.getGuest(roomName, host);
                if (guest != null && !guest.isEmpty()) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Người chơi " + guest + " đã vào phòng. Đang bắt đầu...");
                        stopPolling();
                        openBoard(host, guest);
                    });
                }
            } catch (Exception ignored) {}
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @FXML
    private void handleCancel() {
        stopPolling();
        try {
            roomDAO.deleteRoom(roomName, host);
        } catch (Exception e) {
            e.printStackTrace();
        }
        goToHome();
    }

    private void goToHome() {
        try {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUsername(host);
            homeController.loadStats(host);

            stage.setScene(new Scene(root));
            stage.setTitle("Trang chủ");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBoard(String host, String guest) {
        try {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/board.fxml"));
            Parent root = loader.load();

            BoardController boardController = loader.getController();
            boardController.setCurrentUser(host);

            stage.setScene(new Scene(root));
            stage.setTitle("Ván cờ - " + host + " vs " + guest);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
