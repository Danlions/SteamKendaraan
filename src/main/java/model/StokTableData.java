package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class StokTableData {
    private final SimpleIntegerProperty id_stok;
    private final SimpleStringProperty nama_barang;
    private final SimpleIntegerProperty jumlah;
    private final SimpleStringProperty satuan;
    private final SimpleStringProperty keterangan;
    private final SimpleStringProperty gambar;

    public StokTableData(int id_stok, String nama_barang, int jumlah, String satuan, String keterangan, String gambar) {
        this.id_stok = new SimpleIntegerProperty(id_stok);
        this.nama_barang = new SimpleStringProperty(nama_barang);
        this.jumlah = new SimpleIntegerProperty(jumlah);
        this.satuan = new SimpleStringProperty(satuan);
        this.keterangan = new SimpleStringProperty(keterangan);
        this.gambar = new SimpleStringProperty(gambar);
    }

    public int getIdStok() { return id_stok.get(); }
    public String getNamaBarang() { return nama_barang.get(); }
    public int getJumlah() { return jumlah.get(); }
    public String getSatuan() { return satuan.get(); }
    public String getKeterangan() { return keterangan.get(); }
    public String getGambar() { return gambar.get(); }

    // Property getters (opsional, untuk TableView jika masih digunakan)
    public SimpleIntegerProperty idStokProperty() { return id_stok; }
    public SimpleStringProperty namaBarangProperty() { return nama_barang; }
    public SimpleIntegerProperty jumlahProperty() { return jumlah; }
    public SimpleStringProperty satuanProperty() { return satuan; }
    public SimpleStringProperty keteranganProperty() { return keterangan; }
    public SimpleStringProperty gambarProperty() { return gambar; }
}