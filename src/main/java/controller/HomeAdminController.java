package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HomeAdminController {

    @FXML private BarChart<String, Number> barChartPendapatan;
    @FXML private StackedBarChart<Number, String> stackedBarPopularitas;
    @FXML private NumberAxis xAxisPersen;
    @FXML private CategoryAxis yAxisLayanan;
    @FXML private ComboBox<String> cbGlobalFilter;
    @FXML private Label lblTotalPendapatan, lblTotalPelanggan, lblTotalTrans;
    @FXML private Label lblCountMobil, lblCountMotor, lblCountProses, lblCountAntri;
    
    // Label untuk fungsi period (Growth)
    @FXML private Label lblGrowthPendapatan, lblGrowthPelanggan;

    private final String DB_URL = "jdbc:mysql://localhost:3306/steamkendaraan";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    @FXML
    public void initialize() {
        cbGlobalFilter.setItems(FXCollections.observableArrayList("Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini"));
        cbGlobalFilter.setValue("Bulan Ini");
        
        // Legenda di Kanan
        stackedBarPopularitas.setLegendVisible(true);
        stackedBarPopularitas.setLegendSide(Side.RIGHT);
        
        updateDashboard();
        cbGlobalFilter.setOnAction(e -> updateDashboard());
    }

    private void updateDashboard() {
        String filter = getSqlFilter(cbGlobalFilter.getValue());
        String prevFilter = getPrevSqlFilter(cbGlobalFilter.getValue());

        Thread t = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                loadSummary(conn, filter, prevFilter);
                loadBarChart(conn, cbGlobalFilter.getValue(), filter);
                loadPopularity(conn, filter);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void loadPopularity(Connection conn, String filter) throws SQLException {
        String sql = "SELECT l.nama_layanan, COUNT(t.id_transaksi) as jumlah " +
                     "FROM layanan l LEFT JOIN transaksi t ON l.id_layanan = t.id_layanan " +
                     "AND t.tanggal_transaksi " + filter + " GROUP BY l.nama_layanan";

        ResultSet rs = conn.createStatement().executeQuery(sql);
        List<DataPoint> points = new ArrayList<>();
        double total = 0;
        while (rs.next()) {
            int jml = rs.getInt("jumlah");
            points.add(new DataPoint(rs.getString("nama_layanan"), jml));
            total += jml;
        }

        final double finalTotal = total;
        ObservableList<XYChart.Series<Number, String>> chartData = FXCollections.observableArrayList();

        for (DataPoint p : points) {
            XYChart.Series<Number, String> series = new XYChart.Series<>();
            series.setName(p.key); 
            double percent = (finalTotal > 0) ? (p.val / finalTotal) * 100 : 0;
            XYChart.Data<Number, String> data = new XYChart.Data<>(percent, "Popularitas");
            
            if (percent > 0) {
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        Platform.runLater(() -> {
                            VBox labelContainer = new VBox(-1);
                            labelContainer.setAlignment(Pos.CENTER_LEFT);
                            labelContainer.setPadding(new Insets(0, 0, 0, 10)); 
                            Text txtCount = new Text(String.valueOf(p.val));
                            txtCount.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                            Text txtPercent = new Text(String.format("(%.0f%%)", percent));
                            txtPercent.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
                            labelContainer.getChildren().addAll(txtCount, txtPercent);
                            StackPane bar = (StackPane) newNode;
                            bar.getChildren().add(labelContainer);
                            StackPane.setAlignment(labelContainer, Pos.CENTER_LEFT);
                        });
                    }
                });
            }
            series.getData().add(data);
            chartData.add(series);
        }

        Platform.runLater(() -> {
            stackedBarPopularitas.getData().clear();
            stackedBarPopularitas.getData().addAll(chartData);
            
            stackedBarPopularitas.setMinHeight(120);
            
            yAxisLayanan.setTickLabelsVisible(false);
            yAxisLayanan.setOpacity(0);
            
            // --- BAGIAN PENGATURAN LEGENDA ---
            javafx.scene.Node legend = stackedBarPopularitas.lookup(".chart-legend");
            if (legend != null) {
                legend.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-border-color: transparent; " +
                    "-fx-padding: 10 10 10 30;"
                ); 
                
                if (legend instanceof javafx.scene.layout.TilePane) {
                    ((javafx.scene.layout.TilePane) legend).setVgap(10); 
                }
            }
        });
    }

    private void loadSummary(Connection conn, String filter, String prevFilter) throws SQLException {
        String sql = "SELECT SUM(total_harga), COUNT(*) FROM transaksi WHERE status='Selesai' AND tanggal_transaksi " + filter;
        ResultSet rs = conn.createStatement().executeQuery(sql);
        int harga = 0, transSelesai = 0;
        if (rs.next()) {
            harga = rs.getInt(1);
            transSelesai = rs.getInt(2);
        }

        String sqlPrev = "SELECT SUM(total_harga) FROM transaksi WHERE status='Selesai' AND tanggal_transaksi " + prevFilter;
        ResultSet rsP = conn.createStatement().executeQuery(sqlPrev);
        int hargaPrev = 0;
        if (rsP.next()) hargaPrev = rsP.getInt(1);

        int proses = 0, antri = 0, mobil = 0, motor = 0;
        ResultSet rsS = conn.createStatement().executeQuery("SELECT status, COUNT(*) FROM transaksi WHERE tanggal_transaksi "+filter+" GROUP BY status");
        while(rsS.next()){
            if("Sedang Dicuci".equalsIgnoreCase(rsS.getString(1))) proses = rsS.getInt(2);
            else if("Belum Dicuci".equalsIgnoreCase(rsS.getString(1))) antri = rsS.getInt(2);
        }
        ResultSet rsK = conn.createStatement().executeQuery("SELECT jenis_kendaraan, COUNT(*) FROM transaksi WHERE tanggal_transaksi "+filter+" GROUP BY jenis_kendaraan");
        while(rsK.next()){
            if("Mobil".equalsIgnoreCase(rsK.getString(1))) mobil = rsK.getInt(2);
            else if("Motor".equalsIgnoreCase(rsK.getString(1))) motor = rsK.getInt(2);
        }

        final int fH = harga, fT = transSelesai, fMo = mobil, fMt = motor, fPr = proses, fAn = antri, fHP = hargaPrev;

        Platform.runLater(() -> {
            lblTotalPendapatan.setText("Rp " + String.format("%,d", fH));
            lblTotalTrans.setText(String.valueOf(fT));
            lblCountMobil.setText(String.valueOf(fMo));
            lblCountMotor.setText(String.valueOf(fMt));
            lblCountProses.setText(String.valueOf(fPr));
            lblCountAntri.setText(String.valueOf(fAn));
            
            if(lblGrowthPendapatan != null) {
                int selisih = fH - fHP;
                double persen = (fHP == 0) ? 100 : ((double) selisih / fHP) * 100;
                String sign = (selisih >= 0) ? "+" : "";
                lblGrowthPendapatan.setText(String.format("%sRp %,d (%s%.1f%%)", sign, Math.abs(selisih), sign, persen));
            }
            
            try (Connection c2 = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                ResultSet rsTotalP = c2.createStatement().executeQuery("SELECT COUNT(*) FROM pelanggan");
                if(rsTotalP.next()) lblTotalPelanggan.setText(rsTotalP.getString(1));
                
                String sqlBaru = "SELECT COUNT(*) FROM pelanggan WHERE tanggal_daftar " + filter;
                ResultSet rsBaru = c2.createStatement().executeQuery(sqlBaru);
                if(rsBaru.next() && lblGrowthPelanggan != null) {
                    lblGrowthPelanggan.setText("+" + rsBaru.getInt(1) + " Pelanggan Baru");
                }
            } catch (SQLException e) {}
        });
    }

    private void loadBarChart(Connection conn, String type, String filter) throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String format = type.equals("Tahun Ini") ? "%M" : "%d/%m";
        String sql = "SELECT DATE_FORMAT(tanggal_transaksi, '"+format+"') as t, SUM(total_harga) as s FROM transaksi " +
                     "WHERE status='Selesai' AND tanggal_transaksi " + filter + " GROUP BY t ORDER BY MIN(tanggal_transaksi)";
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) series.getData().add(new XYChart.Data<>(rs.getString("t"), rs.getInt("s")));
        Platform.runLater(() -> {
            barChartPendapatan.getData().clear();
            barChartPendapatan.getData().add(series);
        });
    }

    private String getSqlFilter(String v) {
        return switch (v) {
            case "Hari Ini" -> "= CURDATE()";
            case "Minggu Ini" -> ">= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            case "Bulan Ini" -> ">= DATE_FORMAT(CURDATE(), '%Y-%m-01')";
            case "Tahun Ini" -> ">= DATE_FORMAT(CURDATE(), '%Y-01-01')";
            default -> ">= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
        };
    }

    private String getPrevSqlFilter(String v) {
        return switch (v) {
            case "Hari Ini" -> "= DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
            case "Minggu Ini" -> "BETWEEN DATE_SUB(CURDATE(), INTERVAL 14 DAY) AND DATE_SUB(CURDATE(), INTERVAL 8 DAY)";
            case "Bulan Ini" -> "BETWEEN DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01') AND LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))";
            case "Tahun Ini" -> "BETWEEN DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 YEAR), '%Y-01-01') AND DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 YEAR), '%Y-12-31')";
            default -> "< DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
        };
    }

    private static class DataPoint {
        String key; int val;
        DataPoint(String k, int v) { this.key = k; this.val = v; }
    }
}