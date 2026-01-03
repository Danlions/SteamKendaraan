import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    private static URL getFxmlUrl(String fxmlFileName) {
        String fxmlResourcePath = "/view/" + fxmlFileName + ".fxml";
        URL url = Main.class.getResource(fxmlResourcePath);
        
        if (url == null) {
            System.err.println("--- KESALAHAN RESOURCE ---");
            System.err.println("Gagal memuat FXML: " + fxmlResourcePath);
            System.err.println("--------------------------");
        }
        return url;
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("STEAM - Sistem Aplikasi Layanan"); 

        URL fxmlUrl = getFxmlUrl("Login");
        if (fxmlUrl == null) {
            throw new IOException("Kritis: File FXML 'Login.fxml' tidak ditemukan.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);

        // Halaman Login biasanya tidak fullscreen agar rapi
        stage.setResizable(false); 
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}