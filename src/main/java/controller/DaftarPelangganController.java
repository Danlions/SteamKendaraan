package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.DatabaseConnection;
import model.PelangganData; 

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class DaftarPelangganController implements Initializable {

    // --- FXML Components ---
    @FXML private TableView<PelangganData> pelangganTable;
    @FXML private TableColumn<PelangganData, String> colNo; 
    @FXML private TableColumn<PelangganData, String> colNoTelp;
    @FXML private TableColumn<PelangganData, String> colNamaPelanggan;
    @FXML private TableColumn<PelangganData, Void> colAction;

    @FXML private TextField txtSearch;
    @FXML private Button btnAddPelanggan;

    private ObservableList<PelangganData> masterData = FXCollections.observableArrayList();
    private FilteredList<PelangganData> filteredData;
    private PelangganData selectedPelanggan; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Inisialisasi Kolom Tabel
        setupNoColumn();
        
        colNoTelp.setCellValueFactory(data -> data.getValue().noTelpProperty()); 
        colNamaPelanggan.setCellValueFactory(data -> data.getValue().namaPelangganProperty());
        
        pelangganTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 2. Load Data Awal
        loadData();

        // 3. Set up Kolom Aksi (Edit dan Hapus)
        setupActionColumn();
        
        // 4. Setup Search/Filter
        setupSearch();

        // 5. Handle Klik pada Tabel (untuk mendapatkan objek yang dipilih)
        pelangganTable.setOnMouseClicked((MouseEvent e) -> {
            selectedPelanggan = pelangganTable.getSelectionModel().getSelectedItem();
        });
    }

    private void setupNoColumn() {
        colNo.setCellValueFactory(param -> null); 
        
        colNo.setCellFactory(col -> new TableCell<PelangganData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1)); 
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

    //Memuat data pelanggan dari database ke ObservableList.
    public void loadData() {
        masterData.clear();
        String sql = "SELECT no_telp, nama_pelanggan FROM pelanggan ORDER BY no_telp ASC"; 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                masterData.add(
                    new PelangganData(
                        rs.getString("no_telp"),
                        rs.getString("nama_pelanggan")
                    )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error Database", "Gagal memuat data pelanggan! Periksa koneksi dan tabel.", Alert.AlertType.ERROR);
        }
        
        // Memastikan filter diterapkan pada data yang baru dimuat
        if (filteredData != null) {
            applyFilter();
        }
    }
    
    // logika pencarian dan pemfilteran pada tabel.
    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true); // FilteredList awal
        
        // Listener untuk perubahan teks di kolom pencarian
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });

        // Binding SortedList ke TableView
        SortedList<PelangganData> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(pelangganTable.comparatorProperty());
        pelangganTable.setItems(sortedData);
    }
    
    // Menerapkan logika filter berdasarkan teks pencarian.
    private void applyFilter() {
        String searchText = txtSearch.getText();
        if (searchText == null || searchText.isEmpty()) {
             filteredData.setPredicate(p -> true);
             return; 
        }
        
        String lowerCaseFilter = searchText.toLowerCase();
        
        filteredData.setPredicate(pelanggan -> {
            // Filter berdasarkan No. Telp
            if (pelanggan.getNoTelp().contains(lowerCaseFilter)) {
                return true; 
            }
            // Filter berdasarkan Nama Pelanggan
            if (pelanggan.getNamaPelanggan().toLowerCase().contains(lowerCaseFilter)) {
                return true; 
            }
            return false;
        });
    }

    // Menangani pembukaan form Tambah Pelanggan.
    @FXML
    private void handleAddPelanggan(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TambahPelanggan.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.setTitle("Tambah Pelanggan Baru");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
            loadData(); // Refresh data setelah menambahkan
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form Tambah Pelanggan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void editPelanggan(PelangganData pelanggan) {
        if (pelanggan == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditPelanggan.fxml"));
            Parent root = loader.load();
            
            EditPelangganController editController = loader.getController();
            editController.initData(pelanggan); 
  
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.setTitle("Edit Pelanggan: " + pelanggan.getNamaPelanggan());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
            loadData(); // Refresh data setelah editing
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form Edit Pelanggan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void hapusPelanggan(PelangganData pelanggan) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus data pelanggan ini?");
        konfirmasi.setContentText("Anda akan menghapus pelanggan dengan No. Telp: " + pelanggan.getNoTelp());

        Optional<ButtonType> result = konfirmasi.showAndWait(); 

        if (result.isPresent() && result.get() == ButtonType.OK) { 
            String sql = "DELETE FROM pelanggan WHERE no_telp = ?"; 
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, pelanggan.getNoTelp());
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    showAlert("Sukses", "Data pelanggan berhasil dihapus.", Alert.AlertType.INFORMATION);
                    loadData(); 
                } else {
                    showAlert("Error", "Data pelanggan tidak ditemukan untuk dihapus.", Alert.AlertType.ERROR);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                // SQL Error Code 1451 adalah kode umum untuk Foreign Key Constraint
                if (e.getErrorCode() == 1451) { 
                    showAlert("Error Database", "Gagal menghapus pelanggan! Data ini masih terkait dengan data transaksi lain.", Alert.AlertType.ERROR);
                } else {
                    showAlert("Error Database", "Gagal menghapus pelanggan! Pesan DB: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    // Menyiapkan kolom aksi (Edit dan Hapus) dalam tabel.
    private void setupActionColumn() {
        final String EDIT_ICON_PATH = "/assets/edit.png";     
        final String DELETE_ICON_PATH = "/assets/delete.png"; 

        Callback<TableColumn<PelangganData, Void>, TableCell<PelangganData, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<PelangganData, Void> call(final TableColumn<PelangganData, Void> param) {
                final TableCell<PelangganData, Void> cell = new TableCell<>() {
                    private final Button btnEdit = new Button();
                    private final Button btnDelete = new Button();
                    
                    {
                        // 1. Setup Ikon
                        try {
                            ImageView editIcon = new ImageView(new Image(getClass().getResource(EDIT_ICON_PATH).toExternalForm()));
                            editIcon.setFitWidth(20); editIcon.setFitHeight(20); btnEdit.setGraphic(editIcon);
                            ImageView deleteIcon = new ImageView(new Image(getClass().getResource(DELETE_ICON_PATH).toExternalForm()));
                            deleteIcon.setFitWidth(20); deleteIcon.setFitHeight(20); btnDelete.setGraphic(deleteIcon);
                        } catch (Exception e) {
                            // Fallback jika ikon tidak ditemukan
                            btnEdit.setText("Edit"); btnDelete.setText("Hapus");
                        }
                        
                        // 2. Setup CSS Class
                        btnEdit.getStyleClass().addAll("icon-button", "edit-button");
                        btnDelete.getStyleClass().addAll("icon-button", "delete-button");
                        
                        // 3. Setup Aksi Tombol
                        btnEdit.setOnAction(event -> {
                            PelangganData pelanggan = getTableView().getItems().get(getIndex());
                            if (pelanggan != null) {
                                editPelanggan(pelanggan); // Panggil metode Edit
                            }
                        });
                        
                        btnDelete.setOnAction(event -> {
                            PelangganData pelanggan = getTableView().getItems().get(getIndex());
                            if (pelanggan != null) {
                                hapusPelanggan(pelanggan); // Panggil metode Hapus
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox actionButtons = new HBox(5, btnEdit, btnDelete);
                            actionButtons.setStyle("-fx-alignment: center;"); 
                            setGraphic(actionButtons);
                        }
                    }
                };
                return cell;
            }
        };

        colAction.setCellFactory(cellFactory);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}