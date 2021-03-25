package main.java.ee.ut.similaritydetector.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.java.ee.ut.similaritydetector.ui.controllers.MainViewController;

public class SimilarityDetectorLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../../../../../resources/ee/ut/similaritydetector/fxml/main_view.fxml"));
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setTitle("Source code similarity detector");
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("../../../../../resources/images/app_icon.png")));
        primaryStage.show();
        MainViewController.stage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        /* TODO: siin teha asjad, mida sulgemisel vaja oleks:
                resources kausta kusutamine?,
                salvestada äkki ümber sarnased tööd kuhugi? */
        super.stop();
    }
}
