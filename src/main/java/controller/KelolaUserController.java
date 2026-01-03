package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DatabaseConnection;
import model.UserTableData;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.geometry.Insets;

public class KelolaUserController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterRole;
    @FXML private FlowPane userFlowPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup awal combo box filter
        cmbFilterRole.getItems().addAll("Semua", "Admin", "Kasir", "Koordinator");
        cmbFilterRole.getSelectionModel().selectFirst();

        // Load data awal
        loadData();

        // Listener untuk pencarian dan filter otomatis
        txtSearch.textProperty().addListener((obs, old, val) -> loadData());
        cmbFilterRole.valueProperty().addListener((obs, old, val) -> loadData());
    }

    public void loadData() {
        userFlowPane.getChildren().clear();
        String keyword = txtSearch.getText().trim();
        String roleFilter = cmbFilterRole.getValue();

        // Konversi filter teks ke ID (1, 2, 3)
        int filterId = 0;
        if ("Admin".equals(roleFilter)) filterId = 1;
        else if ("Kasir".equals(roleFilter)) filterId = 2;
        else if ("Koordinator".equals(roleFilter)) filterId = 3;

        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE (nama LIKE ? OR username LIKE ?)");
        if (filterId > 0) {
            sql.append(" AND role = ").append(filterId);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            String searchVal = "%" + keyword + "%";
            stmt.setString(1, searchVal);
            stmt.setString(2, searchVal);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserTableData user = new UserTableData(
                    rs.getInt("id_user"),
                    rs.getString("nama"),
                    rs.getString("username"),
                    rs.getInt("role")
                );
                
                userFlowPane.getChildren().add(createUserCard(user));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createUserCard(UserTableData user) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(210); 
        card.setMinWidth(185);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Gunakan getRoleId()
        String roleImg = "kasir"; 
        if (user.getRoleId() == 1) roleImg = "admin";
        else if (user.getRoleId() == 3) roleImg = "koord";

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(getClass().getResourceAsStream("/assets/" + roleImg + ".png")));
        } catch (Exception e) {
            img.setImage(new Image(getClass().getResourceAsStream("/assets/user.png")));
        }
        img.setFitWidth(65);
        img.setFitHeight(65);

        Label lblNama = new Label(user.getNama());
        lblNama.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label lblUser = new Label("@" + user.getUsername());
        lblUser.setStyle("-fx-text-fill: gray;");

        // Tombol aksi
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        
        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("btnEdit");
        btnEdit.setPrefWidth(50); // Atur lebar pasti
        btnEdit.setOnAction(e -> handleEditUser(user));

        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().add("btnDelete");
        btnHapus.setPrefWidth(50); // Gunakan nilai yang sama
        btnHapus.setOnAction(e -> handleDeleteUser(user.getId(), user.getNama()));

        actions.getChildren().addAll(btnEdit, btnHapus);
        card.getChildren().addAll(img, lblNama, lblUser, actions);

        return card;
    }

    @FXML
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TambahUser.fxml"));
            Parent root = loader.load();
            
            // Sesuai kodingan kamu: setKelolaUserController
            TambahUserController controller = loader.getController();
            controller.setKelolaUserController(this); 

            showModal("Tambah User Baru", root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEditUser(UserTableData user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditUser.fxml"));
            Parent root = loader.load();
            
            // Sesuai kodingan kamu: initData(UserTableData, KelolaUserController)
            EditUserController controller = loader.getController();
            controller.initData(user, this); 

            showModal("Edit User", root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(int id, String nama) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hapus User");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin menghapus " + nama + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id_user = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadData(); // Refresh tampilan
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showModal(String title, Parent root) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
    }
}