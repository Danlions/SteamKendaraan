package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.DatabaseConnection;
import model.TransaksiData; 

public class UpdateStatusController {

    @FXML
    private TextField txtPelangganPlat;
    @FXML
    private ComboBox<String> cbStatusBaru;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnBatal;

    private TransaksiData dataToUpdate;
    private Runnable refreshCallback; 

    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "Belum Dicuci", 
            "Sedang Dicuci", 
            "Selesai"
    );

    public void initData(TransaksiData data, Runnable refreshCallback) {
        this.dataToUpdate = data;
        this.refreshCallback = refreshCallback;
        
        // Hanya mengisi field Pelanggan & Plat
        txtPelangganPlat.setText(data.getNamaPelanggan() + " (" + data.getPlatNomor() + ")");
        
        cbStatusBaru.setItems(statusOptions);
        
        // Pilih status saat ini sebagai default
        cbStatusBaru.getSelectionModel().select(data.getStatus());
    }

    @FXML
    private void handleSimpanStatus(ActionEvent event) {
        String newStatus = cbStatusBaru.getSelectionModel().getSelectedItem();
        
        if (newStatus == null || newStatus.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validasi Gagal", "Status Baru harus dipilih.");
            return;
        }
       
        // Memanggil fungsi update tanpa parameter keterangan
        if (updateStatusInDatabase(dataToUpdate.getIdTransaksi(), newStatus)) {
              
            // Panggil callback untuk refresh TableView di parent controller
            if (refreshCallback != null) {
                refreshCallback.run();
            }
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal mengupdate status di database.");
        }
    }

    // Mengubah signature: Hapus parameter String keterangan
    private boolean updateStatusInDatabase(int idTransaksi, String newStatus) {
        String SQL = "UPDATE transaksi SET status = ? WHERE id_transaksi = ?"; 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, idTransaksi); 

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnBatal.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}