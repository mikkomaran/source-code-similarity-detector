package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.ee.ut.similaritydetector.backend.Analyser;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class MainViewController {

    public static Stage stage;
    public static File zipDirectory;

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
        if (zipDirectory != null) {
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
        try {
            analyserThread.join();
            openSideBySideView(analyser.getSimilarSolutionClusters());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openSideBySideView(List<SimilarSolutionCluster> clusters) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../../../../../../resources/ee/ut/similaritydetector/fxml/side_by_side_view.fxml"));
        Parent root = loader.load();
        SideBySideViewController controller = loader.getController();
        controller.setClusters(clusters);
        controller.createClusterItems();

        Scene sideBySideScene = new Scene(root, 1200, 700);
        stage.setScene(sideBySideScene);
        stage.show();

        // Resize cluster table columns
        controller.resizeClusterTableColumns();

        // Binds line numbers to move with code area's scrollbar
        controller.bindLineNumberVerticalScrollToCodeArea(controller.getLineNumbersLeft(), controller.getCodeAreaLeft());
        controller.bindLineNumberVerticalScrollToCodeArea(controller.getLineNumbersRight(), controller.getCodeAreaRight());
    }

}
