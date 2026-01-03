package controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.UserData;
import java.net.URL; 
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DashboardAdminController {

    // FXML IDs untuk Buttons
    @FXML private Button btn_home;
    @FXML private Button laporanTransaksi;    
    @FXML private Button kelolaLayanan;        
    @FXML private Button kelolaUser;            
    @FXML private Button btnLogout;
    
    // FXML IDs BARU untuk ImageViews
    @FXML private ImageView icon_home;
    @FXML private ImageView icon_report;
    @FXML private ImageView icon_management;
    @FXML private ImageView icon_userManagement;
    
    @FXML private AnchorPane scene;    
    
    @FXML private Text username;    

    private Button[] menuButtons;    
    private ImageView[] menuIcons;
    private static final String FXML_HOME_PATH = "/view/HomeAdmin.fxml";    
    private static final String ACTIVE_CLASS = "active";
    
    // Definisikan Sufiks File Gambar
    private static final String DEFAULT_SUFFIX = ".png"; 
    private static final String ACTIVE_SUFFIX = "-active.png";
    private static final String BASE_ICON_PATH = "/assets/"; 
    
    private static final Map<Button, String> BUTTON_TO_BASE_NAME = new HashMap<>();


    @FXML
    public void initialize() {
        menuButtons = new Button[]{btn_home, laporanTransaksi, kelolaLayanan, kelolaUser};
        menuIcons = new ImageView[]{icon_home, icon_report, icon_management, icon_userManagement};

        // Inisialisasi Map (PASTIKAN NAMA DASAR INI SESUAI DENGAN FILE IKON ANDA)
        BUTTON_TO_BASE_NAME.put(btn_home, "home");
        BUTTON_TO_BASE_NAME.put(laporanTransaksi, "report"); 
        BUTTON_TO_BASE_NAME.put(kelolaLayanan, "management"); 
        BUTTON_TO_BASE_NAME.put(kelolaUser, "userManagement"); 

        updateUserNameHeader();
        
        setupPressListeners(); // <-- LOGIKA BARU UNTUK WHEN PRESSED

        loadUI(FXML_HOME_PATH);
        setButtonActive(btn_home);
    }
    
    
     // Menambahkan listener untuk properti 'pressed' pada setiap tombol menu.
    private void setupPressListeners() {
        for (int i = 0; i < menuButtons.length; i++) {
            Button currentButton = menuButtons[i];
            ImageView currentIcon = menuIcons[i];

            currentButton.pressedProperty().addListener((obs, wasPressed, isNowPressed) -> {
                // Jangan lakukan apa-apa jika tombol sudah dalam status 'active' (sudah diklik)
                if (currentButton.getStyleClass().contains(ACTIVE_CLASS)) {
                    return; 
                }

                if (isNowPressed) {
                    // Pressed: Ganti ke gambar 'active'
                    changeIcon(currentIcon, true);
                } else {
                    // Released: Kembali ke gambar default
                    changeIcon(currentIcon, false);
                }
            });
        }
    }


    //Mengganti ImageView suatu tombol antara versi default dan aktif.
    private void changeIcon(ImageView icon, boolean isActive) {
        int index = Arrays.asList(menuIcons).indexOf(icon);
        if (index == -1) return;
        
        Button button = menuButtons[index];
        String baseName = BUTTON_TO_BASE_NAME.get(button);
        if (baseName == null) return;

        // 1. Tentukan nama file resource yang DITARGETKAN
        String targetFileName;
        if (isActive) {
            targetFileName = baseName + ACTIVE_SUFFIX; 
        } else {
            targetFileName = baseName + DEFAULT_SUFFIX; 
        }
        
        String fullResourcePath = BASE_ICON_PATH + targetFileName;

        try {
            // Muat gambar secara langsung menggunakan Class Loader
            URL resource = getClass().getResource(fullResourcePath);
            
            if (resource != null) {
                icon.setImage(new Image(resource.toExternalForm()));
            } else {
                System.err.println("❌ FATAL ERROR: Ikon TIDAK ditemukan di path: " + fullResourcePath);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR saat memuat ikon: " + fullResourcePath);
            e.printStackTrace();
        }
    }


    private void updateUserNameHeader() {
        UserData data = UserData.getInstance();
        if (data == null) {
            username.setText("Guest User");
            return;
        }
        String userName = data.getNama();
        username.setText(userName);
    }
    
    // Mengatur style CSS 'active' dan mengganti ikon untuk tombol yang ditargetkan.
    private void setButtonActive(Button targetButton) {
        for (int i = 0; i < menuButtons.length; i++) {
            Button btn = menuButtons[i];
            ImageView icon = menuIcons[i];
            
            // 1. Reset Style CSS
            btn.getStyleClass().remove(ACTIVE_CLASS);
            
            // 2. Reset Ikon ke Default 
            changeIcon(icon, false);
        }
        
        if (targetButton != null) {
            // 3. Set Style CSS Active
            targetButton.getStyleClass().add(ACTIVE_CLASS);
            
            // 4. Set Ikon ke Active 
            int index = Arrays.asList(menuButtons).indexOf(targetButton);
            if (index != -1) {
                changeIcon(menuIcons[index], true); 
            }
        }
    }
    
    private void loadUI(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = loader.load();
            
            scene.getChildren().clear();    
            scene.getChildren().add(newContent);

            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

        } catch (NullPointerException e) {
            System.err.println("❌ ERROR: File FXML tidak ditemukan di: " + fxmlPath);
            System.err.println("Pastikan file FXML yang diminta ada di folder resources/view/!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("❌ ERROR: Gagal memuat FXML konten: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMenuClick(ActionEvent event) {
        String fxmlPath;
        Button clickedButton = (Button) event.getSource();    
        
        // 1. Tentukan path FXML
        if (clickedButton == btn_home) {
            fxmlPath = FXML_HOME_PATH;    
        } else if (clickedButton == laporanTransaksi) {
            fxmlPath = "/view/LaporanTransaksi.fxml";    
        } else if (clickedButton == kelolaLayanan) {
            fxmlPath = "/view/KelolaLayanan.fxml";    
        } else if (clickedButton == kelolaUser) {
            fxmlPath = "/view/KelolaUser.fxml";    
        } else {
            return;
        }
        
        // 2. Muat FXML
        loadUI(fxmlPath);
        
        // 3. SET BUTTON YANG DIKLIK SEBAGAI AKTIF 
        setButtonActive(clickedButton);    
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            UserData.cleanData();
            
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Scene loginScene = new Scene(loginRoot);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.setMaximized(false);
            
            stage.setScene(loginScene);
            
            stage.setResizable(false);
            
            stage.show();
            stage.centerOnScreen();
            
        } catch (IOException e) {
            System.err.println("❌ ERROR: Gagal memuat tampilan Login.");
            e.printStackTrace();
        }
    }
}