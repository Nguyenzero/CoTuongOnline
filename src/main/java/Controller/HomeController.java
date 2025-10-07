package Controller;

import Client.GameClient;
import Dao.QuickMatchDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;

public class HomeController {

    @FXML private Label lblUsername;
    @FXML private Label lblTotalGames;
    @FXML private Label lblWins;
    @FXML private Label lblDraws;
    @FXML private Label lblPoints;
    @FXML private Label lblRank;

    @FXML private TextField txtMessage;  // ô nhập chat
    @FXML private Button btnSend;        // nút gửi chat

    private String currentUser;
    private GameClient gameClient;

    private Timeline timeline;
    private Alert waitingAlert;
    private boolean isWaiting = false;

    @FXML private TextArea txtChat;  // ô hiển thị chat

    // 👇 kết nối server ngay khi mở Home để nhận được tin nhắn đầu tiên
    @FXML
    private void initialize() {
        try {
            if (gameClient == null) {
                gameClient = new GameClient("localhost", 12345);
            }
            gameClient.setChatListener(this::onChat);
            gameClient.startListening();
            if (currentUser != null && !currentUser.isEmpty()) {
                gameClient.identify(currentUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Gọi từ ngoài để truyền client vào
    public void setGameClient(GameClient client) {
        this.gameClient = client;

        // 👇 lắng nghe chat từ GameClient
        client.setChatListener(this::onChat);

        // 👇 đảm bảo có luồng đọc để nhận CHAT/PM
        client.startListening();

        // gán sự kiện nút gửi (nếu đã set qua FXML thì vẫn ok vì gọi cùng 1 hàm)
        btnSend.setOnAction(e -> onChatSend());

        // 👇 gửi danh tính nếu đã có user
        if (currentUser != null && !currentUser.isEmpty()) {
            client.identify(currentUser);
        }
    }


    // khi ấn nút gửi
    @FXML
    private void onChat(ActionEvent e) {
        onChatSend();
    }

    private void onChatSend() {
        String text = txtMessage.getText().trim();
        if (text.isEmpty()) return;

        try {
            // 👇 tạo client nếu chưa được truyền vào
            if (gameClient == null) {
                gameClient = new GameClient("localhost", 12345);
                gameClient.setChatListener(this::onChat);
                gameClient.startListening(); // cần thiết vì constructor không tự start nữa
                if (currentUser != null && !currentUser.isEmpty()) {
                    gameClient.identify(currentUser);
                }
            }

            // 👉 nếu vì lý do nào đó chưa lắng nghe, echo cục bộ để không “mất” tin đầu tiên
            if (!gameClient.isListening()) {
                String local;
                if (text.startsWith("/")) {
                    int idx = text.indexOf(' ');
                    if (idx > 1) {
                        String target = text.substring(1, idx);
                        String body = text.substring(idx + 1);
                        local = "🔒 Bạn ➜ " + target + ": " + body;
                    } else {
                        local = "🔒 " + text;
                    }
                } else {
                    String name = (currentUser == null || currentUser.isEmpty()) ? "Bạn" : currentUser;
                    local = name + ": " + text;
                }
                onChat(local);
            }

            // gửi qua server
            gameClient.sendChat(text);
            txtMessage.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // hiện tin nhắn lên TextArea
    public void onChat(String message) {
        txtChat.appendText(message + "\n");
    }

    public void setUsername(String username) {
        currentUser = username;
        lblUsername.setText(username);
        // 👉 đảm bảo server biết tên người dùng
        if (gameClient != null) {
            gameClient.identify(username);
        }
    }

    public void loadStats(String username) {
        lblTotalGames.setText("Số ván: 10");
        lblWins.setText("Thắng: 5");
        lblDraws.setText("Hòa: 2");
        lblPoints.setText("Điểm: 1200");
        lblRank.setText("Hạng: 15");
    }

    // Xử lý khi nhấn nút "Chơi nhanh"
    @FXML
    private void playQuick() {
     QuickMatchDAO dao = new QuickMatchDAO();

        try {
            String opponent = dao.tryMatchOrEnqueue(currentUser);

            if (opponent != null) {
                openBoard(currentUser, opponent);
                return;
            }

            // Chưa có ai -> hiển thị dialog chờ
            isWaiting = true;
            waitingAlert = new Alert(Alert.AlertType.INFORMATION);
            waitingAlert.setTitle("Ghép phòng");
            waitingAlert.setHeaderText(null);
            waitingAlert.setContentText("Đang tìm phòng, vui lòng chờ...");
            ButtonType stopType = new ButtonType("Dừng");
            waitingAlert.getButtonTypes().setAll(stopType);
            waitingAlert.show();

            Node stopBtn = waitingAlert.getDialogPane().lookupButton(stopType);
            stopBtn.addEventFilter(ActionEvent.ACTION, ev -> {
                dao.cancelWaiting(currentUser);
                isWaiting = false;
                if (timeline != null) timeline.stop();
                waitingAlert.close();
                ev.consume();
            });

            // Kiểm tra định kỳ đối thủ
            timeline = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                try {
                    String foundOpponent = dao.getOpponent(currentUser);
                    if (foundOpponent != null) {
                        if (waitingAlert != null) waitingAlert.close();
                        isWaiting = false;
                        timeline.stop();
                        openBoard(currentUser, foundOpponent);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // Khi nhấn "Tạo phòng"
    @FXML
    private void openCreateRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/CreateRoom.fxml"));
            Parent root = loader.load();

            // Lấy controller của CreateRoom
            CreateRoomController controller = loader.getController();
            controller.setCurrentUser(currentUser); // ✅ Truyền user vào

            Stage stage = new Stage();
            stage.setTitle("Tạo phòng");
            stage.setScene(new Scene(root));
            stage.show();

            // Đóng Home nếu muốn
            Stage currentStage = (Stage) lblUsername.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Khi nhấn "Vào phòng"
    @FXML
    private void openRoomList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/RoomList.fxml"));
            Parent root = loader.load();

            // Lấy controller của RoomList
            RoomListController controller = loader.getController();
            controller.setCurrentUser(currentUser); // ✅ Truyền user vào

            Stage stage = new Stage();
            stage.setTitle("Danh sách phòng");
            stage.setScene(new Scene(root));
            stage.show();

            // Đóng Home nếu muốn
            Stage currentStage = (Stage) lblUsername.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Hàm mở bàn cờ
    private void openBoard(String player1, String player2) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/board.fxml"));
            Parent root = loader.load();

            // ✅ Truyền user sang BoardController
            BoardController boardController = loader.getController();
            boardController.setCurrentUser(currentUser);


            Stage stage = new Stage();
            stage.setTitle("Ván cờ - " + player1 + " vs " + player2);
            stage.setScene(new Scene(root));
            stage.show();

            // Đóng trang Home hiện tại
            Stage currentStage = (Stage) lblUsername.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    // Method to set the current user
    public void setCurrentUser(String username) {
        this.currentUser = username;
        lblUsername.setText( username);
        System.out.println("Current user set to: " + username);
        // 👇 thông báo cho server để chat riêng hoạt động bằng tên người dùng
        if (gameClient != null) {
            gameClient.identify(username);
        }
    }

    // Đăng xuất -> quay về màn hình đăng nhập
    @FXML
    private void logout() {
        try {
            QuickMatchDAO dao = new QuickMatchDAO();

            // Hủy trạng thái chờ ghép (nếu có)
            if (isWaiting) {
                try {
                    dao.cancelWaiting(currentUser);
                } catch (Exception ignored) {}
                isWaiting = false;
            }

            // Dừng kiểm tra ghép cặp
            if (timeline != null) timeline.stop();
            if (waitingAlert != null) waitingAlert.close();

            // Xóa user hiện tại
            currentUser = null;

            // Chuyển về Login.fxml trên cùng Stage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LoginRegister.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblUsername.getScene().getWindow();
            stage.setTitle("Đăng nhập");
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("✅ Đăng xuất thành công, đã hủy trạng thái chờ trên Firebase!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}