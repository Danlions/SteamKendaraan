package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PelangganData {

    private final StringProperty noTelp; 
    private final StringProperty namaPelanggan;

    public PelangganData(String noTelp, String namaPelanggan) {
        this.noTelp = new SimpleStringProperty(noTelp);
        this.namaPelanggan = new SimpleStringProperty(namaPelanggan);
    }

    // GETTER INI DIGUNAKAN UNTUK MENGAMBIL NILAI SAAT EDIT
    public String getNoTelp() {
        return noTelp.get();
    }

    public String getNamaPelanggan() {
        return namaPelanggan.get();
    }
    
    // (property methods lainnya)
    public StringProperty noTelpProperty() {
        return noTelp;
    }

    public StringProperty namaPelangganProperty() {
        return namaPelanggan;
    }
}