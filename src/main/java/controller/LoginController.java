package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.DatabaseConnection;
import model.UserData;

public class LoginController {

    @FXML private TextField loginIdentifierField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private HBox warningBar;

    private UserDetails authenticatedUser = null; 
    
    private static class UserDetails {
        int idUser;
        String nama; 
        int roleId;
        
        public UserDetails(int idUser, String nama, int roleId) {
            this.idUser = idUser;
            this.nama = nama;
            this.roleId = roleId;
        }
    }

    @FXML
    public void initialize() {
        warningBar.setOpacity(0.0);
    }

    private void showWarning(String message) {
        statusLabel.setText(message);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), warningBar);
        fadeIn.setToValue(1.0);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), warningBar);
        fadeOut.setToValue(0.0);
        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.play();
    }

private void changeScene(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        Scene scene = new Scene(root);
        
        stage.setResizable(true); 
        
        stage.setScene(scene);

        stage.setMaximized(true); 
        
        stage.show();
        
        if (!stage.isMaximized()) {
            stage.centerOnScreen();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginIdentifierField.getText();
        String password = passwordField.getText();

        int role = authenticate(username, password);

        if (role == -1) {
            showWarning("Login gagal! Username atau password salah.");
            return;
        }
        
        String roleText = getRoleText(role);
        
        if (authenticatedUser != null) {
            UserData.createInstance(
                authenticatedUser.idUser, 
                authenticatedUser.nama, 
                roleText
            );
        }

        final String fxmlPath = switch (role) {
            case 1 -> "/view/DashboardAdmin.fxml";
            case 2 -> "/view/DashboardKasir.fxml";
            case 3 -> "/view/DashboardKoordinator.fxml";
            default -> "";
        };
        
        if (fxmlPath.isEmpty()) {
            showWarning("Error: Peran pengguna tidak valid.");
            return;
        }

        try {
            changeScene(event, fxmlPath);
        } catch (IOException ex) {
            System.err.println("Gagal memuat dashboard FXML: " + fxmlPath);
            ex.printStackTrace();
            showWarning("Error: Gagal memuat tampilan dashboard.");
        }
    }
    
    private String getRoleText(int roleId) {
        return switch (roleId) {
            case 1 -> "ADMIN";
            case 2 -> "KASIR";
            case 3 -> "KOORDINATOR CUCI"; 
            default -> "UNKNOWN";
        };
    }

    private int authenticate(String username, String password) {
        String sql = "SELECT id_user, nama, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                authenticatedUser = new UserDetails(
                    rs.getInt("id_user"), 
                    rs.getString("nama"), 
                    rs.getInt("role")
                );
                return rs.getInt("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showWarning("Error koneksi database.");
        }
        return -1;
    }
}