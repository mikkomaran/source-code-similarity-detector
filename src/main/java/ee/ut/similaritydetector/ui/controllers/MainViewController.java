package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.ui.SimilarityDetectorLauncher;
import ee.ut.similaritydetector.ui.utils.UserPreferences;
import javafx.animation.*;
import javafx.application.Platform;
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
import ee.ut.similaritydetector.backend.Analyser;
import ee.ut.similaritydetector.ui.utils.IntegerStringConverter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ResourceBundle;

import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAlert;
import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAndWaitAlert;

public class MainViewController {

    public static final String resourceBundlePath = "ee.ut.similaritydetector.language.main_view";

    public static Stage stage;
    public static File zipDirectory;

    @FXML
    private MenuBarController menuBarController;

    // Options
    @FXML
    private AnchorPane optionsPane;
    @FXML
    private CheckBox customSimilarityThresholdCheckbox;
    @FXML
    private Spinner<Integer> customSimilarityThresholdSpinner;
    @FXML
    private CheckBox preprocessCodeCheckbox;
    @FXML
    private CheckBox anonymousResultsCheckbox;

    // File choosing
    @FXML
    private Button fileChooseButton;
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
        toolTipManager.setDismissDelay(20000);
        toolTipManager.setInitialDelay(500);
        toolTipManager.setReshowDelay(100);

        // Persists dark theme if it was activated before
        Platform.runLater(menuBarController::persistCurrentTheme);
    }

    /**
     * Opens a {@link FileChooser} when the "Select ZIP folder" is pressed.
     */
    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        ResourceBundle langBundle = ResourceBundle.getBundle(resourceBundlePath, UserPreferences.getInstance().getLocale());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(langBundle.getString("zip_descriptor"), "*.zip");
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
            fileChooseButton.setVisible(false);
            fileArea.setVisible(true);
            startButton.setVisible(true);
        }
    }

    /**
     * Starts the analysis process if the "Start analysis" button is clicked.
     * The {@link Analyser} is started on a new thread.
     */
    @FXML
    private void startAnalysis() {
        // Animates settings pane to disappear left
        hideOptions();

        fileArea.setVisible(false);
        startButton.setVisible(false);
        progressArea.setVisible(true);

        Analyser analyser;
        if (customSimilarityThresholdCheckbox.isSelected()){
            analyser = new Analyser(zipDirectory, customSimilarityThresholdSpinner.getValue() / 100.0,
                    preprocessCodeCheckbox.isSelected(), anonymousResultsCheckbox.isSelected(), this);
        } else {
            analyser = new Analyser(zipDirectory, preprocessCodeCheckbox.isSelected(), anonymousResultsCheckbox.isSelected(), this);
        }
        progressBar.progressProperty().bind(analyser.progressProperty());
        progressPercentageLabel.textProperty().bind(analyser.progressProperty().multiply(100).asString("%.0f%%"));
        ResourceBundle langBundle = ResourceBundle.getBundle(resourceBundlePath, UserPreferences.getInstance().getLocale());
        setProgressText(langBundle.getString("starting_analysis"));

        //Starts the backend similarity analysis on a new thread
        Thread analyserThread = new Thread(analyser, "analyser_thread");
        analyserThread.setDaemon(true);
        long startTime = System.nanoTime();
        analyserThread.start();

        analyser.setOnSucceeded(workerStateEvent -> {
            long endTime = System.nanoTime();
            BigDecimal duration = new BigDecimal(Double.toString(((double) endTime - startTime) / 1000000000));
            duration = duration.setScale(1, RoundingMode.HALF_UP);
            analyser.setAnalysisDuration(duration.doubleValue());
            if (analyser.getSimilarSolutionPairs().size() == 0) {
                showAlert(langBundle.getString("error_msg1"),
                        langBundle.getString("context_msg1"),
                        Alert.AlertType.INFORMATION);
                resetMainView();
                SimilarityDetectorLauncher.deleteOutputFiles();
                return;
            }
            try {
                openResultsView(analyser);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(langBundle.getString("error_msg2"),
                        langBundle.getString("context_msg2"),
                        Alert.AlertType.ERROR);
                resetMainView();
            }
        });

        analyser.setOnFailed(event -> {
            analyser.getException().printStackTrace();
            showAndWaitAlert(langBundle.getString("error_msg3"),
                    langBundle.getString("context_msg2"),
                    Alert.AlertType.ERROR);
            resetMainView();
            SimilarityDetectorLauncher.deleteOutputFiles();
        });
    }

    /**
     * Animates the closing of options pane.
     */
    private void hideOptions(){
        Duration duration = Duration.millis(300);
        optionsPane.setPrefWidth(optionsPane.getMinWidth());
        Timeline timeline = new Timeline(
                new KeyFrame(duration,
                        new KeyValue(optionsPane.maxWidthProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(optionsPane.minWidthProperty(), 0, Interpolator.EASE_OUT)));
        timeline.play();
    }

    /**
     * Animates the opening of options pane.
     */
    public void openOptions(){
        Duration duration = Duration.millis(300);
        Timeline timeline = new Timeline(
                new KeyFrame(duration,
                        new KeyValue(optionsPane.maxWidthProperty(), optionsPane.getPrefWidth(), Interpolator.EASE_OUT),
                        new KeyValue(optionsPane.minWidthProperty(), optionsPane.getPrefWidth(), Interpolator.EASE_OUT)));
        timeline.play();
    }

    /**
     * Changes the progress text that is displayed above the progress bar.
     *
     * @param text the text to be displayed
     */
    public void setProgressText(String text) {
        Platform.runLater(() -> progressTextLabel.setText(text));
    }

    /**
     * Resets the main view to the initial state.
     */
    private void resetMainView() {
        openOptions();

        fileArea.setVisible(true);
        startButton.setVisible(true);
        progressArea.setVisible(false);
    }

    /**
     * Opens the results view and passes the {@link Analyser} used for analysing solutions to the
     * {@link ResultsViewController}
     *
     * @param analyser {@link Analyser}
     * @throws IOException
     */
    @FXML
    private void openResultsView(Analyser analyser) throws IOException {
        ResourceBundle langBundle = ResourceBundle.getBundle(ResultsViewController.resourceBundlePath, UserPreferences.getInstance().getLocale());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ee/ut/similaritydetector/fxml/results_view.fxml"));
        loader.setResources(langBundle);
        Parent root = loader.load();
        ResultsViewController controller = loader.getController();
        controller.setAnalyser(analyser);
        controller.setMainViewController(this);
        Platform.runLater(controller::readStatistics);

        Scene resultsViewScene = new Scene(root, MainViewController.stage.getScene().getWidth(), MainViewController.stage.getScene().getHeight());
        stage.setScene(resultsViewScene);
        stage.setTitle(langBundle.getString("app_name") + " - " + langBundle.getString("results") + " - " + analyser.getZipDirectory().getName());

        // Makes the "View clusters" button clickable if analysis found any clusters
        controller.toggleClusterButtonUsability();

        stage.centerOnScreen();
        stage.show();
    }

}
