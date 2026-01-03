package controller;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
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
import javafx.stage.FileChooser;

import model.TransaksiData; 
import model.DatabaseConnection; 

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.UnitValue;

public class LaporanTransaksiController implements Initializable {

    @FXML private TextField txtNamaPelangganSearch; 
    @FXML private ComboBox<String> cmbJenisLayananFilter, cmbJenisKendaraanFilter, cmbFilterBulanTabel, cmbFilterTahun;
    @FXML private ComboBox<String> cmbBulan1, cmbBulan2;
    @FXML private DatePicker dpTanggalFilter;
    @FXML private Label lblTotalBulan1, lblTotalBulan2, lblPersentase, lblSelisihHarga;
    @FXML private Button btnReset, btnExport;
    
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
        loadDataFromDatabase(); // Load data untuk mendapatkan list tahun
        loadComboBoxOptions();
        
        filteredData = new FilteredList<>(masterData, p -> true);
        transaksiTable.setItems(filteredData);
        transaksiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Filter Table Listeners
        txtNamaPelangganSearch.textProperty().addListener((obs, old, val) -> applyFilters());
        cmbJenisLayananFilter.valueProperty().addListener((obs, old, val) -> applyFilters());
        cmbJenisKendaraanFilter.valueProperty().addListener((obs, old, val) -> applyFilters());
        cmbFilterBulanTabel.valueProperty().addListener((obs, old, val) -> applyFilters());
        cmbFilterTahun.valueProperty().addListener((obs, old, val) -> {
            applyFilters();
            calculateComparison(); // Update dashboard saat tahun ganti
        });
        dpTanggalFilter.valueProperty().addListener((obs, old, val) -> applyFilters());

