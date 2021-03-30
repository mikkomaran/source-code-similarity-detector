package ee.ut.similaritydetector.ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    public void readStatistics() {
        totalSolutionsLabel.setText(String.valueOf(analyser.getExercises().stream().mapToInt(Exercise::getExerciseSolutionCount).sum()));
        solutionPairsLabel.setText(String.valueOf(analyser.getAnalysedSolutionPairsCount()));
        similarPairsLabel.setText(String.valueOf(analyser.getSimilarSolutionPairs().size()));
        similarClustersLabel.setText(String.valueOf(analyser.getSimilarSolutionClusters().size()));
    }


    public void toggleClusterButtonUsability() {
        if (analyser.getSimilarSolutionClusters() != null && analyser.getSimilarSolutionClusters().size() != 0) {
            viewClustersButton.setDisable(false);
        }
    }

    @FXML
    private void viewClusters() {
        try {
            openCodeView2();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Could not view clusters");
            alert.showAndWait();
        }
    }

    /*  public void openCodeView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../../fxml/code_view.fxml"));
        Parent root = loader.load();
        CodeViewController controller = loader.getController();
        controller.setClusters(analyser.getSimilarSolutionClusters());
        controller.createClusterItems();

        Scene codeViewScene = new Scene(root, 1200, 700);
        Stage newWindow = new Stage();
        newWindow.setMinWidth(800);
        newWindow.setMinHeight(600);
        newWindow.setScene(codeViewScene);
        newWindow.centerOnScreen();
        newWindow.show();

        // Persists dark theme if it was activated before
        menuBarController.persistDarkTheme();

        // Resize cluster table columns
        controller.resizeClusterTableColumns();

        // Binds line numbers to move with code area's scrollbar
        controller.bindLineNumberVerticalScrollToCodeArea(controller.getLineNumbersLeft(), controller.getCodeAreaLeft());
        controller.bindLineNumberVerticalScrollToCodeArea(controller.getLineNumbersRight(), controller.getCodeAreaRight());
    }
     */

    public void openCodeView2() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../../fxml/code_view_2.fxml"));
        Parent root = loader.load();
        CodeViewController2 controller = loader.getController();
        controller.setClusters(analyser.getSimilarSolutionClusters());
        Platform.runLater(controller::createClusterItems);

        Scene codeViewScene = new Scene(root, 1200, 700);
        Stage newWindow = new Stage();
        newWindow.setMinWidth(800);
        newWindow.setMinHeight(600);
        newWindow.setScene(codeViewScene);
        newWindow.centerOnScreen();
        newWindow.show();

        // Persists dark theme if it was activated before
        menuBarController.persistDarkTheme();

        // Resize cluster table columns
        controller.resizeClusterTableColumns();
    }

}
