package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.ee.ut.similaritydetector.backend.Analyser;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.ui.utils.IntegerStringConverter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class MainViewController {

    public static Stage stage;
    public static File zipDirectory;

    @FXML
    private MenuBarController menuBarController;

    // Settings
    @FXML
    private AnchorPane settingsPane;
    @FXML
    private CheckBox customSimilarityThresholdCheckbox;
    @FXML
    private Spinner<Integer> customSimilarityThresholdSpinner;
    @FXML
    private CheckBox preprocessCodeCheckbox;

    // File choosing
    @FXML
    private Button fileChooserButton;
    @FXML
    private Label fileNameLabel;
    @FXML
    private VBox fileArea;

    // Analyse progress
    @FXML
    private VBox progressArea;
    @FXML
    private Button startButton;
    @FXML
    private Label progressTextLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressPercentageLabel;

    public MainViewController() {
    }

    @FXML
    private void initialize() {
        startButton.setVisible(false);
        fileArea.setVisible(false);
        progressArea.setVisible(false);

        // Spinner restrictions and bindings
        customSimilarityThresholdSpinner.visibleProperty().bind(customSimilarityThresholdCheckbox.selectedProperty());
        customSimilarityThresholdSpinner.managedProperty().bind(customSimilarityThresholdSpinner.visibleProperty());
        customSimilarityThresholdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 100));
        IntegerStringConverter.createFor(customSimilarityThresholdSpinner);

        // Tooltip rules
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.setDismissDelay(10000);
        toolTipManager.setInitialDelay(600);
        toolTipManager.setReshowDelay(300);
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
        // Animates settings pane to disappear left
        hideSettings();

        fileArea.setVisible(false);
        startButton.setVisible(false);
        progressArea.setVisible(true);

        //Starts the backend similarity analysis on a new thread
        Analyser analyser;
        if (customSimilarityThresholdCheckbox.isSelected()){
            analyser = new Analyser(zipDirectory, customSimilarityThresholdSpinner.getValue() / 100.0, preprocessCodeCheckbox.isSelected());
        } else {
            analyser = new Analyser(zipDirectory, preprocessCodeCheckbox.isSelected());
        }
        progressBar.progressProperty().bind(analyser.progressProperty());
        progressPercentageLabel.textProperty().bind(analyser.progressProperty().multiply(100).asString("%.0f%%"));
        Thread analyserThread = new Thread(analyser, "analyser_thread");
        analyserThread.setDaemon(true);
        analyserThread.start();

        analyser.setOnSucceeded(workerStateEvent -> {
            try {
                openResultsView(analyser);
            } catch (IOException e) {
                System.out.println("Failed to open results view:\n" +  e.getMessage());
            }
        });

        // TODO: error message when analysis thread failed
        //analyser.setOnFailed();
    }

    private void hideSettings(){
        Duration duration = Duration.millis(500);
        Timeline timeline = new Timeline(
                new KeyFrame(duration,
                        new KeyValue(settingsPane.maxWidthProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(settingsPane.minWidthProperty(), 0, Interpolator.EASE_OUT)));
        timeline.play();
    }

    @FXML
    private void openResultsView(Analyser analyser) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../../../../../../resources/ee/ut/similaritydetector/fxml/results_view.fxml"));
        Parent root = loader.load();
        ResultsViewController controller = loader.getController();
        controller.setAnalyser(analyser);

        Scene resultsViewScene = new Scene(root, 1000, 700);
        stage.setScene(resultsViewScene);

        // Persists dark theme if it was activated before
        menuBarController.persistDarkTheme();

        // Makes the "View clusters" button clickable if analysis found any clusters
        controller.toggleClusterButtonUsability();

        stage.show();
    }

}
