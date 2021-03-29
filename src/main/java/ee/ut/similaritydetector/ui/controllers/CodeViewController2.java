package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;
import main.java.ee.ut.similaritydetector.backend.Solution;
import main.java.ee.ut.similaritydetector.ui.components.AccordionTableView;
import main.java.ee.ut.similaritydetector.ui.components.CodePaneController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeViewController2 {

    private final List<TableView<SimilarSolutionPair>> clusterTables;

    @FXML
    private MenuBarController menuBarController;
    @FXML
    private VBox solutionClusterView;
    @FXML
    private SplitPane codeSplitPane;


    private List<SimilarSolutionCluster> clusters;

    public CodeViewController2() {
        clusterTables = new ArrayList<>();
    }

    public List<TableView<SimilarSolutionPair>> getClusterTables() {
        return clusterTables;
    }

    public void setClusters(List<SimilarSolutionCluster> clusters) {
        this.clusters = clusters;
    }

    @FXML
    private void initialize() {
    }

    /**
     * Creates the {@code AccordionTableView} elements from each {@code SimilarSolutionCluster} and adds to the view.
     */
    public void createClusterItems() {
        for (SimilarSolutionCluster cluster : clusters) {
            AccordionTableView solutionClusterItem = new AccordionTableView(cluster);
            VBox.setVgrow(solutionClusterItem, Priority.NEVER);
            solutionClusterView.getChildren().add(solutionClusterItem);

            // Gets the tableView and adds to list
            AnchorPane anchorPane = (AnchorPane) solutionClusterItem.getContent();
            clusterTables.add((TableView<SimilarSolutionPair>) anchorPane.getChildren().get(0));
        }
        // Add custom listeners to each table
        clusterTables.forEach(this::addCustomListener);

    }

    /**
     * Adds a custom listener to the given table that on table row selection
     * loads the corresponding solution pair's codes
     * and removes current selection from other tables.
     *
     * @param tableView the tableview that gets added the listener
     */
    private void addCustomListener(TableView<SimilarSolutionPair> tableView) {
        tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            SimilarSolutionPair solutionPair = tableView.getSelectionModel().getSelectedItem();
            // First solution
            try {
                createNewCodePane(solutionPair.getFirstSolution());
            } catch (IOException e) {
                //TODO: error handling
                e.printStackTrace();
                showSolutionCodeReadingErrorAlert(solutionPair.getFirstSolution());
            }
            // Second solution
            try {
                createNewCodePane(solutionPair.getSecondSolution());
            } catch (IOException e) {
                //TODO: error handling
                e.printStackTrace();
                showSolutionCodeReadingErrorAlert(solutionPair.getFirstSolution());
            }
            // Clears selection from all other tables
            for (TableView<SimilarSolutionPair> otherTableView : clusterTables) {
                if (! tableView.equals(otherTableView)) {
                    otherTableView.getSelectionModel().clearSelection();
                }
            }
        });
    }

    private void createNewCodePane(Solution solution) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../../../../../../resources/ee/ut/similaritydetector/fxml/code_pane.fxml"));
        AnchorPane root = loader.load();
        codeSplitPane.getItems().add(root);
        CodePaneController controller = loader.getController();
        controller.setCodeViewController(this);
        controller.loadSolutionSourceCode(solution);

        // Persists dark theme if it was activated before
        menuBarController.persistDarkTheme();
    }

    private void showSolutionCodeReadingErrorAlert(Solution solution) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Could not load solution code");
        alert.setContentText(solution.getExerciseName() + " - " + solution.getAuthor());
        alert.showAndWait();
    }

    /**
     * Resizes the cluster tables' columns
     */
    public void resizeClusterTableColumns() {
        clusterTables.forEach(this::resizeTable);
    }

    /**
     * Resizes the columns of the given {@code TableView} to fit the size of columns' content.
     *
     * @param table - the {@code TableView} to be resized
     */
    private void resizeTable(TableView<?> table) {
        double columnsWidth = table.getColumns().stream().mapToDouble(TableColumnBase::getWidth).sum();
        double tableWidth = table.getWidth();
        if (tableWidth > columnsWidth) {
            tableWidth -= 4; // So random horizontal scroll doesn't happen
            TableColumn<?, ?> col1 = table.getColumns().get(0);
            TableColumn<?, ?> col2 = table.getColumns().get(1);
            TableColumn<?, ?> col3 = table.getColumns().get(2);
            double nameColumnsWidth = col1.getWidth() + col2.getWidth();
            double nameColumnsPrefWidth = tableWidth * 0.75;
            if (nameColumnsWidth < nameColumnsPrefWidth) {
                if (col1.getWidth() > nameColumnsPrefWidth / 2) {
                    col2.setPrefWidth(nameColumnsPrefWidth - col1.getWidth());
                    col3.setPrefWidth(tableWidth - nameColumnsPrefWidth);
                }
                if (col2.getWidth() > nameColumnsPrefWidth / 2) {
                    col1.setPrefWidth(nameColumnsPrefWidth - col2.getWidth());
                } else {
                    col1.setPrefWidth(nameColumnsPrefWidth / 2);
                    col2.setPrefWidth(nameColumnsPrefWidth / 2);
                }
                col3.setPrefWidth(tableWidth - nameColumnsPrefWidth);
            } else {
                col3.setPrefWidth(col3.getWidth() + (tableWidth - columnsWidth));
            }
        }
    }

    public void closeCodePane(AnchorPane codePaneRoot) {
        codeSplitPane.getItems().remove(codePaneRoot);
    }

}
