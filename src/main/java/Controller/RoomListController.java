package Controller;

import Dao.FirebaseConnection;
import Dao.RoomDAO;
import Model.Room;
import com.google.cloud.firestore.Firestore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class RoomListController {

    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> nameColumn;
    @FXML private TableColumn<Room, String> hostColumn;
    @FXML private TableColumn<Room, String> statusColumn;

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button joinRoomButton;

    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    private final ObservableList<Room> filteredList = FXCollections.observableArrayList();
    private final RoomDAO roomDAO;
    private String currentUser;

    public RoomListController() {
        Firestore db = FirebaseConnection.getConnection();
        roomDAO = new RoomDAO(db);
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        hostColumn.setCellValueFactory(cellData -> cellData.getValue().hostProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        loadRoomList();
        searchButton.setOnAction(e -> searchRoom());
        refreshButton.setOnAction(e -> loadRoomList());
        joinRoomButton.setOnAction(e -> joinRoom());
        backButton.setOnAction(e -> backAction());
    }

    private void loadRoomList() {
        try {
            roomList.clear();
            for (Map<String, Object> room : roomDAO.getWaitingRooms()) {
                String name = (String) room.get("room_name");
                String host = (String) room.get("host");
                String password = (String) room.get("password");
                String status = (password != null && !password.isEmpty()) ? "🔒" : "🔓";
                roomList.add(new Room(name, host, status));
            }
            filteredList.setAll(roomList);
            roomTable.setItems(filteredList);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể tải danh sách phòng!").showAndWait();
        }
    }

    private void searchRoom() {
        String keyword = searchField.getText().toLowerCase().trim();
        filteredList.setAll(keyword.isEmpty()
                ? roomList
                : roomList.filtered(room -> room.getName().toLowerCase().contains(keyword)));
    }

    private void joinRoom() {
        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một phòng trước!").showAndWait();
            return;
        }

        String roomName = selectedRoom.getName();
        String host = selectedRoom.getHost();

        try {
            String password = "";
            for (Map<String, Object> room : roomDAO.getWaitingRooms()) {
                if (roomName.equals(room.get("room_name")) && host.equals(room.get("host"))) {
                    password = (String) room.get("password");
                    break;
                }
            }
            final String expectedPassword = password;
            Runnable tryEnter = () -> {
                try {
                    if (roomDAO.joinRoom(roomName, host, currentUser, expectedPassword)) {
                        openBoardAndCloseList(roomName);
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Phòng đã đầy hoặc đã bắt đầu!").showAndWait();
                        loadRoomList();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            if (expectedPassword != null && !expectedPassword.isEmpty()) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Nhập mật khẩu phòng");
                dialog.setHeaderText("Phòng \"" + roomName + "\" yêu cầu mật khẩu");
                dialog.showAndWait().ifPresent(input -> {
                    if (Objects.equals(expectedPassword, input)) {
                        tryEnter.run();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Mật khẩu không đúng!").showAndWait();
                    }
                });
            } else {
                tryEnter.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBoardAndCloseList(String roomName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/board.fxml"));
            Parent root = loader.load();
            BoardController boardController = loader.getController();
            boardController.setCurrentUser(currentUser);

            Stage current = (Stage) roomTable.getScene().getWindow();
            current.setTitle("Phòng: " + roomName);
            current.setScene(new javafx.scene.Scene(root));
            current.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void backAction() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Home.fxml"));
            Parent root = loader.load();
            HomeController homeController = loader.getController();
            homeController.setCurrentUser(currentUser);
            Stage homeStage = new Stage();
            homeStage.setTitle("Trang chủ");
            homeStage.setScene(new javafx.scene.Scene(root));
            homeStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }
}
