package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DatabaseConnection;
import model.PelangganData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditPelangganController {

    // PASTIKAN fx:id COCOK DENGAN FXML!
    @FXML private TextField txtNoTelp;
    @FXML private TextField txtNamaPelanggan;
    @FXML private Button btnUpdate;
    @FXML private Button btnBatal;

    private PelangganData pelangganToEdit;
    

    public void initData(PelangganData pelanggan) {
        this.pelangganToEdit = pelanggan;
        
        if (pelanggan != null) {
            //Menampilkan isi sebelumnya
            txtNoTelp.setText(pelanggan.getNoTelp());
            
            txtNamaPelanggan.setText(pelanggan.getNamaPelanggan());
        }
    }

    @FXML
    private void handleBatal() {
        Stage stage = (Stage) btnBatal.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleUpdatePelanggan() {
        String noTelpID = txtNoTelp.getText().trim();
        String namaPelangganBaru = txtNamaPelanggan.getText().trim();

        if (namaPelangganBaru.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Nama Pelanggan harus diisi.");
            return;
        }

        String sql = "UPDATE pelanggan SET nama_pelanggan = ? WHERE no_telp = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, namaPelangganBaru);
            ps.setString(2, noTelpID);
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                 handleBatal(); 
            } else {
                 showAlert(Alert.AlertType.WARNING, "Gagal Update", "Data pelanggan tidak ditemukan atau tidak ada perubahan.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal memperbarui pelanggan. Pesan: " + e.getMessage());
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