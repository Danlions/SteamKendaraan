package model;

// Model data untuk TableView KelolaUser
public class UserTableData {
    private int id;
    private String nama;
    private String username;
    private int roleId;

    public UserTableData(int id, String nama, String username, int roleId) {
        this.id = id;
        this.nama = nama;
        this.username = username;
        this.roleId = roleId;
    }

    // Getter untuk TableView
    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getUsername() {
        return username;
    }

    // Setter (DIPERLUKAN untuk mengupdate nilai lokal setelah diubah di tabel)
    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    // Getter untuk Role ID (digunakan saat mengirim update ke database)
    public int getRoleId() {
        return roleId;
    }

    // Getter untuk menampilkan Role dalam bentuk teks
    public String getRoleText() {
        return switch (roleId) {
            case 1 -> "Admin";
            case 2 -> "Kasir";
            case 3 -> "Koordinator";
            default -> "NoRole";
        };
    }
    
    // Metode UTILITY: Mengkonversi Role (String) menjadi Role ID (Integer)
    public static int roleStringToId(String role) {
        return switch (role) {
            case "Admin" -> 1;
            case "Kasir" -> 2;
            case "Koordinator" -> 3;
            default -> 0; 
        };
    }
}