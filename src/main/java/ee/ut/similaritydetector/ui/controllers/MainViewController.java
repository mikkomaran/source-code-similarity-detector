package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.scene.layout.VBox;
import main.java.ee.ut.similaritydetector.backend.Analyser;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class MainViewController {

    public static Window stage;
    public static File zipDirectory;

    //UI elements
    @FXML
    private Button fileChooserButton;
    @FXML
    private Label fileNameLabel;
    @FXML
    private VBox fileArea;

    @FXML
    private Button startButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressPercentageLabel;

    public MainViewController() {
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
        if (zipDirectory != null){
            fileChooser.setInitialDirectory(zipDirectory.getParentFile());
        }
        File zipDirectory = fileChooser.showOpenDialog(stage);
        // If a zip file was selected
        if (zipDirectory != null) {
            MainViewController.zipDirectory = zipDirectory;
            //System.out.println(zipDirectoryPath);
            fileNameLabel.setText(MainViewController.zipDirectory.getName());
            fileChooserButton.setVisible(false);
            fileArea.setVisible(true);
            startButton.setVisible(true);
        }
    }

    @FXML
    private void startAnalysis() {
        fileArea.setVisible(false);
        progressBar.setVisible(true);
        progressPercentageLabel.setVisible(true);
        startButton.setVisible(false);

        //Starts the backend similarity analysis on a new thread
        Analyser analyser = new Analyser(zipDirectory);
        progressBar.progressProperty().bind(analyser.progressProperty());
        progressPercentageLabel.textProperty().bind(analyser.progressProperty().multiply(100).asString("%.0f%%"));
        Thread analyserThread = new Thread(analyser, "analyser_thread");
        analyserThread.setDaemon(true);
        analyserThread.start();

            // TODO: open results view and present the information
            // TODO: error message when analysis thread failed
    }

}
