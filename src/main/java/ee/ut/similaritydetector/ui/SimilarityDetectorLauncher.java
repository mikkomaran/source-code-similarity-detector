package ee.ut.similaritydetector.ui;

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
import ee.ut.similaritydetector.ui.controllers.MainViewController;
import ee.ut.similaritydetector.ui.utils.UserData;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class SimilarityDetectorLauncher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
       loadMainView(stage);
    }

    /**
     * Opens the main view or throws {@link IOException} if it fails to load.
     *
     * @param stage current {@link Stage}
     * @throws IOException if fails to load main_view.fxml
     */
    public void loadMainView(Stage stage) throws IOException {
        URL fxmlLocation = this.getClass().getResource("/ee/ut/similaritydetector/fxml/main_view.fxml");
        Parent root = FXMLLoader.load(fxmlLocation);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setTitle("Source code similarity detector");
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        stage.show();
        stage.setUserData(new UserData(true));
        stage.setOnCloseRequest(this::showExitConfirmationAlert);
        MainViewController.stage = stage;
    }

    /**
     * Shows a confirmation {@link Alert} if the main application window is attempted to close.
     *
     * @param windowEvent {@link WindowEvent}
     */
    private void showExitConfirmationAlert(WindowEvent windowEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Results might not have been saved.");
        ButtonType exitButton = new ButtonType("Exit");
        ButtonType cancelButton = ButtonType.CANCEL;
        alert.getButtonTypes().setAll(cancelButton, exitButton);
        // TODO: default button
        //Deactivate Defaultbehavior for yes-Button:
        Button yesButton = (Button) alert.getDialogPane().lookupButton(exitButton);
        yesButton.setDefaultButton(false);
        //Activate Defaultbehavior for no-Button:
        Button noButton = (Button) alert.getDialogPane().lookupButton(cancelButton);
        noButton.setDefaultButton(true);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        // Dark mode
        if (((UserData) MainViewController.stage.getUserData()).isDarkMode()) {
            alert.getDialogPane().getStylesheets().add(String.valueOf(this.getClass().getResource(
                    "/ee/ut/similaritydetector/style/dark_mode.scss")));
        }
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == exitButton) {
            Platform.exit();
        } else {
            windowEvent.consume();
        }
    }

    /**
     * Deletes the files that are generated during runtime for analysis.
     */
    public static void deleteOutputFiles() {
        File outputDirectory = new File("resources/");
        deleteDirectory(outputDirectory);
    }

    /**
     * Adapted from: https://stackoverflow.com/questions/7768071/how-to-delete-directory-content-in-java [30.03.2021]
     *  Original author: NCode (https://stackoverflow.com/users/805569/ncode)
     *
     * @param directory the directory to delete
     */
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if(files != null) {
            for(File f : files) {
                if(f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Overrides the {@link Application#stop()} to delete the files generated on runtime before closing the application.
     */
    @Override
    public void stop() throws Exception {
        /* TODO: siin teha asjad, mida sulgemisel vaja oleks:
                resources kausta kustutamine?,
                salvestada äkki ümber sarnased tööd kuhugi? */
        deleteOutputFiles();
        super.stop();
    }
}
