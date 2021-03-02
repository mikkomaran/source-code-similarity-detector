package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import main.java.ee.ut.similaritydetector.backend.Analyser;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;


public class MainController {

    public static Window stage;
    public static Path zipDirectoryPath;

    //UI elements
    @FXML
    private Button fileChooserButton;
    @FXML
    private Label fileNameLabel;
    @FXML
    private VBox fileArea;

    @FXML
    public Button startButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressPercentageLabel;

    public MainController() {
    }

    @FXML
    private void initialize() {
        progressBar.setVisible(false);
        startButton.setVisible(false);
        fileArea.setVisible(false);
        progressPercentageLabel.setVisible(false);
    }

    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        fileChooser.getExtensionFilters().add(extFilter);
        if (zipDirectoryPath != null){
            fileChooser.setInitialDirectory(zipDirectoryPath.getParent().toFile());
        }
        File zipDirectory = fileChooser.showOpenDialog(stage);
        // If a zip file was selected
        if (zipDirectory != null) {
            zipDirectoryPath = zipDirectory.toPath();
            //System.out.println(zipDirectoryPath);
            fileNameLabel.setText(zipDirectoryPath.getFileName().toString());
            fileChooserButton.setVisible(false);
            fileArea.setVisible(true);
            startButton.setVisible(true);
        }
    }

    @FXML
    private void startAnalysis() {
        progressBar.setVisible(true);
        progressPercentageLabel.setVisible(true);
        startButton.setVisible(false);

        //Starts the backend similarity analysis on a new thread
        Analyser analyser = new Analyser(zipDirectoryPath);
        progressBar.progressProperty().bind(analyser.progressProperty());
        Thread analyserThread = new Thread(analyser, "analyser_thread");
        analyserThread.setDaemon(true);
        analyserThread.start();
    }

}
