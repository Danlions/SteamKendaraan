package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import model.DatabaseConnection;
import model.TransaksiData;

public class HomeKoordinatorController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private ComboBox<String> cmbFilterKendaraan; 
    @FXML private ComboBox<String> cmbFilterLayanan;   
    @FXML private Button btnReset;

    @FXML private TableView<TransaksiData> transaksiTable;
    @FXML private TableColumn<TransaksiData, Integer> colIdTransaksi;
    @FXML private TableColumn<TransaksiData, String> colNamaPelanggan;
    @FXML private TableColumn<TransaksiData, String> colJenisLayanan;
    @FXML private TableColumn<TransaksiData, String> colJenisKendaraan;
    @FXML private TableColumn<TransaksiData, String> colPlatNomor;
    @FXML private TableColumn<TransaksiData, String> colStatus;
    @FXML private TableColumn<TransaksiData, Void> colAction; 

    private ObservableList<TransaksiData> masterData = FXCollections.observableArrayList();
    private FilteredList<TransaksiData> filteredData;
    private SortedList<TransaksiData> sortedData;

    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "Semua Status", "Belum Dicuci", "Sedang Dicuci", "Selesai"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeColumns();
        loadDataFromDatabase();
        setupFilterOptions(); 
        setupFiltering();
    }

    private void initializeColumns() {
        colIdTransaksi.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        colNamaPelanggan.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colJenisLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colJenisKendaraan.setCellValueFactory(new PropertyValueFactory<>("jenisKendaraan"));
        colPlatNomor.setCellValueFactory(new PropertyValueFactory<>("platNomor"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        transaksiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colAction.setCellFactory(param -> new TableCell<TransaksiData, Void>() {
            private final Button editButton = new Button();
            {
                try {
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/assets/edit.png")));
                    editIcon.setFitHeight(18);
                    editIcon.setFitWidth(18);
                    editButton.setGraphic(editIcon);
                } catch (Exception e) {
                    editButton.setText("Edit");
                }
                editButton.getStyleClass().add("icon-button"); 
                editButton.setOnAction((ActionEvent event) -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        TransaksiData data = getTableView().getItems().get(getIndex());
                        handleEditTransaksi(data);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                    setAlignment(Pos.CENTER); 
                }
            }
        });
    }

    private void setupFilterOptions() {
        cmbFilterStatus.setItems(statusOptions);
        cmbFilterStatus.getSelectionModel().selectFirst(); 
        loadUniqueJenisKendaraan(); 
        loadAllLayananFromDatabase(); 
    }
    
    private void loadAllLayananFromDatabase() {
        // Mengubah "Layanan" menjadi "Semua Layanan"
        ObservableList<String> options = FXCollections.observableArrayList("Semua Layanan"); 
        String SQL = "SELECT nama_layanan FROM layanan ORDER BY nama_layanan ASC"; 
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                options.add(rs.getString("nama_layanan"));
            }
        } catch (SQLException e) { }
        cmbFilterLayanan.setItems(options);
        cmbFilterLayanan.getSelectionModel().selectFirst();
    }
    
    private void loadUniqueJenisKendaraan() {
        ObservableList<String> options = FXCollections.observableArrayList("Semua Kendaraan"); 
        masterData.stream()
                  .map(TransaksiData::getJenisKendaraan)
                  .distinct()
                  .sorted()
                  .filter(s -> s != null && !s.isEmpty()) 
                  .forEach(options::add);
        cmbFilterKendaraan.setItems(options);
        cmbFilterKendaraan.getSelectionModel().selectFirst();
    }
    
    private void loadDataFromDatabase() {
        masterData.clear();
        String SQL = "SELECT t.id_transaksi, p.nama_pelanggan, l.nama_layanan, " +
                     "t.jenis_kendaraan, t.plat_nomor, t.status " +
                     "FROM transaksi t " +
                     "LEFT JOIN pelanggan p ON t.no_Telp = p.no_telp " + 
                     "LEFT JOIN layanan l ON t.id_layanan = l.id_layanan " +
                     "ORDER BY t.id_transaksi DESC"; 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                masterData.add(new TransaksiData(
                    rs.getInt("id_transaksi"),
                    rs.getString("nama_pelanggan") != null ? rs.getString("nama_pelanggan") : "Guest", 
                    rs.getString("nama_layanan") != null ? rs.getString("nama_layanan") : "-",   
                    rs.getString("jenis_kendaraan"),
                    rs.getString("plat_nomor"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) { }
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(masterData, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(transaksiTable.comparatorProperty()); 
        
        transaksiTable.setItems(sortedData);
        
        txtSearch.textProperty().addListener((obs, old, newValue) -> applyFilters());
        cmbFilterStatus.valueProperty().addListener((obs, old, newValue) -> applyFilters());
        cmbFilterKendaraan.valueProperty().addListener((obs, old, newValue) -> applyFilters());
        cmbFilterLayanan.valueProperty().addListener((obs, old, newValue) -> applyFilters());
    }
    
    private void applyFilters() {
        filteredData.setPredicate(transaksi -> {
            String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase();
            String status = cmbFilterStatus.getValue();
            String kendaraan = cmbFilterKendaraan.getValue();
            String layanan = cmbFilterLayanan.getValue();
            
            if (!search.isEmpty()) {
                boolean match = String.valueOf(transaksi.getIdTransaksi()).contains(search) ||
                                (transaksi.getPlatNomor() != null && transaksi.getPlatNomor().toLowerCase().contains(search)) ||
                                (transaksi.getNamaPelanggan() != null && transaksi.getNamaPelanggan().toLowerCase().contains(search));
                if (!match) return false;
            }
            
            if (status != null && !status.equals("Semua Status") && !transaksi.getStatus().equals(status)) return false;
            if (kendaraan != null && !kendaraan.equals("Semua Kendaraan") && !transaksi.getJenisKendaraan().equals(kendaraan)) return false;
            if (layanan != null && !layanan.equals("Semua Layanan") && !transaksi.getNamaLayanan().equals(layanan)) return false;
            
            return true;
        });
    }

    @FXML
    private void handleReset() {
        txtSearch.clear();
        cmbFilterStatus.getSelectionModel().selectFirst();
        cmbFilterKendaraan.getSelectionModel().selectFirst();
        cmbFilterLayanan.getSelectionModel().selectFirst();
        applyFilters();
    }

    private void handleEditTransaksi(TransaksiData data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UpdateStatus.fxml"));
            AnchorPane root = loader.load();
            UpdateStatusController controller = loader.getController();
            
            controller.initData(data, () -> {
                loadDataFromDatabase(); 
                applyFilters(); 
            }); 

            Stage stage = new Stage();
            stage.setTitle("Update Status: " + data.getPlatNomor());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}