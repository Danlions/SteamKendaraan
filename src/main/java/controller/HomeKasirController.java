package controller;

import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.DatabaseConnection;
import model.UserData;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class HomeKasirController implements Initializable {

    @FXML private TextField txtNoTelp, txtNamaPelanggan, txtPlatNomor, txtJenisKendaraan, txtHargaLayanan, txtTotalHargaPembayaran, txtJumlahBayar, txtKembalian;
    @FXML private ComboBox<String> cbLayanan, cbMetodePembayaran;
    @FXML private GridPane gridPembayaranCash;
    @FXML private VBox vboxQrCode;
    @FXML private ImageView imgQrCode;
    @FXML private Label statusLabel;

    private ContextMenu telpSuggestions = new ContextMenu();
    private ObservableList<LayananData> masterLayanan = FXCollections.observableArrayList();
    private boolean isDataSaved = false;

    private static class LayananData {
        int id; String nama, jenis; int harga;
        LayananData(int id, String nama, int harga, String jenis) {
            this.id = id; this.nama = nama; this.harga = harga; this.jenis = jenis;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadLayananFromDB();
        cbMetodePembayaran.setItems(FXCollections.observableArrayList("Cash", "Transfer"));
        setupAutoCompletionTelp();
        statusLabel.setText("");
    }

    private void loadLayananFromDB() {
        masterLayanan.clear();
        cbLayanan.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT l.id_layanan, l.nama_layanan, l.harga, k.nama_kendaraan " +
                         "FROM layanan l JOIN kendaraan k ON l.id_kendaraan = k.id_kendaraan";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                masterLayanan.add(new LayananData(
                    rs.getInt("id_layanan"), rs.getString("nama_layanan"), 
                    rs.getInt("harga"), rs.getString("nama_kendaraan")
                ));
                cbLayanan.getItems().add(rs.getString("nama_layanan"));
            }
        } catch (SQLException e) { 
            showStatus("Gagal memuat layanan", Color.RED);
        }
    }

    @FXML
    private void handleCariPelanggan() {
        String telp = txtNoTelp.getText();
        if (telp.isEmpty()) {
            txtNamaPelanggan.clear();
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT nama_pelanggan FROM pelanggan WHERE no_telp = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, telp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                txtNamaPelanggan.setText(rs.getString("nama_pelanggan"));
            } else {
                txtNamaPelanggan.setText("Pelanggan Baru");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    @FXML
    private void handleLayananSelected() {
        String sel = cbLayanan.getValue();
        if (sel == null) return;
        for (LayananData l : masterLayanan) {
            if (l.nama.equals(sel)) {
                txtJenisKendaraan.setText(l.jenis);
                txtHargaLayanan.setText(String.valueOf(l.harga));
                txtTotalHargaPembayaran.setText("Rp " + String.format("%,d", l.harga));
                handleHitungKembalian();
                break;
            }
        }
    }

    @FXML
    private void handleMetodePembayaranChanged() {
        boolean isTransfer = "Transfer".equals(cbMetodePembayaran.getValue());
        gridPembayaranCash.setVisible(!isTransfer);
        gridPembayaranCash.setManaged(!isTransfer);
        vboxQrCode.setVisible(isTransfer);
        vboxQrCode.setManaged(isTransfer);

        if (isTransfer) {
            try {
                InputStream is = getClass().getResourceAsStream("/assets/qrCode.jpeg");
                if (is != null) imgQrCode.setImage(new Image(is));
                txtJumlahBayar.setText(txtHargaLayanan.getText());
                txtKembalian.setText("LUNAS");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleHitungKembalian() {
        try {
            if (txtJumlahBayar.getText().isEmpty() || txtHargaLayanan.getText().isEmpty()) return;
            int bayar = Integer.parseInt(txtJumlahBayar.getText().replaceAll("[^0-9]", ""));
            int harga = Integer.parseInt(txtHargaLayanan.getText().replaceAll("[^0-9]", ""));
            int kembali = bayar - harga;
            txtKembalian.setText(kembali >= 0 ? "Rp " + String.format("%,d", kembali) : "Kurang");
        } catch (Exception e) { txtKembalian.setText("Rp 0"); }
    }

    @FXML
    private void handleCetakStruk() {
        if (isDataSaved) {
            showStatus("ℹ Transaksi sudah diproses. Silahkan Reset.", Color.BLUE);
            return;
        }

        if (txtNoTelp.getText().isEmpty() || txtPlatNomor.getText().isEmpty() || 
            cbLayanan.getValue() == null || cbMetodePembayaran.getValue() == null) {
            showStatus("⚠ Gagal: Data tidak lengkap!", Color.RED);
            return;
        }

        int idLayanan = -1;
        for (LayananData l : masterLayanan) {
            if (l.nama.equals(cbLayanan.getValue())) { idLayanan = l.id; break; }
        }

        String sql = "INSERT INTO transaksi (no_Telp, id_layanan, jenis_kendaraan, plat_nomor, id_kasir, tanggal_transaksi, total_harga, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtNoTelp.getText());
            pstmt.setInt(2, idLayanan);
            pstmt.setString(3, txtJenisKendaraan.getText());
            pstmt.setString(4, txtPlatNomor.getText());
            pstmt.setInt(5, UserData.getInstance().getIdUser()); 
            pstmt.setDate(6, java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.setInt(7, Integer.parseInt(txtHargaLayanan.getText()));
            pstmt.setString(8, "Belum Dicuci");

            if (pstmt.executeUpdate() > 0) {
                isDataSaved = true;
                showStatus("✔ Berhasil: Data Tersimpan!", Color.GREEN);
                prosesCetakKePrinter();
            }
        } catch (SQLException e) {
            showStatus("✖ Gagal Simpan: " + e.getMessage(), Color.RED);
        }
    }

    private void prosesCetakKePrinter() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // Menambahkan Jam Menit Detik
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String waktuSekarang = LocalDateTime.now().format(dtf);

            // Menyusun isi struk
            String s =
                "\n      STEAM KENDARAAN\n" +
                "--------------------------\n" +
                "Pelanggan : " + txtNamaPelanggan.getText() + "\n" +
                "No Telp   : " + txtNoTelp.getText() + "\n" +
                "Plat      : " + txtPlatNomor.getText() + "\n" +
                "Layanan   : " + cbLayanan.getValue() + "\n" +
                "Harga     : " + txtTotalHargaPembayaran.getText() + "\n" +
                "Waktu     : " + waktuSekarang + "\n" +
                "--------------------------\n" +
                "      Terima Kasih\n\n";

            Text textNode = new Text(s);

            textNode.setFont(new Font("Monospaced", 9)); 

            TextFlow flow = new TextFlow(textNode);
            flow.setTextAlignment(TextAlignment.LEFT);

            flow.setPrefWidth(200); 

            if (job.showPrintDialog(null)) {
                boolean success = job.printPage(flow);
                if (success) {
                    job.endJob();
                    showStatus("✔ Struk dicetak!", Color.GREEN);
                }
            }
        } else {
            showStatus("✖ Printer tidak terdeteksi!", Color.RED);
        }
    }

    @FXML
    private void handleReset() {
        txtNoTelp.clear(); txtNamaPelanggan.clear(); txtPlatNomor.clear();
        cbLayanan.getSelectionModel().clearSelection();
        cbMetodePembayaran.getSelectionModel().clearSelection();
        txtHargaLayanan.clear(); txtJenisKendaraan.clear();
        txtTotalHargaPembayaran.clear(); txtJumlahBayar.clear(); txtKembalian.clear();
        vboxQrCode.setVisible(false);
        gridPembayaranCash.setVisible(true);
        statusLabel.setText("");
        isDataSaved = false;
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(color);
    }

    private void setupAutoCompletionTelp() {
        txtNoTelp.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) { telpSuggestions.hide(); return; }
            telpSuggestions.getItems().clear();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) return;
                PreparedStatement ps = conn.prepareStatement("SELECT no_telp, nama_pelanggan FROM pelanggan WHERE no_telp LIKE ? LIMIT 5");
                ps.setString(1, newVal + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String t = rs.getString("no_telp");
                    String n = rs.getString("nama_pelanggan");
                    MenuItem item = new MenuItem(t + " (" + n + ")");
                    item.setOnAction(e -> {
                        txtNoTelp.setText(t);
                        txtNamaPelanggan.setText(n);
                        telpSuggestions.hide();
                    });
                    telpSuggestions.getItems().add(item);
                }
                if (!telpSuggestions.getItems().isEmpty()) {
                    telpSuggestions.show(txtNoTelp, javafx.geometry.Side.BOTTOM, 0, 0);
                }
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }
}