package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.ui.utils.UserData;
import javafx.application.Platform;
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
import java.math.BigDecimal;
import java.math.RoundingMode;

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
    }

    private void fillExerciseStatisticsTable() {
        exerciseNameColumn.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        totalSolutionsColumn.setCellValueFactory(new PropertyValueFactory<>("totalSolutions"));
        suspiciousSolutionsColumn.setCellValueFactory(new PropertyValueFactory<>("suspiciousSolutions"));
        percentageSuspiciousSolutionsColumn.setCellValueFactory(new PropertyValueFactory<>("percentageSuspiciousSolutions"));
        similarPairsColumn.setCellValueFactory(new PropertyValueFactory<>("similarPairs"));
        similarClustersColumn.setCellValueFactory(new PropertyValueFactory<>("similarClusters"));
        analyser.getExercises().forEach(exercise -> exerciseStatisticsTable.getItems().add(new ExerciseStatistics(exercise)));
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Could not view clusters");
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
            // Dark mode
            if (((UserData) MainViewController.stage.getUserData()).isDarkMode()) {
                alert.getDialogPane().getStylesheets().add(String.valueOf(this.getClass().getResource(
                        "/ee/ut/similaritydetector/style/dark_mode.scss")));
            }
            alert.showAndWait();
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

    public class ExerciseStatistics {
        private final String exerciseName;
        private final int totalSolutions;
        private final int suspiciousSolutions;
        private final double percentageSuspiciousSolutions;
        private final int similarPairs;
        private final int similarClusters;

        public String getExerciseName() {
            return exerciseName;
        }

        public int getTotalSolutions() {
            return totalSolutions;
        }

        public int getSuspiciousSolutions() {
            return suspiciousSolutions;
        }

        public double getPercentageSuspiciousSolutions() {
            return percentageSuspiciousSolutions;
        }

        public int getSimilarPairs() {
            return similarPairs;
        }

        public int getSimilarClusters() {
            return similarClusters;
        }

        public ExerciseStatistics(Exercise exercise) {
            this.exerciseName = exercise.getName();
            this.totalSolutions = exercise.getSolutionCount();
            this.suspiciousSolutions = analyser.getSimilarSolutionClusters().stream().filter(cluster ->
                    cluster.getExerciseName().equals(exerciseName)).mapToInt(cluster ->
                    cluster.getSolutions().size()).sum();
            BigDecimal percentage = new BigDecimal(Double.toString((double) suspiciousSolutions / totalSolutions * 100));
            percentage = percentage.setScale(1, RoundingMode.HALF_UP);
            this.percentageSuspiciousSolutions = percentage.doubleValue();
            this.similarPairs = (int) analyser.getSimilarSolutionPairs().stream().filter(pair ->
                    pair.getFirstSolution().getExerciseName().equals(exerciseName)).count();
            this.similarClusters = (int) analyser.getSimilarSolutionClusters().stream().filter(cluster ->
                    cluster.getExerciseName().equals(exerciseName)).count();
        }
    }
}
