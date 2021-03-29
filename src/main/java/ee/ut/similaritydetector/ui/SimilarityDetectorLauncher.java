package main.java.ee.ut.similaritydetector.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.java.ee.ut.similaritydetector.ui.controllers.MainViewController;
import main.java.ee.ut.similaritydetector.ui.utils.UserData;

import java.io.IOException;
import java.util.Optional;

public class SimilarityDetectorLauncher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
       loadMainView(stage);
    }

    public void loadMainView(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("../../../../../resources/ee/ut/similaritydetector/fxml/main_view.fxml"));
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setTitle("Source code similarity detector");
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        stage.getIcons().add(new Image(getClass().getResourceAsStream("../../../../../resources/images/app_icon.png")));
        stage.show();
        stage.setUserData(new UserData());
        stage.setOnCloseRequest(this::showExitConfirmationAlert);
        MainViewController.stage = stage;
    }

    private void showExitConfirmationAlert(WindowEvent windowEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Results will not be saved.");
        ButtonType exitButton = new ButtonType("Exit");
        ButtonType cancelButton = ButtonType.CANCEL;
        alert.getButtonTypes().setAll(cancelButton, exitButton);
        // TODO: defauly button
        //Deactivate Defaultbehavior for yes-Button:
        Button yesButton = (Button) alert.getDialogPane().lookupButton(exitButton);
        yesButton.setDefaultButton(false);
        //Activate Defaultbehavior for no-Button:
        Button noButton = (Button) alert.getDialogPane().lookupButton(cancelButton);
        noButton.setDefaultButton(true);
        // Dark mode
        if (((UserData) MainViewController.stage.getUserData()).isDarkMode()) {
            alert.getDialogPane().getStylesheets().add(String.valueOf(this.getClass().getResource(
                    "../../../../../resources/ee/ut/similaritydetector/style/dark_mode.scss")));
        }
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == exitButton) {
            Platform.exit();
        } else {
            windowEvent.consume();
        }
    }

    @Override
    public void stop() throws Exception {
        /* TODO: siin teha asjad, mida sulgemisel vaja oleks:
                resources kausta kusutamine?,
                salvestada äkki ümber sarnased tööd kuhugi? */
        super.stop();
    }
}
