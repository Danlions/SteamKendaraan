package model;

public class UserData {
    private static UserData instance;
    private int idUser;
    private String nama;
    // Menyimpan role dalam bentuk teks (misal: "Admin", "Kasir", "Koordinator")
    private String role;      

    // Constructor Private
    private UserData(int idUser, String nama, String role) {
        this.idUser = idUser;
        this.nama = nama;
        this.role = role;
    }

    /**
     * Metode yang dipanggil setelah LOGIN berhasil untuk mencatat sesi.
     */
    public static void createInstance(int idUser, String nama, String role) {
        // Hapus instance lama dan buat yang baru dengan data user yang login
        instance = new UserData(idUser, nama, role);
    }

    /**
     * Mengambil data user yang sedang aktif saat ini.
     */
    public static UserData getInstance() {
        return instance;
    }
    
    // --- GETTER (Pintu akses untuk Controller) ---

    public int getIdUser() {
        return idUser;
    }
    
    public String getRole() {
        return role;
    }
    
    public String getNama() {
        return nama;
    }

    /**
     * Menghapus sesi (digunakan saat Logout).
     */
    public static void cleanData() {
        instance = null;
    }
}