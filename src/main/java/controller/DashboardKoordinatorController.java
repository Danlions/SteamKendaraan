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
import javafx.fxml.Initializable;
import java.util.ResourceBundle;

public class DashboardKoordinatorController implements Initializable {

    @FXML private Button btn_home;
    @FXML private Button stok;
    @FXML private Button btnLogout;
    
    @FXML private ImageView icon_home;
    @FXML private ImageView icon_stok;
    
    @FXML private AnchorPane scene;
    @FXML private Text username;

    private Button[] menuButtons;
    private ImageView[] menuIcons;
    
    private static final String FXML_HOME_PATH = "/view/HomeKoordinator.fxml";
    private static final String FXML_STOK_PATH = "/view/KelolaStokBarang.fxml"; 
    
    private static final String ACTIVE_CLASS = "active";
    private static final String DEFAULT_SUFFIX = ".png"; 
    private static final String ACTIVE_SUFFIX = "-active.png";
    private static final String BASE_ICON_PATH = "/assets/";
    
    private final Map<Button, String> BUTTON_TO_BASE_NAME = new HashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menuButtons = new Button[]{btn_home, stok};
        menuIcons = new ImageView[]{icon_home, icon_stok};

        BUTTON_TO_BASE_NAME.put(btn_home, "home");
        BUTTON_TO_BASE_NAME.put(stok, "stok"); 

        if (btn_home == null || stok == null) {
            System.err.println("❌ ERROR: Inisialisasi menu Koordinator gagal. Periksa kembali fx:id di FXML.");
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

            if (currentButton == null || currentIcon == null) continue;

            currentButton.pressedProperty().addListener((obs, wasPressed, isNowPressed) -> {
                if (currentButton.getStyleClass().contains(ACTIVE_CLASS)) {
                    return; 
                }
                changeIcon(currentIcon, isNowPressed); 
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
                System.err.println("❌ ERROR: Ikon TIDAK ditemukan di path: " + fullResourcePath);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR saat memuat ikon: " + fullResourcePath);
            e.printStackTrace();
        }
    }


    private void updateUserNameHeader() {
        UserData data = UserData.getInstance();
        if (data == null || data.getNama() == null) {
            username.setText("Guest Koordinator");
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
            //Path FXML salah (tidak ditemukan)
            System.err.println("❌ ERROR: File FXML konten tidak ditemukan di: " + fxmlPath);
            e.printStackTrace();
        } catch (IOException e) {
            //ClassNotFound di Controller FXML konten)
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
        } else if (clickedButton == stok) { // Cek fx:id 'stok' yang baru
            fxmlPath = FXML_STOK_PATH; 
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