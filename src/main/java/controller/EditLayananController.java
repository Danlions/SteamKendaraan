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
import model.LayananData; 
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class EditLayananController implements Initializable {

    @FXML private TextField txtNamaLayanan;
    @FXML private TextField txtHarga;
    @FXML private ComboBox<String> cbJenisKendaraan;
    @FXML private Button btnBatal;
    @FXML private Button btnUpdate;

    private LayananData layananToEdit;
    private KelolaLayananController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbJenisKendaraan.setItems(FXCollections.observableArrayList("Mobil", "Motor"));
    }

    public void initData(LayananData layanan, KelolaLayananController parent) {
        this.layananToEdit = layanan;
        this.parentController = parent;

        txtNamaLayanan.setText(layanan.getNamaLayanan());
        txtHarga.setText(String.valueOf(layanan.getHarga()));
        
        // Set awal ComboBox berdasarkan ID yang ada
        if (layanan.getJenisKendaraanId() == 1) cbJenisKendaraan.getSelectionModel().select("Mobil");
        else if (layanan.getJenisKendaraanId() == 2) cbJenisKendaraan.getSelectionModel().select("Motor");
    }

    @FXML
    private void handleUpdateLayanan() {
        String nama = txtNamaLayanan.getText();
        String hargaStr = txtHarga.getText();
        String jenis = cbJenisKendaraan.getValue();

        if (nama.isEmpty() || hargaStr.isEmpty() || jenis == null) {
            return;
        }

        int idKendaraanBaru = jenis.equals("Mobil") ? 1 : 2;

        String sql = "UPDATE layanan SET nama_layanan=?, harga=?, id_kendaraan=? WHERE id_layanan=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nama);
            ps.setDouble(2, Double.parseDouble(hargaStr));
            ps.setInt(3, idKendaraanBaru);
            ps.setInt(4, layananToEdit.getIdLayanan()); 

            int res = ps.executeUpdate();
            if (res > 0) {
                // REFRESH DATA DI PARENT
                if (parentController != null) {
                    parentController.loadData();
                }
                closeWindow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnUpdate.getScene().getWindow();
        stage.close();
    }
    
    @FXML private void handleBatal() { closeWindow(); }
}