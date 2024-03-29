package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.backend.Analyser;
import ee.ut.similaritydetector.ui.utils.UserPreferences;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import ee.ut.similaritydetector.backend.SimilarSolutionPair;
import ee.ut.similaritydetector.backend.Solution;
import ee.ut.similaritydetector.ui.components.AccordionTableView;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAndWaitAlert;

public class CodeViewController {

    public static final String resourceBundlePath = "ee.ut.similaritydetector.language.code_view";

    private final List<AccordionTableView> clusterPanes;
    private final List<TableView<SimilarSolutionPair>> clusterTables;
    private Analyser analyser;

    private static CodeViewController instance;

    @FXML
    private MenuBarController menuBarController;
    @FXML
    private ScrollPane solutionClusterScrollPane;
    @FXML
    private VBox solutionClusterView;
    @FXML
    private SplitPane codeSplitPane;
    @FXML
    private Button hideSideBarButton;

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
        Platform.runLater(this::resizeCodePanes);
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
        hideSideBarButton.setOnAction(event -> Platform.runLater(this::hideClusterPane));
        ImageView arrowImg = new ImageView(new Image(
                getClass().getResourceAsStream("/ee/ut/similaritydetector/img/hidearrow.png")));
        arrowImg.setFitHeight(13);
        arrowImg.setFitWidth(9);
        hideSideBarButton.setGraphic(arrowImg);
        hideSideBarButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        // Persists dark theme if it was activated before
        Platform.runLater(menuBarController::persistCurrentTheme);
    }

    /**
     * Animates the closing of cluster pane.
     */
    private void hideClusterPane(){
        Duration duration = Duration.millis(300);
        solutionClusterScrollPane.setPrefWidth(solutionClusterScrollPane.getWidth());
        solutionClusterScrollPane.setMinWidth(solutionClusterScrollPane.getWidth());
        Timeline timeline = new Timeline(
                new KeyFrame(duration,
                    new KeyValue(solutionClusterScrollPane.maxWidthProperty(), 0, Interpolator.EASE_OUT),
                    new KeyValue(solutionClusterScrollPane.minWidthProperty(), 0, Interpolator.EASE_OUT)));
        timeline.setOnFinished(event -> {
            solutionClusterScrollPane.setVisible(false);
            hideSideBarButton.setOnAction(e -> Platform.runLater(this::openClusterPane));
        });
        hideSideBarButton.setOnAction(e -> {});
        timeline.play();
        hideSideBarButton.setRotate(180);

    }

    /**
     * Animates the opening of cluster pane.
     */
    public void openClusterPane(){
        Duration duration = Duration.millis(300);
        Timeline timeline = new Timeline(
                new KeyFrame(duration,
                        new KeyValue(solutionClusterScrollPane.maxWidthProperty(), solutionClusterScrollPane.getPrefWidth(), Interpolator.EASE_OUT),
                        new KeyValue(solutionClusterScrollPane.minWidthProperty(), solutionClusterScrollPane.getPrefWidth(), Interpolator.EASE_OUT)));
        solutionClusterScrollPane.setVisible(true);
        timeline.setOnFinished(event -> hideSideBarButton.setOnAction(e -> Platform.runLater(this::hideClusterPane)));
        hideSideBarButton.setOnAction(e -> {});
        timeline.play();
        hideSideBarButton.setRotate(0);
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
        ResourceBundle langBundle = ResourceBundle.getBundle(resourceBundlePath, UserPreferences.getInstance().getLocale());
        showAndWaitAlert(langBundle.getString("error_msg1"),
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
