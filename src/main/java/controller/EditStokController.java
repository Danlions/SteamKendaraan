package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DatabaseConnection;
import model.StokTableData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditStokController {

    @FXML private Label lblIdStok; 
    @FXML private TextField txtNamaBarang;
    @FXML private TextField txtJumlah;
    @FXML private ComboBox<String> cbSatuan;
    @FXML private TextArea txtKeterangan;

    private KelolaStokBarangController parentController;

    @FXML
    public void initialize() {
        if (cbSatuan != null && cbSatuan.getItems().isEmpty()) {
            cbSatuan.getItems().addAll("Unit", "Liter", "Pcs", "Meter", "Kotak", "Botol");
        }
    }

    public void setKelolaStokBarangController(KelolaStokBarangController controller) {
        this.parentController = controller;
    }

    public void setData(StokTableData stok) {
        lblIdStok.setText(String.valueOf(stok.getIdStok()));
        txtNamaBarang.setText(stok.getNamaBarang());
        txtJumlah.setText(String.valueOf(stok.getJumlah()));
        cbSatuan.setValue(stok.getSatuan());
        txtKeterangan.setText(stok.getKeterangan());
    }

    @FXML
    private void handleUpdateStok() {
        String id = lblIdStok.getText();
        String nama = txtNamaBarang.getText();
        String jumlahStr = txtJumlah.getText();
        String satuan = cbSatuan.getValue();
        String keterangan = txtKeterangan.getText();

        if (nama.isEmpty() || jumlahStr.isEmpty() || satuan == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Data wajib tidak boleh kosong!");
            alert.show();
            return;
        }

        String namaFileGambar;
        switch (satuan.toLowerCase()) {
            case "liter":
                namaFileGambar = "liter.png";
                break;
            case "pcs":
                namaFileGambar = "pcs.png";
                break;
            case "unit":
                namaFileGambar = "unit.png";
                break;
            case "meter":
                namaFileGambar = "meter.png";
                break;
            case "kotak":
                namaFileGambar = "kotak.png"; 
                break;
            case "botol":
                namaFileGambar = "botol.png";
                break;
            default:
                namaFileGambar = "unit.png"; // Fallback default
                break;
        }

        // UPDATE kolom gambar ke database
        String sql = "UPDATE stok SET nama_barang = ?, jumlah = ?, satuan = ?, keterangan = ?, gambar = ? WHERE id_stok = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setInt(2, Integer.parseInt(jumlahStr));
            ps.setString(3, satuan);
            ps.setString(4, keterangan);
            ps.setString(5, namaFileGambar);
            ps.setInt(6, Integer.parseInt(id));

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                if (parentController != null) {
                    parentController.loadData();
                }
                handleBatal();
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBatal() {
        Stage stage = (Stage) txtNamaBarang.getScene().getWindow();
        stage.close();
    }
}