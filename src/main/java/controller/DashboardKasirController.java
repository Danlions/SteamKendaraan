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

public class DashboardKasirController {

    @FXML private Button btn_home;
    @FXML private Button DaftarPelanggan; 
    @FXML private Button RiwayatTransaksi; 
    @FXML private Button btnLogout;
    
    @FXML private ImageView icon_home; 
    @FXML private ImageView icon_daftarPelanggan; 
    @FXML private ImageView icon_riwayatTransaksi; 
    
    @FXML private AnchorPane scene; 
    @FXML private Text username;


    private Button[] menuButtons;
    private ImageView[] menuIcons;
    
    private static final String FXML_HOME_PATH = "/view/HomeKasir.fxml"; 
    private static final String FXML_PELANGGAN_PATH = "/view/DaftarPelanggan.fxml";
    private static final String FXML_RIWAYAT_PATH = "/view/RiwayatTransaksi.fxml"; 
    
    private static final String ACTIVE_CLASS = "active";
    private static final String DEFAULT_SUFFIX = ".png"; 
    private static final String ACTIVE_SUFFIX = "-active.png";
    private static final String BASE_ICON_PATH = "/assets/";
    
    private final Map<Button, String> BUTTON_TO_BASE_NAME = new HashMap<>();


    @FXML
    public void initialize() {
        // Inisialisasi array dengan komponen FXML yang dipastikan ada di FXML Kasir
        menuButtons = new Button[]{btn_home, DaftarPelanggan, RiwayatTransaksi};
        menuIcons = new ImageView[]{icon_home, icon_daftarPelanggan, icon_riwayatTransaksi};

        // 2. Inisialisasi Map Ikon
        BUTTON_TO_BASE_NAME.put(btn_home, "home");
        BUTTON_TO_BASE_NAME.put(DaftarPelanggan, "daftarPelanggan"); 
        BUTTON_TO_BASE_NAME.put(RiwayatTransaksi, "riwayatTransaksi"); 

        // PENTING: Pengecekan Null untuk memastikan FXML terisi sebelum diproses
        if (btn_home == null || DaftarPelanggan == null || RiwayatTransaksi == null) {
            System.err.println("❌ ERROR: Inisialisasi menu gagal. Periksa kembali fx:id di FXML.");
            return;
        }

        updateUserNameHeader();
        setupPressListeners(); 

        // Muat Home sebagai tampilan default
        loadUI(FXML_HOME_PATH);
        setButtonActive(btn_home);
    }
    
    
    private void setupPressListeners() {
        if (menuButtons == null) return;
        
        for (int i = 0; i < menuButtons.length; i++) {
            Button currentButton = menuButtons[i];
            ImageView currentIcon = menuIcons[i];

            // Pengecekan ini vital, jika ada tombol yang null, looping dilewati
            if (currentButton == null || currentIcon == null) continue;

            currentButton.pressedProperty().addListener((obs, wasPressed, isNowPressed) -> {
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


    private void changeIcon(ImageView icon, boolean isActive) {
        int index = Arrays.asList(menuIcons).indexOf(icon);
        if (index == -1) return;
        
        Button button = menuButtons[index];
        String baseName = BUTTON_TO_BASE_NAME.get(button);
        if (baseName == null) return;

        String targetFileName = baseName + (isActive ? ACTIVE_SUFFIX : DEFAULT_SUFFIX);
        String fullResourcePath = BASE_ICON_PATH + targetFileName;

        try {
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
        if (data == null || data.getNama() == null) {
            username.setText("Guest User");
            return;
        }
        username.setText(data.getNama());
    }
    
    private void setButtonActive(Button targetButton) {
        if (menuButtons == null) return;
        
        for (int i = 0; i < menuButtons.length; i++) {
            Button btn = menuButtons[i];
            ImageView icon = menuIcons[i];
            
            if (btn == null || icon == null) continue;

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
            System.err.println("❌ ERROR: File FXML konten tidak ditemukan di: " + fxmlPath);
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
        
        if (clickedButton == btn_home) {
            fxmlPath = FXML_HOME_PATH;
        } else if (clickedButton == DaftarPelanggan) {
            fxmlPath = FXML_PELANGGAN_PATH;
        } else if (clickedButton == RiwayatTransaksi) {
            fxmlPath = FXML_RIWAYAT_PATH;
        } else {
            return;
        }
        
        loadUI(fxmlPath);
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