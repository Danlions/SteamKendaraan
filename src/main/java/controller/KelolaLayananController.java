package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DatabaseConnection;
import model.LayananData;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.geometry.Insets;

public class KelolaLayananController implements Initializable {

    @FXML private FlowPane layananFlowPane; 
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterJenis;
    @FXML private Button btnAddLayanan;

    private ObservableList<LayananData> masterData = FXCollections.observableArrayList();
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        formatter.setMaximumFractionDigits(0);
        
        cmbFilterJenis.getItems().addAll("Semua", "Mobil", "Motor");
        cmbFilterJenis.getSelectionModel().selectFirst();

        loadData();

        txtSearch.textProperty().addListener((obs, old, val) -> displayCards());
        cmbFilterJenis.valueProperty().addListener((obs, old, val) -> displayCards());
    }

    public void loadData() {
        masterData.clear();
        String sql = "SELECT id_layanan, nama_layanan, harga, id_kendaraan FROM layanan ORDER BY id_layanan ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                masterData.add(new LayananData(
                    rs.getInt("id_layanan"),
                    rs.getString("nama_layanan"),
                    rs.getDouble("harga"),
                    rs.getInt("id_kendaraan")
                ));
            }
            displayCards();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error Database", "Gagal memuat data layanan!", Alert.AlertType.ERROR);
        }
    }

    private void displayCards() {
        layananFlowPane.getChildren().clear();
        String searchText = txtSearch.getText().toLowerCase();
        String selectedJenis = cmbFilterJenis.getValue();

        for (LayananData layanan : masterData) {
            boolean matchesSearch = layanan.getNamaLayanan().toLowerCase().contains(searchText);
            
            int targetId = 0;
            if ("Mobil".equals(selectedJenis)) targetId = 1;
            else if ("Motor".equals(selectedJenis)) targetId = 2;

            boolean matchesFilter = (targetId == 0) || (layanan.getJenisKendaraanId() == targetId);

            if (matchesSearch && matchesFilter) {
                layananFlowPane.getChildren().add(createCard(layanan));
            }
        }
    }

    private VBox createCard(LayananData layanan) {
        // 1. Container Utama (Card)
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(210); 
        card.setMinWidth(185);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 4);");

        // 2. Gambar
        String imageName = (layanan.getJenisKendaraanId() == 1) ? "mobil.png" : "motor.png";
        ImageView imgView = new ImageView();
        try {
            var imgStream = getClass().getResourceAsStream("/assets/" + imageName);
            if (imgStream != null) {
                imgView.setImage(new Image(imgStream));
            }
        } catch (Exception e) { e.printStackTrace(); }
        imgView.setFitWidth(85);
        imgView.setFitHeight(85);
        imgView.setPreserveRatio(true);

        // 3. Label Nama Layanan
        Label lblNama = new Label(layanan.getNamaLayanan());
        lblNama.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");
        lblNama.setWrapText(true);
        lblNama.setAlignment(Pos.CENTER);

        // 4. Label Harga
        String formattedPrice = formatter.format(layanan.getHarga()).replace("IDR", "Rp").trim();
        Label lblHarga = new Label(formattedPrice);
        lblHarga.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

        // 5. Tombol Aksi (Edit & Hapus)
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);
        
        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("btnEdit");
        btnEdit.setPrefWidth(50);
        btnEdit.setOnAction(e -> handleEditLayanan(layanan));

        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().add("btnDelete");
        btnHapus.setPrefWidth(50);
        btnHapus.setOnAction(e -> handleHapusLayanan(layanan));

        actions.getChildren().addAll(btnEdit, btnHapus);

        card.getChildren().addAll(imgView, lblNama, lblHarga, actions);

        return card;
    }

    @FXML
    private void handleAddLayanan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TambahLayanan.fxml"));
            Parent root = loader.load();
            // Jika ada controller untuk tambah, bisa dihubungkan di sini
            showStage(root, "Tambah Layanan Baru");
            loadData(); // Memuat ulang data setelah jendela tambah ditutup
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleEditLayanan(LayananData layanan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditLayanan.fxml"));
            Parent root = loader.load();
            EditLayananController ctrl = loader.getController();
            ctrl.initData(layanan, this);
            showStage(root, "Edit Layanan: " + layanan.getNamaLayanan());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleHapusLayanan(LayananData layanan) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Hapus layanan " + layanan.getNamaLayanan() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM layanan WHERE id_layanan = ?")) {
                    ps.setInt(1, layanan.getIdLayanan());
                    ps.executeUpdate();
                    loadData();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void showStage(Parent root, String title) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}