package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.ui.utils.UserData;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import ee.ut.similaritydetector.backend.SimilarSolutionPair;
import ee.ut.similaritydetector.backend.Solution;
import ee.ut.similaritydetector.ui.components.AccordionTableView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeViewController {

    private final List<TableView<SimilarSolutionPair>> clusterTables;

    private static CodeViewController instance;

    @FXML
    private MenuBarController menuBarController;
    @FXML
    private VBox solutionClusterView;
    @FXML
    private SplitPane codeSplitPane;

    @FXML
    private MenuItem closeAllTabsMenuItem;

    private List<SimilarSolutionCluster> clusters;

    private final List<CodePaneController> openCodePanes;


    public CodeViewController() {
        instance = this;
        clusterTables = new ArrayList<>();
        openCodePanes = new ArrayList<>();
    }

    public static CodeViewController getInstance() {
        return instance;
    }

    public List<TableView<SimilarSolutionPair>> getClusterTables() {
        return clusterTables;
    }

    public void setClusters(List<SimilarSolutionCluster> clusters) {
        this.clusters = clusters;
    }

    public void addCodePane(CodePaneController codePaneController) {
        openCodePanes.add(codePaneController);
        codeSplitPane.getItems().add(codePaneController.getRoot());
    }

    public void removeCodePane(CodePaneController codePaneController) {
        openCodePanes.remove(codePaneController);
        codeSplitPane.getItems().remove(codePaneController.getRoot());
    }

    public List<CodePaneController> getOpenCodePanes() {
        return openCodePanes;
    }

    @FXML
    private void initialize() {
        closeAllTabsMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                codeSplitPane.getItems().size() == 0,
                codeSplitPane.getItems())
        );
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
        tableView.setRowFactory( tv -> {
            TableRow<SimilarSolutionPair> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SimilarSolutionPair solutionPair = row.getItem();
                    // First solution
                    try {
                        createNewCodePane(solutionPair.getFirstSolution());
                    } catch (IOException e) {
                        e.printStackTrace();
                        showSolutionCodeReadingErrorAlert(solutionPair.getFirstSolution());
                    }
                    // Second solution
                    try {
                        createNewCodePane(solutionPair.getSecondSolution());
                    } catch (IOException e) {
                        e.printStackTrace();
                        showSolutionCodeReadingErrorAlert(solutionPair.getFirstSolution());
                    }
                }
            });
            return row;
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            // Clears selection from all other tables
            for (TableView<SimilarSolutionPair> otherTableView : clusterTables) {
                if (! tableView.equals(otherTableView)) {
                    otherTableView.getSelectionModel().clearSelection();
                }
            }
        });
    }

    private void createNewCodePane(Solution solution) throws IOException {
        // If the solution is already open then we don't duplicate it
        if (openCodePanes.stream().anyMatch(codePaneController -> codePaneController.getSolution().equals(solution))){
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/ee/ut/similaritydetector/fxml/code_pane.fxml"));
        loader.load();
        CodePaneController controller = loader.getController();
        controller.setCodeViewController(this);
        controller.setSolution(solution);
        Platform.runLater(() -> {
            try {
                controller.loadSolutionSourceCode();
            } catch (Exception e) {
                e.printStackTrace();
                showSolutionCodeReadingErrorAlert(solution);
            }
        });
        addCodePane(controller);
    }

    private void showSolutionCodeReadingErrorAlert(Solution solution) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Could not load solution code");
        alert.setContentText(solution.getExerciseName() + " - " + solution.getAuthor());
        // Dark mode
        if (((UserData) MainViewController.stage.getUserData()).isDarkMode()) {
            alert.getDialogPane().getStylesheets().add(String.valueOf(this.getClass().getResource(
                    "/ee/ut/similaritydetector/style/dark_mode.scss")));
        }
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
     * @param table the {@code TableView} to be resized
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

    public void closeCodeTab(CodePaneController codePaneController) {
        removeCodePane(codePaneController);
        codeSplitPane.getItems().remove(codePaneController.getRoot());
    }

    @FXML
    private void closeAllCodeTabs() {
        openCodePanes.clear();
        codeSplitPane.getItems().clear();
    }

}
