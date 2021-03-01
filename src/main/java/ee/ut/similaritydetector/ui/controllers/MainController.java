package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import main.java.ee.ut.similaritydetector.backend.Analyser;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;


public class MainController {

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
    public Button startButton;
    @FXML
    private ProgressBar progressBar;

    public MainController() {
    }

    @FXML
    private void initialize() {
        progressBar.setVisible(false);
        startButton.setVisible(false);
        fileArea.setVisible(false);
        //fileNameLabel.setVisible(false);
    }

    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        fileChooser.getExtensionFilters().add(extFilter);
        zipDirectory = fileChooser.showOpenDialog(stage);
        // If a zip file was selected
        if (zipDirectory != null) {
            System.out.println(zipDirectory);
            fileNameLabel.setText(zipDirectory.getName());
            fileChooserButton.setVisible(false);
            //fileNameLabel.setVisible(true);
            fileArea.setVisible(true);
            startButton.setVisible(true);
        }
    }

    @FXML
    private void startAnalysis() {
        progressBar.setVisible(true);

        //Starts the code analysis on the backend service
        Analyser analyser = new Analyser(zipDirectory);
        analyser.startAnalysis();

    }

}
