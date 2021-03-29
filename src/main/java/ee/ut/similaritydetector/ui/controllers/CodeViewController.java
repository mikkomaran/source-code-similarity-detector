package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;
import main.java.ee.ut.similaritydetector.backend.Solution;
import main.java.ee.ut.similaritydetector.ui.components.AccordionTableView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeViewController {

    private final List<TableView<SimilarSolutionPair>> clusterTables;

    @FXML
    private MenuBarController menuBarController;

    @FXML
    private VBox solutionClusterView;

    @FXML
    private ScrollPane scrollPaneLeft;
    @FXML
    private Label titleLeft;
    @FXML
    private TextArea lineNumbersLeft;
    @FXML
    private TextArea codeAreaLeft;

    @FXML
    private ScrollPane scrollPaneRight;
    @FXML
    private Label titleRight;
    @FXML
    private TextArea lineNumbersRight;
    @FXML
    private TextArea codeAreaRight;

    private List<SimilarSolutionCluster> clusters;

    public CodeViewController() {
        clusterTables = new ArrayList<>();
    }

    public List<TableView<SimilarSolutionPair>> getClusterTables() {
        return clusterTables;
    }

    public void setClusters(List<SimilarSolutionCluster> clusters) {
        this.clusters = clusters;
    }

    public TextArea getLineNumbersLeft() {
        return lineNumbersLeft;
    }

    public TextArea getCodeAreaLeft() {
        return codeAreaLeft;
    }

    public TextArea getLineNumbersRight() {
        return lineNumbersRight;
    }

    public TextArea getCodeAreaRight() {
        return codeAreaRight;
    }

    @FXML
    private void initialize() {
        // Binding code area height to the scroll pane's height
        codeAreaLeft.prefHeightProperty().bind(scrollPaneLeft.heightProperty().subtract(2));
        codeAreaRight.prefHeightProperty().bind(scrollPaneRight.heightProperty().subtract(2));

        // Restricts selecting text from line numbers area
        restrictTextSelection(lineNumbersLeft);
        restrictTextSelection(lineNumbersRight);
    }

    // Taken from: https://stackoverflow.com/questions/61665296/how-to-disable-text-selection-in-textarea-javafx [25.03.2021]
    private void restrictTextSelection(TextArea textArea) {
        textArea.setTextFormatter(new TextFormatter<String>(change -> {
            change.setAnchor(change.getCaretPosition());
            return change;
        }));
    }

    public void bindLineNumberVerticalScrollToCodeArea(TextArea lineNumbersArea, TextArea codeArea) {
        ScrollBar scrollBar = (ScrollBar) codeArea.lookup(".scroll-bar:vertical");
        scrollBar.valueProperty().addListener((src, ov, nv) -> lineNumbersArea.setScrollTop(codeArea.getScrollTop()));
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
     * loads the corresponding solution pair code and removes current selection from other tables.
     *
     * @param tableView the tableview that gets added the listener
     */
    private void addCustomListener(TableView<SimilarSolutionPair> tableView) {
        tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            SimilarSolutionPair solutionPair = tableView.getSelectionModel().getSelectedItem();
            loadSolutionPairSourceCodes(solutionPair, titleLeft, titleRight, lineNumbersLeft, lineNumbersRight, codeAreaLeft, codeAreaRight);
            for (TableView<SimilarSolutionPair> otherTableView : clusterTables) {
                if (!tableView.equals(otherTableView)) {
                    otherTableView.getSelectionModel().clearSelection();
                }
            }
        });
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
                }
                else {
                    col1.setPrefWidth(nameColumnsPrefWidth / 2);
                    col2.setPrefWidth(nameColumnsPrefWidth / 2);
                }
                col3.setPrefWidth(tableWidth - nameColumnsPrefWidth);
            }
            else {
                col3.setPrefWidth(col3.getWidth() + (tableWidth - columnsWidth));
            }
        }
    }

    /**
     * Loads the source codes from the given solutionPair to the view.
     *
     * @param solutionPair given {@code SimilarSolutionPair}
     * @param titleLeft title label for first solution's name
     * @param titleRight title label for second solution's name
     * @param lineNumbersLeft {@code TextArea} for first solution code's line numbers
     * @param lineNumbersRight {@code TextArea} for second solution code's line numbers
     * @param codeAreaLeft {@code TextArea} for first solution's source code lines
     * @param codeAreaRight {@code TextArea} for second solution's source code lines
     */
    private void loadSolutionPairSourceCodes(SimilarSolutionPair solutionPair, Label titleLeft, Label titleRight, TextArea lineNumbersLeft,
                                            TextArea lineNumbersRight, TextArea codeAreaLeft, TextArea codeAreaRight) {
        // First solution
        try {
            List<String> sourceCodeLines1 = solutionPair.getFirstSolution().getSourceCodeLines();
            titleLeft.setText(solutionPair.getFirstSolution().getAuthor() + " - " + solutionPair.getFirstSolution().getExerciseName());
            setLineNumbersAndCodeLines(lineNumbersLeft, codeAreaLeft, sourceCodeLines1);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: error handling
            showSolutionCodeReadingErrorAlert(solutionPair.getFirstSolution());
        }

        // Second solution
        try {
            List<String> sourceCodeLines2 = solutionPair.getSecondSolution().getSourceCodeLines();
            titleRight.setText(solutionPair.getSecondSolution().getAuthor() + " - " + solutionPair.getSecondSolution().getExerciseName());
            setLineNumbersAndCodeLines(lineNumbersRight, codeAreaRight, sourceCodeLines2);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: error handling
            showSolutionCodeReadingErrorAlert(solutionPair.getSecondSolution());
        }
    }

    private void showSolutionCodeReadingErrorAlert(Solution solution) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Could not load solution code");
        alert.setContentText(solution.getExerciseName() + " - " + solution.getAuthor());
        alert.showAndWait();
    }

    /**
     * Generates line numbers from the given source code lines.
     * Adds line numbers and code line to the view.
     *
     * @param lineNumbersArea {@code TextArea} for line numbers
     * @param codeLinesArea {@code TextArea} for code lines
     * @param sourceCodeLines the given code lines
     */
    private void setLineNumbersAndCodeLines(TextArea lineNumbersArea, TextArea codeLinesArea, List<String> sourceCodeLines) {
        StringBuilder lineNumbers = new StringBuilder();
        StringBuilder codeLines = new StringBuilder();
        for (int i = 0, n = sourceCodeLines.size(); i < n; i++) {
            String line = sourceCodeLines.get(i);
            lineNumbers.append(i + 1 < 10 ? "  " : i + 1 < 100 ? " " : "").append(i + 1).append(System.lineSeparator());
            codeLines.append(line).append(System.lineSeparator());
        }
        lineNumbers.append(System.lineSeparator());
        lineNumbersArea.setText(lineNumbers.toString());
        codeLinesArea.setText(codeLines.toString());
    }

}
