package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TransaksiData {
    
    // Properti Dasar
    private final IntegerProperty idTransaksi;
    private final StringProperty noTelp; 
    private final StringProperty platNomor;
    private final StringProperty jenisKendaraan;
    private final DoubleProperty totalHarga;
    private final StringProperty status;
    private final StringProperty tglTransaksi;
    
    // Properti Hasil Join (Lookup)
    private final StringProperty namaPelanggan; 
    private final StringProperty namaLayanan;
    private final StringProperty namaKasir;

    // --- Konstruktor Utama
    public TransaksiData(int idTransaksi, String noTelp, String namaPelanggan, String namaLayanan, 
                         String jenisKendaraan, String platNomor, double totalHarga, 
                         String namaKasir, String status, String tglTransaksi) {
        
        this.idTransaksi = new SimpleIntegerProperty(idTransaksi);
        this.noTelp = new SimpleStringProperty(noTelp);
        this.namaPelanggan = new SimpleStringProperty(namaPelanggan);
        this.namaLayanan = new SimpleStringProperty(namaLayanan);
        this.jenisKendaraan = new SimpleStringProperty(jenisKendaraan);
        this.platNomor = new SimpleStringProperty(platNomor);
        this.totalHarga = new SimpleDoubleProperty(totalHarga);
        this.namaKasir = new SimpleStringProperty(namaKasir);
        this.status = new SimpleStringProperty(status);
        this.tglTransaksi = new SimpleStringProperty(tglTransaksi);
    }

    // --- Konstruktor Khusus Koordinator
    // Digunakan untuk HomeKoordinator karena tidak memerlukan semua detail (harga, kasir, dll.)
    public TransaksiData(int idTransaksi, String namaPelanggan, String namaLayanan, 
                         String jenisKendaraan, String platNomor, String status) {
        
        this.idTransaksi = new SimpleIntegerProperty(idTransaksi);
        this.namaPelanggan = new SimpleStringProperty(namaPelanggan);
        this.namaLayanan = new SimpleStringProperty(namaLayanan);
        this.jenisKendaraan = new SimpleStringProperty(jenisKendaraan);
        this.platNomor = new SimpleStringProperty(platNomor);
        this.status = new SimpleStringProperty(status);
        
        // Inisialisasi properti yang tidak digunakan dengan nilai default/null
        this.noTelp = new SimpleStringProperty(null); 
        this.totalHarga = new SimpleDoubleProperty(0.0);
        this.namaKasir = new SimpleStringProperty(null);
        this.tglTransaksi = new SimpleStringProperty(null);
    }
    
    // --- Getter untuk CellValueFactory ---
    
    public int getIdTransaksi() { return idTransaksi.get(); }
    public IntegerProperty idTransaksiProperty() { return idTransaksi; }

    public String getNoTelp() { return noTelp.get(); }
    public StringProperty noTelpProperty() { return noTelp; }
    
    public String getNamaPelanggan() { return namaPelanggan.get(); }
    public StringProperty namaPelangganProperty() { return namaPelanggan; }

    public String getNamaLayanan() { return namaLayanan.get(); }
    public StringProperty namaLayananProperty() { return namaLayanan; }

    public String getJenisKendaraan() { return jenisKendaraan.get(); }
    public StringProperty jenisKendaraanProperty() { return jenisKendaraan; }

    public String getPlatNomor() { return platNomor.get(); }
    public StringProperty platNomorProperty() { return platNomor; }

    public double getTotalHarga() { return totalHarga.get(); }
    public DoubleProperty totalHargaProperty() { return totalHarga; }
    
    public String getNamaKasir() { return namaKasir.get(); }
    public StringProperty namaKasirProperty() { return namaKasir; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    
    public String getTglTransaksi() { return tglTransaksi.get(); }
    public StringProperty tglTransaksiProperty() { return tglTransaksi; }
}