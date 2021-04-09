package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.backend.Analyser;
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

import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAndWaitAlert;

public class CodeViewController {

    private final List<AccordionTableView> clusterPanes;
    private final List<TableView<SimilarSolutionPair>> clusterTables;
    private Analyser analyser;

    private static CodeViewController instance;

    @FXML
    private MenuBarController menuBarController;
    @FXML
    private VBox solutionClusterView;
    @FXML
    private SplitPane codeSplitPane;

    @FXML
    private MenuItem closeAllTabsMenuItem;

    private final List<CodePaneController> openCodePanes;


    public CodeViewController() {
        instance = this;
        clusterTables = new ArrayList<>();
        clusterPanes = new ArrayList<>();
        openCodePanes = new ArrayList<>();
    }

    public static CodeViewController getInstance() {
        return instance;
    }

    public void setAnalyser(Analyser analyser) {
        this.analyser = analyser;
    }

    public void addCodePane(CodePaneController codePaneController) {
        Platform.runLater(() -> openCodePanes.add(codePaneController));
        Platform.runLater(() -> codeSplitPane.getItems().add(codePaneController.getRoot()));
    }

    public void removeCodePane(CodePaneController codePaneController) {
        Platform.runLater(() -> openCodePanes.remove(codePaneController));
        Platform.runLater(() -> codeSplitPane.getItems().remove(codePaneController.getRoot()));
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
        MenuItem closeAllTabsMenuItem = menuBarController.getCloseAllTabsMenuItem();
        closeAllTabsMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        codeSplitPane.getItems().size() == 0,
                codeSplitPane.getItems())
        );
        closeAllTabsMenuItem.setVisible(true);
        closeAllTabsMenuItem.setOnAction(e -> closeAllCodeTabs());
    }

    /**
     * Creates the {@code AccordionTableView} elements from each {@code SimilarSolutionCluster} and adds to the view.
     */
    public void createClusterItems() {
        for (SimilarSolutionCluster cluster : analyser.getSimilarSolutionClusters()) {
            AccordionTableView solutionClusterItem = new AccordionTableView(cluster);
            VBox.setVgrow(solutionClusterItem, Priority.NEVER);
            clusterPanes.add(solutionClusterItem);
            solutionClusterView.getChildren().add(solutionClusterItem);

            // Gets the tableView and adds to list
            AnchorPane anchorPane = (AnchorPane) solutionClusterItem.getContent();
            clusterTables.add((TableView<SimilarSolutionPair>) anchorPane.getChildren().get(0));
        }
        // Add custom listeners to each table
        clusterTables.forEach(this::addCustomListener);

        // Add custom listener to each TitledPane
        clusterPanes.forEach(this::addOpenedListener);
    }

    /**
     * Adds a custom listener to {@link AccordionTableView} that closes other opened
     * AccordionTableViews if this one is opened.
     * @param clusterPane {@link AccordionTableView}
     */
    private  void addOpenedListener(AccordionTableView clusterPane) {
        clusterPane.expandedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || !newValue) return;
            // Closes other accordion items that are opened.
            for (AccordionTableView otherClusterPane : clusterPanes) {
                if (! clusterPane.equals(otherClusterPane)) {
                    otherClusterPane.setExpanded(false);
                }
            }
        });
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
                    // Closes opened tabs that are from a different cluster than this solution pair
                    SimilarSolutionCluster currentCluster = analyser.getSimilarSolutionClusters().stream().filter(cluster ->
                            cluster.getSolutionPairs().contains(solutionPair)).findAny().get();
                    for (CodePaneController codeTab : openCodePanes) {
                        if (! currentCluster.containsSolution(codeTab.getSolution())) {
                            closeCodeTab(codeTab);
                        }
                    }
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
                    // Resize code panes equally
                    Platform.runLater(this::resizeCodePanes);
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

    /**
     * Creates and opens a new code tab with the given solutions source code
     * in the code view if it is not already opened.
     *
     * @param solution {@link Solution}
     * @throws IOException if the solution's source code HTML could not be loaded,
     *          an {@link Alert} is created by {@link CodeViewController#showSolutionCodeReadingErrorAlert(Solution)}
     */
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

    /**
     * Shows an {@link Alert} if the solutions code could not be loaded.
     *
     * @param solution {@link Solution}
     */
    private void showSolutionCodeReadingErrorAlert(Solution solution) {
        showAndWaitAlert("Could not load solution code",
                solution.getExerciseName() + " - " + solution.getAuthor(),
                Alert.AlertType.ERROR);
    }

    /**
     * Resizes the cluster tables' columns
     */
    public void resizeClusterTableColumns() {
        clusterTables.forEach(this::resizeTableColumns);
    }

    /**
     * Resizes the columns of the given {@code TableView} to fit the size of columns' content.
     *
     * @param table the {@code TableView} to be resized
     */
    private void resizeTableColumns(TableView<?> table) {
        table.applyCss();
        table.layout();
        double columnsWidth = table.getColumns().stream().mapToDouble(TableColumnBase::getWidth).sum();
        double tableWidth = table.getWidth();
        if (tableWidth == 0) {
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            return;
        }
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

    private void resizeCodePanes() {
        int nrPanes = openCodePanes.size();
        double[] dividerPositions = new double[nrPanes];
        double dividerSize = 1.0 / nrPanes;
        for (int i = 0; i < nrPanes; i++) {
            dividerPositions[i] = dividerSize * (i + 1);
        }
        Platform.runLater(() -> codeSplitPane.setDividerPositions(dividerPositions));
    }

    /**
     * Closes the given code tab
     *
     * @param codePaneController {@link CodePaneController}
     */
    public void closeCodeTab(CodePaneController codePaneController) {
        removeCodePane(codePaneController);
    }

    /**
     * Closes all code tabs that are currently open.
     */
    @FXML
    private void closeAllCodeTabs() {
        Platform.runLater(openCodePanes::clear);
        Platform.runLater(() -> codeSplitPane.getItems().clear());
    }

}
