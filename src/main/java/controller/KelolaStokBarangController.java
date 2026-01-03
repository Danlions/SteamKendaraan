package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DatabaseConnection;
import model.StokTableData;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class KelolaStokBarangController implements Initializable {

    @FXML private FlowPane cardContainer;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> filterSatuan; 

    private ObservableList<StokTableData> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Setup ComboBox Satuan
        if (filterSatuan != null) {
            filterSatuan.getItems().addAll("Semua", "Unit", "Liter", "Pcs", "Meter", "Kotak", "Botol");
            filterSatuan.setValue("Semua");
            
            // Listener Filter ComboBox
            filterSatuan.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateFilterButtonStyle(newVal);
                applyFilterAndSearch();
            });
        }

        // 2. Load Data Awal
        loadData();

        // 3. Listener Search Bar
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSearch());
    }

    private void updateFilterButtonStyle(String value) {
        if (value.equals("Semua")) {
            filterSatuan.getStyleClass().remove("btnFilterActive");
        } else {
            if (!filterSatuan.getStyleClass().contains("btnFilterActive")) {
                filterSatuan.getStyleClass().add("btnFilterActive");
            }
        }
    }

    public void loadData() {
        masterData.clear();
        String sql = "SELECT * FROM stok ORDER BY id_stok DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                masterData.add(new StokTableData(
                        rs.getInt("id_stok"), 
                        rs.getString("nama_barang"),
                        rs.getInt("jumlah"), 
                        rs.getString("satuan"),
                        rs.getString("keterangan"), 
                        rs.getString("gambar")
                ));
            }
            applyFilterAndSearch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyFilterAndSearch() {
        cardContainer.getChildren().clear();
        String search = txtSearch.getText().toLowerCase();
        String selectedSatuan = filterSatuan.getValue();

        masterData.stream()
            .filter(s -> (selectedSatuan.equals("Semua") || s.getSatuan().equalsIgnoreCase(selectedSatuan)))
            .filter(s -> s.getNamaBarang().toLowerCase().contains(search) || 
                         (s.getKeterangan() != null && s.getKeterangan().toLowerCase().contains(search)))
            .forEach(stok -> cardContainer.getChildren().add(createCard(stok)));
    }

    private VBox createCard(StokTableData stok) {
        // Container Utama Kartu
        VBox card = new VBox(10); 
        card.setPadding(new Insets(15));
        card.setPrefWidth(210); 
        card.setMinWidth(185);
        card.setAlignment(Pos.TOP_CENTER); // Penting: Agar elemen tersusun dari atas
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // 1. Gambar Barang
        ImageView imgView = new ImageView();
        String namaFile = (stok.getGambar() != null && !stok.getGambar().isEmpty()) ? stok.getGambar() : "box.png";
        try {
            URL imgUrl = getClass().getResource("/assets/" + namaFile);
            imgView.setImage(new Image(imgUrl != null ? imgUrl.toExternalForm() : getClass().getResource("/assets/kotak.png").toExternalForm()));
        } catch (Exception e) {}
        imgView.setFitHeight(65); 
        imgView.setFitWidth(65); 
        imgView.setPreserveRatio(true);

        // 2. Nama Barang
        Label lblNama = new Label(stok.getNamaBarang());
        lblNama.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        lblNama.setWrapText(true); 
        lblNama.setTextAlignment(TextAlignment.CENTER);
        lblNama.setAlignment(Pos.CENTER);
        lblNama.setMaxWidth(160);

        // 3. Info Stok (Jumlah + Satuan)
        Label lblStok = new Label(stok.getJumlah() + " " + stok.getSatuan());
        lblStok.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 12px;");

        // 4. Keterangan
        String ketText = (stok.getKeterangan() == null || stok.getKeterangan().isEmpty()) ? "-" : stok.getKeterangan();
        Label lblKet = new Label(ketText);
        lblKet.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px; -fx-font-style: italic;");
        lblKet.setWrapText(true); 
        lblKet.setMinHeight(35); // Memberi ruang minimal agar layout tidak berantakan
        lblKet.setTextAlignment(TextAlignment.CENTER);
        lblKet.setAlignment(Pos.CENTER);
        lblKet.setMaxWidth(160);

        // 5. Tombol Aksi (Edit & Hapus)
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        
        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("btnEdit");
        btnEdit.setPrefWidth(50); // Atur lebar pasti
        btnEdit.setOnAction(e -> handleEdit(stok));

        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().add("btnDelete");
        btnHapus.setPrefWidth(50); // Gunakan nilai yang sama
        btnHapus.setOnAction(e -> handleHapus(stok));

        actions.getChildren().addAll(btnEdit, btnHapus);

        // Menambahkan semua komponen ke dalam VBox kartu
        card.getChildren().addAll(imgView, lblNama, lblStok, lblKet, actions);
        
        return card;
    }

    @FXML
    private void handleAddStok() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TambahStok.fxml"));
            Parent root = loader.load();
            TambahStokController controller = loader.getController();
            controller.setKelolaStokBarangController(this);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Tambah Stok");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEdit(StokTableData stok) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditStok.fxml"));
            Parent root = loader.load();
            EditStokController controller = loader.getController();
            controller.setData(stok);
            controller.setKelolaStokBarangController(this);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Stok");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleHapus(StokTableData stok) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Hapus " + stok.getNamaBarang() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM stok WHERE id_stok = ?")) {
                ps.setInt(1, stok.getIdStok());
                ps.executeUpdate();
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}