package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Konfigurasi Database Anda
    private static final String DB_URL = "jdbc:mysql://localhost:3306/steamkendaraan"; // Ganti dengan nama DB Anda
    private static final String DB_USER = "root"; // Ganti dengan username DB Anda
    private static final String DB_PASSWORD = ""; // Ganti dengan password DB Anda

    /**
     * Membuat dan mengembalikan objek Connection ke database.
     * @return Connection objek koneksi.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Memuat driver JDBC (untuk Java versi lama, di versi baru opsional)
            // Class.forName("com.mysql.cj.jdbc.Driver"); 
            
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Koneksi database berhasil!");
            return connection;
        } catch (SQLException e) {
            System.err.println("Koneksi database GAGAL!");
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Menutup objek Connection.
     * @param connection Objek Connection yang akan ditutup.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Koneksi ditutup.");
            } catch (SQLException e) {
                System.err.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }
}