        // Comparison Listeners
        cmbBulan1.valueProperty().addListener((obs, old, val) -> calculateComparison());
        cmbBulan2.valueProperty().addListener((obs, old, val) -> calculateComparison());
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
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText("Rp " + rupiahFormat.format(price));
            }
        });
    }

    private void loadComboBoxOptions() {
        // Setup Bulan
        ObservableList<String> bulanList = FXCollections.observableArrayList(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni", 
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        );
        cmbFilterBulanTabel.setItems(FXCollections.observableArrayList("Semua Bulan"));
        cmbFilterBulanTabel.getItems().addAll(bulanList);
        cmbFilterBulanTabel.getSelectionModel().selectFirst();
        
        cmbBulan1.setItems(bulanList);
        cmbBulan2.setItems(bulanList);

        // Setup Tahun secara dinamis dari data yang ada di database
        TreeSet<String> tahunSet = new TreeSet<>((a, b) -> b.compareTo(a)); // Urutan terbaru ke terlama
        for (TransaksiData d : masterData) {
            String tahun = d.getTglTransaksi().split("-")[2];
            tahunSet.add(tahun);
        }
        
        ObservableList<String> listTahun = FXCollections.observableArrayList("Semua Tahun");
        listTahun.addAll(tahunSet);
        cmbFilterTahun.setItems(listTahun);
        cmbFilterTahun.getSelectionModel().selectFirst();

        // Setup Layanan & Kendaraan dari DB
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
            // Search Text
            String search = txtNamaPelangganSearch.getText().toLowerCase();
            if (!search.isEmpty() && !t.getNamaPelanggan().toLowerCase().contains(search) && 
                !t.getNoTelp().contains(search) && !t.getPlatNomor().toLowerCase().contains(search)) return false;

            // Combo Filters
            if (!checkCombo(cmbJenisLayananFilter, t.getNamaLayanan())) return false;
            if (!checkCombo(cmbJenisKendaraanFilter, t.getJenisKendaraan())) return false;

            // Filter Bulan
            if (cmbFilterBulanTabel.getValue() != null && !cmbFilterBulanTabel.getValue().equals("Semua Bulan")) {
                int bulanData = Integer.parseInt(t.getTglTransaksi().split("-")[1]);
                int bulanIdx = cmbFilterBulanTabel.getSelectionModel().getSelectedIndex();
                if (bulanData != bulanIdx) return false;
            }

            // Filter Tahun
            if (cmbFilterTahun.getValue() != null && !cmbFilterTahun.getValue().equals("Semua Tahun")) {
                String tahunData = t.getTglTransaksi().split("-")[2];
                if (!tahunData.equals(cmbFilterTahun.getValue())) return false;
            }

            // Filter Tanggal Exact
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

    private void calculateComparison() {
        if (cmbBulan1.getValue() == null || cmbBulan2.getValue() == null) return;
        
        // Perbandingan harus berdasarkan tahun yang dipilih
        String tahunTerpilih = cmbFilterTahun.getValue();
        if (tahunTerpilih == null || tahunTerpilih.equals("Semua Tahun")) {
            tahunTerpilih = String.valueOf(LocalDate.now().getYear());
        }

        final String finalTahun = tahunTerpilih;
        double total1 = sumMonthlyIncome(cmbBulan1.getSelectionModel().getSelectedIndex() + 1, finalTahun);
        double total2 = sumMonthlyIncome(cmbBulan2.getSelectionModel().getSelectedIndex() + 1, finalTahun);

        lblTotalBulan1.setText("Rp " + rupiahFormat.format(total1));
        lblTotalBulan2.setText("Rp " + rupiahFormat.format(total2));

        double selisih = total2 - total1;
        double persen = (total1 == 0) ? 0 : (selisih / total1) * 100;
        
        lblSelisihHarga.setText("Selisih: Rp " + rupiahFormat.format(Math.abs(selisih)));
        lblPersentase.setText(String.format("%.1f%%", Math.abs(persen)));

        if (selisih > 0) {
            lblPersentase.setStyle("-fx-text-fill: #27ae60;");
            lblPersentase.setText("↑ " + lblPersentase.getText());
        } else if (selisih < 0) {
            lblPersentase.setStyle("-fx-text-fill: #e74c3c;");
            lblPersentase.setText("↓ " + lblPersentase.getText());
        } else {
            lblPersentase.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    private double sumMonthlyIncome(int month, String year) {
        return masterData.stream()
            .filter(t -> {
                String[] parts = t.getTglTransaksi().split("-");
                return Integer.parseInt(parts[1]) == month && parts[2].equals(year);
            })
            .mapToDouble(TransaksiData::getTotalHarga)
            .sum();
    }

    private void loadDataFromDatabase() {
        masterData.clear();
        String sql = "SELECT t.*, p.nama_pelanggan, l.nama_layanan, u.nama AS nama_kasir " +
                     "FROM transaksi t JOIN pelanggan p ON t.no_Telp = p.no_telp " +
                     "JOIN layanan l ON t.id_layanan = l.id_layanan " +
                     "JOIN users u ON t.id_kasir = u.id_user ORDER BY t.id_transaksi DESC";
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
        // 1. Reset Field Pencarian dan Filter Tabel
        txtNamaPelangganSearch.clear();
        cmbJenisLayananFilter.getSelectionModel().selectFirst();
        cmbJenisKendaraanFilter.getSelectionModel().selectFirst();
        cmbFilterBulanTabel.getSelectionModel().selectFirst();
        cmbFilterTahun.getSelectionModel().selectFirst();
        dpTanggalFilter.setValue(null);
        
        // 2. Reset Dashboard (ComboBox Bulan Utama & Pembanding)
        cmbBulan1.getSelectionModel().clearSelection();
        cmbBulan2.getSelectionModel().clearSelection();
        
        // 3. Reset Label Dashboard ke angka nol/awal
        lblTotalBulan1.setText("Rp 0");
        lblTotalBulan2.setText("Rp 0");
        lblSelisihHarga.setText("Selisih: Rp 0");
        lblPersentase.setText("0%");
        lblPersentase.setStyle("-fx-text-fill: #7f8c8d;"); // Warna abu-abu netral
        
        // 4. Terapkan filter ulang ke tabel
        applyFilters();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        if (filteredData.isEmpty()) {
            showAlert("Peringatan", "Tidak ada data untuk diekspor!", AlertType.WARNING);
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Simpan Laporan");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"),
            new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"),
            new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf")
        );
        
        File file = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (file != null) {
            String extension = file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
            switch (extension) {
                case ".csv" -> exportToCSV(file);
                case ".xlsx" -> exportToExcel(file);
                case ".pdf" -> exportToPDF(file);
            }
        }
    }

    // --- LOGIKA EKSPOR ---

    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("ID,Tanggal,Pelanggan,Telepon,Plat Nomor,Layanan,Kendaraan,Total Harga,Kasir");
            for (TransaksiData d : filteredData) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%.0f,%s%n",
                    d.getIdTransaksi(), d.getTglTransaksi(), d.getNamaPelanggan(),
                    d.getNoTelp(), d.getPlatNomor(), d.getNamaLayanan(),
                    d.getJenisKendaraan(), d.getTotalHarga(), d.getNamaKasir());
            }
        } catch (IOException e) { showAlert("Error", e.getMessage(), AlertType.ERROR); }
    }

    private void exportToExcel(File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Laporan");
            String[] headers = {"ID", "Tanggal", "Pelanggan", "Telepon", "Plat Nomor", "Layanan", "Kendaraan", "Total Harga", "Kasir"};
            
            Row hRow = sheet.createRow(0);
            for(int i=0; i<headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = hRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowIdx = 1;
            for (TransaksiData d : filteredData) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getIdTransaksi());
                row.createCell(1).setCellValue(d.getTglTransaksi());
                row.createCell(2).setCellValue(d.getNamaPelanggan());
                row.createCell(3).setCellValue(d.getNoTelp());
                row.createCell(4).setCellValue(d.getPlatNomor());
                row.createCell(5).setCellValue(d.getNamaLayanan());
                row.createCell(6).setCellValue(d.getJenisKendaraan());
                row.createCell(7).setCellValue(d.getTotalHarga());
                row.createCell(8).setCellValue(d.getNamaKasir());
            }
            for(int i=0; i<headers.length; i++) sheet.autoSizeColumn(i);
            
            try (FileOutputStream out = new FileOutputStream(file)) { workbook.write(out); }
        } catch (Exception e) { showAlert("Error", e.getMessage(), AlertType.ERROR); }
    }

    private void exportToPDF(File file) {
        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {
            
            doc.add(new Paragraph("LAPORAN TRANSAKSI").setBold().setFontSize(18));
            doc.add(new Paragraph("Filter: " + cmbFilterTahun.getValue() + " | Dicetak: " + LocalDate.now()));
            doc.add(new Paragraph("\n"));
            
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 2, 2, 2, 2, 2})).useAllAvailableWidth();
            String[] headers = {"ID", "Tgl", "Plgn", "Plat", "Layanan", "Kndrn", "Total", "Kasir"};
            for(String h : headers) table.addHeaderCell(h);

            for (TransaksiData d : filteredData) {
                table.addCell(String.valueOf(d.getIdTransaksi()));
                table.addCell(d.getTglTransaksi());
                table.addCell(d.getNamaPelanggan());
                table.addCell(d.getPlatNomor());
                table.addCell(d.getNamaLayanan());
                table.addCell(d.getJenisKendaraan());
                table.addCell(rupiahFormat.format(d.getTotalHarga()));
                table.addCell(d.getNamaKasir());
            }
            doc.add(table);
        } catch (Exception e) { showAlert("Error", e.getMessage(), AlertType.ERROR); }
    }

    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}