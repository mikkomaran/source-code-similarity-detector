package main.java.ee.ut.similaritydetector.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.ee.ut.similaritydetector.ui.controllers.MainViewController;

public class SimilarityDetectorLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../../../../../resources/ee/ut/similaritydetector/fxml/main_view.fxml"));
        primaryStage.setTitle("Source code similarity detector");
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        MainViewController.stage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
