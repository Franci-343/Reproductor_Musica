package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.net.URL;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			Scene scene = new Scene(root);
			
			// Add CSS stylesheet
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Set scene fill to transparent for glassmorphism
			scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
			
			// Make window undecorated for custom title bar
			primaryStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
			
			// Load window icon from src/resources/Duke256.png (classpath: /resources/Duke256.png)
			URL iconUrl = getClass().getResource("/resources/Duke256.png");
			if (iconUrl != null) {
				primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
			} else {
				System.err.println("Icon resource not found: /resources/Duke256.png");
			}
			
			primaryStage.setScene(scene);
			primaryStage.setTitle("Reproductor de Música - Glassmorphism");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}