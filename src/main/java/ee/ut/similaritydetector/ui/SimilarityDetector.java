package main.java.ee.ut.similaritydetector.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.ee.ut.similaritydetector.ui.controllers.MainController;

public class SimilarityDetector extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../../../../../resources/ee/ut/similaritydetector/fxml/main.fxml"));
        primaryStage.setTitle("Source code similarity detector");
        Scene scene = new Scene(root, 1000, 700);
        //scene.getStylesheets().add(getClass().getResource("../../../../../resources/ee/ut/similaritydetector/style/style.scss").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.show();
        MainController.stage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
