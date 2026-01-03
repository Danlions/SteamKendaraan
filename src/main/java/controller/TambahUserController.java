package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class TambahUserController implements Initializable {

    @FXML private TextField txtNama;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole; 
    @FXML private Button btnSimpan;
    @FXML private Button btnBatal;
    
    // Role sesuai data: 1=Admin, 2=Kasir, 3=Koordinator
    private final ObservableList<String> roles = FXCollections.observableArrayList("Admin", "Kasir", "Koordinator");

    // Callback untuk me-refresh tabel utama
    private KelolaUserController kelolaUserController; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRole.setItems(roles);
        cbRole.getSelectionModel().selectFirst(); // Pilih Admin sebagai default
        
        btnBatal.setOnAction(e -> closeWindow());
    }
    
    // Metode yang dipanggil dari KelolaUserController untuk passing reference
    public void setKelolaUserController(KelolaUserController controller) {
        this.kelolaUserController = controller;
    }

    @FXML
    private void handleSimpanUser() {
        String nama = txtNama.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText(); // Password tidak perlu trim
        String roleText = cbRole.getSelectionModel().getSelectedItem();
        
        int roleId = getRoleIdFromText(roleText); 

        // --- VALIDASI INPUT ---
        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Validasi", "Field Nama, Username, dan Password harus diisi.", Alert.AlertType.WARNING);
            return;
        }

        if (roleId == 0) {
            showAlert("Validasi", "Pilih Role pengguna.", Alert.AlertType.WARNING);
            return;
        }

        if (saveUserToDatabase(nama, username, password, roleId)) {
            
            // 1. Refresh tabel di controller utama 
            if (kelolaUserController != null) {
                kelolaUserController.loadData();
            }
            
            closeWindow();
            
        } 
    }

    private boolean saveUserToDatabase(String nama, String username, String password, int roleId) {
        String sql = "INSERT INTO users (nama, username, password, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nama);
            ps.setString(2, username);
            ps.setString(3, password); 
            ps.setInt(4, roleId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            showAlert("Error Database", "Username '" + username + "' sudah digunakan. Pilih username lain.", Alert.AlertType.ERROR);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error Database", "Gagal menyimpan data pengguna: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnBatal.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    // Helper function untuk mapping role string ke ID
    private int getRoleIdFromText(String roleText) {
        if (roleText == null) return 0;
        
        switch (roleText) {
            case "Admin":
                return 1;
            case "Kasir":
                return 2;
            case "Koordinator":
                return 3;
            default:
                return 0;
        }
    }
}