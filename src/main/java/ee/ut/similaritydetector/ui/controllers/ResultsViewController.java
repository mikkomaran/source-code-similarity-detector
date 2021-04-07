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

import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAndWaitAlert;

public class ResultsViewController {

    private Analyser analyser;

    @FXML
    private MenuBarController menuBarController;

    @FXML
    private Label title;

    @FXML
    private Label totalSolutionsLabel;
    @FXML
    private Label solutionPairsLabel;

    @FXML
    private TableView<ExerciseStatistics> exerciseStatisticsTable;
    @FXML
    private TableColumn<ExerciseStatistics, String> exerciseNameColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> totalSolutionsColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> suspiciousSolutionsColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Double> percentageSuspiciousSolutionsColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> similarPairsColumn;
    @FXML
    private TableColumn<ExerciseStatistics, Integer> similarClustersColumn;

    @FXML
    private Button viewClustersButton;


    public ResultsViewController() {
    }

    public void setAnalyser(Analyser analyser) {
        this.analyser = analyser;
    }

    @FXML
    private void initialize() {
    }

    /**
     * Loads the statistics from the {@link Analyser} onto the results view.
     */
    public void readStatistics() {
        title.setText("Results - " + analyser.getZipDirectory().getName());
        totalSolutionsLabel.setText(String.valueOf(analyser.getExercises().stream().mapToInt(Exercise::getSolutionCount).sum()));
        solutionPairsLabel.setText(String.valueOf(analyser.getAnalysedSolutionPairsCount()));
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
        percentageSuspiciousSolutionsColumn.setCellValueFactory(new PropertyValueFactory<>("percentageSuspiciousSolutions"));
        similarPairsColumn.setCellValueFactory(new PropertyValueFactory<>("similarPairs"));
        similarClustersColumn.setCellValueFactory(new PropertyValueFactory<>("similarClusters"));
        analyser.getExercises().forEach(exercise -> exerciseStatisticsTable.getItems().add(new ExerciseStatistics(exercise, analyser)));
    }

    /**
     * If no similar solutions were found then the cluster viewing button is not interactable.
     */
    public void toggleClusterButtonUsability() {
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
    public void openCodeView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/ee/ut/similaritydetector/fxml/code_view.fxml"));
        Parent root = loader.load();
        CodeViewController controller = loader.getController();
        controller.setClusters(analyser.getSimilarSolutionClusters());
        Platform.runLater(controller::createClusterItems);

        Scene codeViewScene = new Scene(root, 1200, 700);
        Stage newWindow = new Stage();
        newWindow.setMinWidth(800);
        newWindow.setMinHeight(600);
        newWindow.setScene(codeViewScene);
        newWindow.centerOnScreen();
        newWindow.setTitle("Source code similarity detector - Similar clusters & pairs - " + analyser.getZipDirectory().getName());
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        newWindow.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));

        // Persists dark theme if it was activated before
        Platform.runLater(menuBarController::persistCurrentTheme);

        newWindow.show();

        // Resize cluster table columns
        Platform.runLater(controller::resizeClusterTableColumns);
    }

}
