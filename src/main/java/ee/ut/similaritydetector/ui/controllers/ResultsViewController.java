package ee.ut.similaritydetector.ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ee.ut.similaritydetector.backend.Analyser;
import ee.ut.similaritydetector.backend.Exercise;

import java.io.IOException;

public class ResultsViewController {

    private Analyser analyser;

    @FXML
    private MenuBarController menuBarController;

    @FXML
    private Label totalSolutionsLabel;
    @FXML
    private Label solutionPairsLabel;

    @FXML
    private Label similarPairsLabel;
    @FXML
    private Label similarClustersLabel;
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
        totalSolutionsLabel.setText(String.valueOf(analyser.getExercises().stream().mapToInt(Exercise::getExerciseSolutionCount).sum()));
        solutionPairsLabel.setText(String.valueOf(analyser.getAnalysedSolutionPairsCount()));
        similarPairsLabel.setText(String.valueOf(analyser.getSimilarSolutionPairs().size()));
        similarClustersLabel.setText(String.valueOf(analyser.getSimilarSolutionClusters().size()));
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
        newWindow.setTitle("Source code similarity detector - Similar clusters & pairs");
        // Icon from: https://icons-for-free.com/spy-131964785010048699/ [25.03.2021]
        newWindow.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));

        // Persists dark theme if it was activated before
        Platform.runLater(menuBarController::persistCurrentTheme);

        // Resize cluster table columns
        Platform.runLater(controller::resizeClusterTableColumns);

        newWindow.show();
    }

}
