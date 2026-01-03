package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import model.TransaksiData; 
import model.DatabaseConnection; 

public class RiwayatTransaksiController implements Initializable {

    @FXML private TextField txtNamaPelangganSearch; 
    @FXML private ComboBox<String> cmbJenisLayananFilter, cmbJenisKendaraanFilter, cmbFilterBulan, cmbFilterTahun;
    @FXML private DatePicker dpTanggalFilter; 
    @FXML private Button btnReset;
    
    @FXML private TableView<TransaksiData> transaksiTable;
    @FXML private TableColumn<TransaksiData, Integer> colIdTransaksi;
    @FXML private TableColumn<TransaksiData, String> colWaktuTransaksi, colNamaPelanggan, colPelangganTelp, colNamaLayanan, colJenisKendaraan, colPlatNomor, colKasir;
    @FXML private TableColumn<TransaksiData, Double> colTotalHarga; 

    private ObservableList<TransaksiData> masterData = FXCollections.observableArrayList();
    private FilteredList<TransaksiData> filteredData;
    private DecimalFormat rupiahFormat; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupRupiahFormat();
        initializeColumns();
        loadDataFromDatabase();
        loadComboBoxOptions();
        
        // Inisialisasi FilteredList
        filteredData = new FilteredList<>(masterData, p -> true);
        transaksiTable.setItems(filteredData); // Langsung set ke tabel tanpa pagination
        
        setupFilterListeners();
    }

    private void setupRupiahFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        rupiahFormat = new DecimalFormat("#,##0", symbols); 
    }

    private void initializeColumns() {
        colIdTransaksi.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        colWaktuTransaksi.setCellValueFactory(new PropertyValueFactory<>("tglTransaksi"));
        colNamaPelanggan.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colPelangganTelp.setCellValueFactory(new PropertyValueFactory<>("noTelp"));
        colNamaLayanan.setCellValueFactory(new PropertyValueFactory<>("namaLayanan"));
        colJenisKendaraan.setCellValueFactory(new PropertyValueFactory<>("jenisKendaraan"));
        colPlatNomor.setCellValueFactory(new PropertyValueFactory<>("platNomor"));
        colKasir.setCellValueFactory(new PropertyValueFactory<>("namaKasir"));
        colTotalHarga.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        
        colTotalHarga.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double totalHarga, boolean empty) {
                super.updateItem(totalHarga, empty);
                if (empty || totalHarga == null) setText(null);
                else setText("Rp " + rupiahFormat.format(totalHarga));
            }
        });
        
        transaksiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void setupFilterListeners() {
        txtNamaPelangganSearch.textProperty().addListener((obs, old, val) -> applyFilters());
        cmbJenisLayananFilter.valueProperty().addListener((obs, old, val) -> applyFilters());
        cmbJenisKendaraanFilter.valueProperty().addListener((obs, old, val) -> applyFilters());
        cmbFilterBulan.valueProperty().addListener((obs, old, val) -> applyFilters());
        cmbFilterTahun.valueProperty().addListener((obs, old, val) -> applyFilters());
        dpTanggalFilter.valueProperty().addListener((obs, old, val) -> applyFilters());
    }
    
    private void loadComboBoxOptions() {
        // Setup Bulan
        ObservableList<String> bulanList = FXCollections.observableArrayList(
            "Semua Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni", 
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        );
        cmbFilterBulan.setItems(bulanList);
        cmbFilterBulan.getSelectionModel().selectFirst();

        // Setup Tahun dinamis
        TreeSet<String> tahunSet = new TreeSet<>((a, b) -> b.compareTo(a)); 
        for (TransaksiData d : masterData) {
            String tahun = d.getTglTransaksi().split("-")[2];
            tahunSet.add(tahun);
        }
        ObservableList<String> listTahun = FXCollections.observableArrayList("Semua Tahun");
        listTahun.addAll(tahunSet);
        cmbFilterTahun.setItems(listTahun);
        cmbFilterTahun.getSelectionModel().selectFirst();

        // Setup Layanan & Kendaraan
        try (Connection conn = DatabaseConnection.getConnection()) {
            fetchToCombo(conn, "SELECT DISTINCT nama_layanan FROM layanan", cmbJenisLayananFilter, "Semua Layanan");
            fetchToCombo(conn, "SELECT DISTINCT nama_kendaraan FROM kendaraan", cmbJenisKendaraanFilter, "Semua Kendaraan");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void fetchToCombo(Connection conn, String sql, ComboBox<String> combo, String firstOption) throws SQLException {
        ObservableList<String> list = FXCollections.observableArrayList(firstOption);
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) list.add(rs.getString(1));
        combo.setItems(list);
        combo.getSelectionModel().selectFirst();
    }

    private void applyFilters() {
        filteredData.setPredicate(t -> {
            String search = txtNamaPelangganSearch.getText().toLowerCase();
            if (!search.isEmpty() && !t.getNamaPelanggan().toLowerCase().contains(search) && 
                !t.getNoTelp().contains(search) && !t.getPlatNomor().toLowerCase().contains(search)) return false;

            if (!checkCombo(cmbJenisLayananFilter, t.getNamaLayanan())) return false;
            if (!checkCombo(cmbJenisKendaraanFilter, t.getJenisKendaraan())) return false;

            if (cmbFilterBulan.getValue() != null && !cmbFilterBulan.getValue().equals("Semua Bulan")) {
                int bulanData = Integer.parseInt(t.getTglTransaksi().split("-")[1]);
                int bulanIdx = cmbFilterBulan.getSelectionModel().getSelectedIndex();
                if (bulanData != bulanIdx) return false;
            }

            if (cmbFilterTahun.getValue() != null && !cmbFilterTahun.getValue().equals("Semua Tahun")) {
                String tahunData = t.getTglTransaksi().split("-")[2];
                if (!tahunData.equals(cmbFilterTahun.getValue())) return false;
            }

            if (dpTanggalFilter.getValue() != null) {
                String dateStr = dpTanggalFilter.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                if (!t.getTglTransaksi().equals(dateStr)) return false;
            }

            return true; 
        });
    }

    private boolean checkCombo(ComboBox<String> combo, String value) {
        String sel = combo.getValue();
        return sel == null || sel.startsWith("Semua") || sel.equals(value);
    }

    private void loadDataFromDatabase() {
        masterData.clear();
        String sql = "SELECT t.*, p.nama_pelanggan, l.nama_layanan, u.nama AS nama_kasir " +
                     "FROM transaksi t JOIN pelanggan p ON t.no_Telp = p.no_telp " +
                     "JOIN layanan l ON t.id_layanan = l.id_layanan " +
                     "JOIN users u ON t.id_kasir = u.id_user ORDER BY t.tanggal_transaksi DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                masterData.add(new TransaksiData(
                    rs.getInt("id_transaksi"), rs.getString("no_Telp"), rs.getString("nama_pelanggan"),
                    rs.getString("nama_layanan"), rs.getString("jenis_kendaraan"), rs.getString("plat_nomor"),
                    rs.getDouble("total_harga"), rs.getString("nama_kasir"), rs.getString("status"),
                    rs.getDate("tanggal_transaksi").toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleReset(ActionEvent event) {
        txtNamaPelangganSearch.clear();
        cmbJenisLayananFilter.getSelectionModel().selectFirst();
        cmbJenisKendaraanFilter.getSelectionModel().selectFirst();
        cmbFilterBulan.getSelectionModel().selectFirst();
        cmbFilterTahun.getSelectionModel().selectFirst();
        dpTanggalFilter.setValue(null);
        applyFilters();
    }
}