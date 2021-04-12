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
import ee.ut.similaritydetector.ui.utils.UserPreferences;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

import static ee.ut.similaritydetector.backend.SolutionParser.outputDirectoryPath;

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
        ResourceBundle langBundle = ResourceBundle.getBundle(MainViewController.resourceBundlePath, UserPreferences.getInstance().getLocale());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ee/ut/similaritydetector/fxml/main_view.fxml"));
        loader.setResources(langBundle);
        Parent root = loader.load();
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setTitle(langBundle.getString("app_name"));
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        stage.show();
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
        ResourceBundle langBundle = ResourceBundle.getBundle(MainViewController.resourceBundlePath, UserPreferences.getInstance().getLocale());
        alert.setHeaderText(langBundle.getString("exit_msg"));
        //alert.setContentText("Results might not have been saved.");
        ButtonType exitButtonType = ButtonType.OK;
        Button exitButton = (Button) alert.getDialogPane().lookupButton(exitButtonType);
        exitButton.setText(langBundle.getString("exit"));
        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText(langBundle.getString("cancel"));

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        // Dark mode
        if (UserPreferences.getInstance().isDarkMode()) {
            alert.getDialogPane().getStylesheets().add(String.valueOf(this.getClass().getResource(
                    "/ee/ut/similaritydetector/style/dark_mode.scss")));
        }
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == exitButtonType) {
            Platform.exit();
        } else {
            windowEvent.consume();
        }
    }

    /**
     * Deletes the files that are generated during runtime for analysis.
     */
    public static void deleteOutputFiles() {
        Platform.runLater(() -> {
            File outputDirectory = new File(outputDirectoryPath);
            if (deleteDirectory(outputDirectory)) {
                System.out.println("Deleted analysis files.");
            }
        });
    }

    /**
     * Adapted from: https://stackoverflow.com/questions/7768071/how-to-delete-directory-content-in-java [30.03.2021]
     *  Original author: NCode (https://stackoverflow.com/users/805569/ncode)
     *
     * @param directory the directory to delete
     */
    private static boolean deleteDirectory(File directory) {
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
        return directory.delete();
    }

    /**
     * Overrides the {@link Application#stop()} to delete the files generated on runtime before closing the application.
     */
    @Override
    public void stop() throws Exception {
        deleteOutputFiles();
        super.stop();
    }
}
