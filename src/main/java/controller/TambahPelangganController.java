package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TambahPelangganController {

    @FXML private TextField txtNoTelp;
    @FXML private TextField txtNamaPelanggan;
    @FXML private Button btnSimpan;
    @FXML private Button btnBatal;

    @FXML
    private void handleBatal() {
        Stage stage = (Stage) btnBatal.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSimpanPelanggan() {
        String noTelp = txtNoTelp.getText().trim();
        String namaPelanggan = txtNamaPelanggan.getText().trim();

        // 1. Validasi Input
        if (noTelp.isEmpty() || namaPelanggan.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Semua bidang bertanda (*) harus diisi.");
            return;
        }
        
        if (!noTelp.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Nomor Telepon hanya boleh mengandung angka.");
            return;
        }


        // 2. Simpan ke Database
        String sql = "INSERT INTO pelanggan (no_telp, nama_pelanggan) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, noTelp);
            ps.setString(2, namaPelanggan);
            
            ps.executeUpdate();
                
            handleBatal(); 

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan: Nomor Telepon ini (" + noTelp + ") sudah terdaftar sebagai pelanggan.");
            } else {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal menambahkan pelanggan. Pesan: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}