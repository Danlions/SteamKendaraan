package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TambahStokController {

    @FXML private TextField txtNamaBarang;
    @FXML private TextField txtJumlah;
    @FXML private ComboBox<String> cbSatuan;
    @FXML private TextArea txtKeterangan;

    private KelolaStokBarangController mainController;

    /**
     * Menerima referensi dari dashboard utama agar bisa refresh data
     */
    public void setKelolaStokBarangController(KelolaStokBarangController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        // Mengisi pilihan satuan pada ComboBox
        if (cbSatuan != null) {
            cbSatuan.getItems().addAll("Unit", "Liter", "Pcs", "Meter", "Kotak", "Botol");
        }
    }

    @FXML
    private void handleSimpanStok() {
        String nama = txtNamaBarang.getText();
        String jumlahStr = txtJumlah.getText();
        String satuan = cbSatuan.getValue();

        String namaGambar = (satuan != null) ? satuan.toLowerCase() + ".png" : "box.png";

        // Validasi input
        if (nama.isEmpty() || jumlahStr.isEmpty() || satuan == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Mohon lengkapi data yang bertanda bintang (*)");
            alert.showAndWait();
            return;
        }

        String sql = "INSERT INTO stok (nama_barang, jumlah, satuan, keterangan, gambar) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setInt(2, Integer.parseInt(jumlahStr));
            ps.setString(3, satuan);
            ps.setString(4, txtKeterangan.getText());
            ps.setString(5, namaGambar);
            
            ps.executeUpdate();

            // Memanggil refresh data pada dashboard utama
            if (mainController != null) {
                mainController.loadData();
            }

            // Tutup jendela popup
            handleBatal(); 

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Jumlah harus berupa angka!").show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal menyimpan data: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleBatal() {
        Stage stage = (Stage) txtNamaBarang.getScene().getWindow();
        stage.close();
    }
}