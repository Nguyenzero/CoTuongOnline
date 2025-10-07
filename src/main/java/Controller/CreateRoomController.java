package Controller;

import Dao.FirebaseConnection;
import Dao.RoomDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.google.cloud.firestore.Firestore;

public class CreateRoomController {

    @FXML private TextField roomNameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private String currentUser; // host
    private final RoomDAO roomDAO;

    public CreateRoomController() {
        Firestore db = FirebaseConnection.getConnection();
        roomDAO = new RoomDAO(db);
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    @FXML
    private void handleCreateRoom() {
        String roomName = roomNameField.getText().trim();
        String password = passwordField.getText().trim();

        if (roomName.isEmpty()) {
            statusLabel.setText("Tên phòng không được để trống!");
            return;
        }
        if (currentUser == null || currentUser.isEmpty()) {
            statusLabel.setText("Lỗi: chưa xác định người tạo phòng!");
            return;
        }

        try {
            roomDAO.createRoom(roomName, currentUser, password.isEmpty() ? "" : password);
            statusLabel.setText("Tạo phòng thành công!");
            openWaitingRoom(roomName, password);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi khi tạo phòng!");
        }
    }

    private void openWaitingRoom(String roomName, String password) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/WaitingRoom.fxml"));
            Parent root = loader.load();

            WaitingRoomController waitingCtrl = loader.getController();
            waitingCtrl.init(roomName, password, currentUser);

            Stage stage = new Stage();
            stage.setTitle("Phòng: " + roomName);
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) roomNameField.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Không mở được màn hình chờ!");
        }
    }

    @FXML
    private void handleCancel() {
        backToHome();
    }

    private void backToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUsername(currentUser);
            homeController.loadStats(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Trang chủ");
            stage.setScene(new Scene(root));
            stage.show();

            ((Stage) roomNameField.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
