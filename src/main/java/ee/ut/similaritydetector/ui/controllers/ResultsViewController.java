package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.ui.utils.ExerciseStatistics;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ee.ut.similaritydetector.backend.Analyser;
import ee.ut.similaritydetector.backend.Exercise;

import java.io.IOException;

import static ee.ut.similaritydetector.ui.SimilarityDetectorLauncher.deleteOutputFiles;
import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAndWaitAlert;

public class ResultsViewController {

    private Analyser analyser;
    private MainViewController mainViewController;
    private Stage codeViewStage;

    @FXML
    private MenuBarController menuBarController;

    @FXML
    private Label title;

    @FXML
    private Label totalSolutionsLabel;
    @FXML
    private Label solutionPairsLabel;
    @FXML
    private Label analysisDurationLabel;

    @FXML
    private TableView<ExerciseStatistics> exerciseStatisticsTable;
    @FXML
    private TableColumn<ExerciseStatistics, String> exerciseNameColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> totalSolutionsColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> suspiciousSolutionsColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> similarPairsColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> similarClustersColumn;
    @FXML
    private TableColumn<ExerciseStatistics, String> similarityThresholdColumn;

    @FXML
    private Button viewClustersButton;
    @FXML
    private Button runNewAnalysisButton;


    public ResultsViewController() {
    }

    public void setAnalyser(Analyser analyser) {
        this.analyser = analyser;
    }

    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    @FXML
    private void initialize() {
        // Persists dark theme if it was activated before
        Platform.runLater(menuBarController::persistCurrentTheme);
    }

    /**
     * Loads the statistics from the {@link Analyser} onto the results view.
     */
    public void readStatistics() {
        title.setText("Results - " + analyser.getZipDirectory().getName());
        totalSolutionsLabel.setText(String.valueOf(analyser.getExercises().stream().mapToInt(Exercise::getSolutionCount).sum()));
        solutionPairsLabel.setText(String.valueOf(analyser.getAnalysedSolutionPairsCount()));
        analysisDurationLabel.setText(analyser.getAnalysisDuration() + " s");
        fillExerciseStatisticsTable();
        // To remove empty rows from the bottom of the table we have to set fixed cell
        int cellSize = 30;
        exerciseStatisticsTable.setFixedCellSize(cellSize);
        exerciseStatisticsTable.prefHeightProperty().bind(Bindings.size(exerciseStatisticsTable.getItems()).
                multiply(exerciseStatisticsTable.getFixedCellSize()).add(cellSize + 6));
    }

    private void fillExerciseStatisticsTable() {
        exerciseNameColumn.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        totalSolutionsColumn.setCellValueFactory(new PropertyValueFactory<>("totalSolutions"));
        suspiciousSolutionsColumn.setCellValueFactory(new PropertyValueFactory<>("suspiciousSolutions"));
        similarPairsColumn.setCellValueFactory(new PropertyValueFactory<>("similarPairs"));
        similarClustersColumn.setCellValueFactory(new PropertyValueFactory<>("similarClusters"));
        similarityThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("similarityThreshold"));

        analyser.getExercises().forEach(exercise -> exerciseStatisticsTable.getItems().add(new ExerciseStatistics(exercise, analyser)));
    }

    /**
     * If no similar solutions were found then the cluster viewing button is not interactable.
     */
    protected void toggleClusterButtonUsability() {
        if (analyser.getSimilarSolutionClusters() != null && analyser.getSimilarSolutionClusters().size() != 0) {
            viewClustersButton.setDisable(false);
        }
    }

    /**
     * Tries to open the Code view if the "View similar clusters & pairs" button is clicked.
     * If it fails, then shows an {@link Alert}.
     */
    @FXML
    private void viewClusters() {
        try {
            openCodeView();
        } catch (IOException e) {
            e.printStackTrace();
            showAndWaitAlert("Could not view clusters", "", Alert.AlertType.ERROR);
        }
    }

    /**
     * Opens the code view in a new window, or throws {@link IOException} if it fails to load.
     *
     * @throws IOException if the code view could not be opened
     */
    private void openCodeView() throws IOException {
        // If already open then bring to front and don't open new one
        if (codeViewStage != null) {
            codeViewStage.toFront();
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/ee/ut/similaritydetector/fxml/code_view.fxml"));
        Parent root = loader.load();
        CodeViewController controller = loader.getController();
        controller.setAnalyser(analyser);
        Platform.runLater(controller::createClusterItems);
        double width = MainViewController.stage.getScene().getWidth() > 1200 ?
                MainViewController.stage.getScene().getWidth() : 1200;
        double height = MainViewController.stage.getScene().getHeight() > 700 ?
                MainViewController.stage.getScene().getHeight() : 700;
        Scene codeViewScene = new Scene(root, width, height);
        Stage newWindow = new Stage();
        newWindow.setMinWidth(800);
        newWindow.setMinHeight(600);
        newWindow.setScene(codeViewScene);
        newWindow.centerOnScreen();
        newWindow.setTitle("Source code similarity detector - Code review - " + analyser.getZipDirectory().getName());
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        newWindow.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));

        newWindow.show();
        codeViewStage = newWindow;
        codeViewStage.setOnCloseRequest(event -> codeViewStage = null);

        // Resize cluster table columns
        Platform.runLater(controller::resizeClusterTableColumns);
    }

    @FXML
    private void runNewAnalysis() {
        try {
            // Close code view if open
            if (codeViewStage != null) {
                codeViewStage.close();
                codeViewStage = null;
            }
            openMainView();
        } catch (IOException e) {
            e.printStackTrace();
            showAndWaitAlert("Could not navigate back to main view", "Try restarting the application", Alert.AlertType.ERROR);
        }
    }

    private void openMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ee/ut/similaritydetector/fxml/main_view.fxml"));
        Parent root = loader.load();
        loader.setController(mainViewController);
        MainViewController.stage.setTitle("Source code similarity detector");
        Scene scene = new Scene(root, MainViewController.stage.getScene().getWidth(), MainViewController.stage.getScene().getHeight());
        MainViewController.stage.setScene(scene);
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        MainViewController.stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        deleteOutputFiles();
        mainViewController.openOptions();
        MainViewController.stage.show();
    }

}
