package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class TambahLayananController implements Initializable {

    @FXML private TextField txtNamaLayanan; // Ganti dari txtNama
    @FXML private TextField txtHarga;       // Ganti dari txtUsername/txtPassword
    @FXML private ComboBox<String> cmbJenisKendaraan; // Ganti dari cbRole
    @FXML private Button btnSimpan;
    @FXML private Button btnBatal;
    
    // Jenis Kendaraan
    private final ObservableList<String> jenisKendaraanList = FXCollections.observableArrayList("Mobil", "Motor");

    // Callback untuk me-refresh tabel utama
    private KelolaLayananController kelolaLayananController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbJenisKendaraan.setItems(jenisKendaraanList);
        cmbJenisKendaraan.getSelectionModel().selectFirst();
        
        btnBatal.setOnAction(e -> closeWindow());
    }
    
    // Metode yang dipanggil dari KelolaLayananController untuk passing reference
    public void setKelolaLayananController(KelolaLayananController controller) {
        this.kelolaLayananController = controller;
    }

    @FXML
    private void handleSimpanLayanan() { // Ganti dari handleSimpanUser
        String namaLayanan = txtNamaLayanan.getText().trim();
        String hargaText = txtHarga.getText().trim();
        String jenisKendaraanText = cmbJenisKendaraan.getSelectionModel().getSelectedItem();
        
        int idKendaraan = getKendaraanIdFromText(jenisKendaraanText); 

        // 1. Validasi Input Kosong
        if (namaLayanan.isEmpty() || hargaText.isEmpty()) {
            showAlert("Validasi", "Field Nama Layanan dan Harga harus diisi.", Alert.AlertType.WARNING);
            return;
        }

        if (idKendaraan == 0) {
            showAlert("Validasi", "Pilih Jenis Kendaraan.", Alert.AlertType.WARNING);
            return;
        }
        
        double harga;
        // 2. Validasi Harga 
        try {
            harga = Double.parseDouble(hargaText);
            if (harga <= 0) {
                showAlert("Validasi", "Harga harus lebih besar dari nol.", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Validasi", "Harga harus berupa angka yang valid.", Alert.AlertType.WARNING);
            return;
        }

        // 3. Simpan ke Database
        if (saveLayananToDatabase(namaLayanan, harga, idKendaraan)) {
            
           
            // Refresh tabel di controller utama 
            if (kelolaLayananController != null) {
                kelolaLayananController.loadData();
            }
            
            closeWindow();
        } 
    }

    private boolean saveLayananToDatabase(String namaLayanan, double harga, int idKendaraan) {
        String sql = "INSERT INTO layanan (nama_layanan, harga, id_kendaraan) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, namaLayanan);
            ps.setDouble(2, harga);
            ps.setInt(3, idKendaraan);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error Database", "Gagal menyimpan data layanan: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    // --- UTILITY METHODS ---
    
    private void closeWindow() {
        // Menggunakan btnBatal karena lebih umum untuk tombol penutup di form
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
    
    // Helper function untuk mapping jenis kendaraan string ke ID
    private int getKendaraanIdFromText(String jenisKendaraanText) {
        if (jenisKendaraanText == null) return 0;
        switch (jenisKendaraanText) {
            case "Mobil":
                return 1;
            case "Motor":
                return 2;
            default:
                return 0;
        }
    }
}