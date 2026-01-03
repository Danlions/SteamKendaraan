package model;

public class NamaKendaraan {
    private int idLayanan;
    private String namaLayanan;
    private int idKendaraan; // 1 = Mobil, 2 = Motor
    private int harga;

    public NamaKendaraan(int idLayanan, String namaLayanan, int idKendaraan, int harga) {
        this.idLayanan = idLayanan;
        this.namaLayanan = namaLayanan;
        this.idKendaraan = idKendaraan;
        this.harga = harga;
    }

    public int getIdLayanan() { return idLayanan; }
    public String getNamaLayanan() { return namaLayanan; }
    public int getIdKendaraan() { return idKendaraan; }
    public int getHarga() { return harga; }

    @Override
    public String toString() {
        return namaLayanan;
    }
}
