package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DatabaseConnection;
import model.UserTableData;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class EditUserController implements Initializable {

    // --- FXML Elements ---
    @FXML private TextField txtNama;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword; 
    @FXML private ComboBox<String> cbRole; 
    @FXML private Button btnUpdate; 
    @FXML private Button btnBatal;
    
    private final ObservableList<String> roles = FXCollections.observableArrayList("Admin", "Kasir", "Koordinator");
    private UserTableData userToEdit;
    private KelolaUserController kelolaUserController; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRole.setItems(roles);
        btnBatal.setOnAction(e -> closeWindow());
    }
    

    public void initData(UserTableData user, KelolaUserController controller) {
        this.userToEdit = user;
        this.kelolaUserController = controller;
        
        if (userToEdit != null) {
            // Mengisi field dengan data lama
            txtNama.setText(userToEdit.getNama());
            txtUsername.setText(userToEdit.getUsername());
            cbRole.getSelectionModel().select(userToEdit.getRoleText());
            
            txtPassword.setPromptText("Biarkan kosong jika tidak ingin mengubah");
            
            System.out.println("EditUserController siap mengedit user ID: " + userToEdit.getId());
        }
    }

    @FXML
    private void handleUpdateUser() {
        // Ambil isi field saat ini (termasuk yang lama atau yang baru diedit/dihapus)
        String nama = txtNama.getText().trim();
        String username = txtUsername.getText().trim();
        String newPassword = txtPassword.getText(); 
        String roleText = cbRole.getSelectionModel().getSelectedItem();
        int roleId = getRoleIdFromText(roleText); 

        if (userToEdit == null) {
            System.err.println("User yang akan diedit tidak teridentifikasi.");
            return;
        }

        if (nama.isEmpty() || username.isEmpty() || roleId == 0) {
            showAlert("Validasi", "Field Nama, Username, dan Role harus diisi.", Alert.AlertType.WARNING);
            return;
        }
        
        // --- VALIDASI DUPLIKASI USERNAME ---
        if (!username.equals(userToEdit.getUsername()) && isUsernameExist(username)) {
            showAlert("Validasi", "Username '" + username + "' sudah digunakan oleh user lain.", Alert.AlertType.ERROR);
            return;
        }

        // Lakukan update ke Database
        if (updateUserInDatabase(nama, username, newPassword, roleId)) {
            
            //Refresh tabel dan tutup jendela
            if (kelolaUserController != null) {
                System.out.println("Update berhasil. Memanggil loadData() di KelolaUserController.");
                kelolaUserController.loadData();
            } else {
                 System.err.println("KelolaUserController is null. Gagal me-refresh tabel utama.");
            }
            
            closeWindow();
        } 
        // Jika gagal, showAlert sudah ditangani di dalam updateUserInDatabase
    }

    private boolean updateUserInDatabase(String nama, String username, String newPassword, int roleId) {
        String sql;
        int paramIndex = 1;
        
        if (newPassword.isEmpty()) {
            sql = "UPDATE users SET nama = ?, username = ?, role = ? WHERE id_user = ?";
        } else {
            sql = "UPDATE users SET nama = ?, username = ?, password = ?, role = ? WHERE id_user = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(paramIndex++, nama);
            ps.setString(paramIndex++, username);
            
            if (!newPassword.isEmpty()) {
                ps.setString(paramIndex++, newPassword); 
            }
            
            ps.setInt(paramIndex++, roleId);
            ps.setInt(paramIndex, userToEdit.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error Database", "Gagal memperbarui data pengguna: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }
    
    private boolean isUsernameExist(String username) {
        if (userToEdit == null) return true; 

        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND id_user != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setInt(2, userToEdit.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true; 
        }
        return false; 
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