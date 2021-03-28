package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import main.java.ee.ut.similaritydetector.backend.Analyser;

import java.io.IOException;

public class ResultsViewController {

    private Analyser analyser;

    @FXML
    private MenuBarController menuBarController;

    @FXML
    private Button viewClustersButton;


    public ResultsViewController() {

    }

    @FXML
    private void initialize() {
        //TODO: load statistics
    }

    public void setAnalyser(Analyser analyser) {
        this.analyser = analyser;
    }

    public void toggleClusterButtonUsability() {
        if (analyser.getSimilarSolutionClusters() != null && analyser.getSimilarSolutionClusters().size() != 0) {
            viewClustersButton.setDisable(false);
        }
    }

    public void openCodeView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../../../../../../resources/ee/ut/similaritydetector/fxml/code_view.fxml"));
        Parent root = loader.load();
        CodeViewController controller = loader.getController();
        controller.setClusters(analyser.getSimilarSolutionClusters());
        controller.createClusterItems();

        Scene codeViewScene = new Scene(root, 1200, 700);
        Stage newWindow = new Stage();
        newWindow.setMinWidth(800);
        newWindow.setMinHeight(600);
        newWindow.setScene(codeViewScene);

        // Persists dark theme if it was activated before
        menuBarController.persistDarkTheme();

        // Resize cluster table columns
        controller.resizeClusterTableColumns();

        newWindow.show();

        // Binds line numbers to move with code area's scrollbar
        controller.bindLineNumberVerticalScrollToCodeArea(controller.getLineNumbersLeft(), controller.getCodeAreaLeft());
        controller.bindLineNumberVerticalScrollToCodeArea(controller.getLineNumbersRight(), controller.getCodeAreaRight());

    }

    @FXML
    private void viewClusters() {
        try {
            openCodeView();
        } catch (IOException e) {
            // TODO: error handling
            System.out.println("Could not view clusters.");
        }
    }

}
