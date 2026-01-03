// util/LayananData.java
package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LayananData {
    private final IntegerProperty idLayanan;
    private final StringProperty namaLayanan;
    private final DoubleProperty harga;
    private final IntegerProperty jenisKendaraanId; // Untuk filter (1=Mobil, 2=Motor)

    public LayananData(int idLayanan, String namaLayanan, double harga, int jenisKendaraanId) {
        this.idLayanan = new SimpleIntegerProperty(idLayanan);
        this.namaLayanan = new SimpleStringProperty(namaLayanan);
        this.harga = new SimpleDoubleProperty(harga);
        this.jenisKendaraanId = new SimpleIntegerProperty(jenisKendaraanId);
    }

    // Getter untuk TableView
    public int getIdLayanan() {
        return idLayanan.get();
    }
    public String getNamaLayanan() {
        return namaLayanan.get();
    }
    public double getHarga() {
        return harga.get();
    }
    public int getJenisKendaraanId() {
        return jenisKendaraanId.get();
    }

    // Properti untuk Binding
    public IntegerProperty idLayananProperty() {
        return idLayanan;
    }
    public StringProperty namaLayananProperty() {
        return namaLayanan;
    }
    public DoubleProperty hargaProperty() {
        return harga;
    }
    public IntegerProperty jenisKendaraanIdProperty() {
        return jenisKendaraanId;
    }
    
    // Helper untuk menampilkan jenis kendaraan sebagai teks
    public String getJenisKendaraanText() {
        return jenisKendaraanId.get() == 1 ? "Mobil" : (jenisKendaraanId.get() == 2 ? "Motor" : "Lainnya");
    }
}