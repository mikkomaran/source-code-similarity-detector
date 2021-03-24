package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;
import main.java.ee.ut.similaritydetector.ui.components.AccordionTableView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SideBySideViewController {

    private final List<TableView<SimilarSolutionPair>> clusterTables;
    @FXML
    public TextArea lineNumbersLeft;
    @FXML
    private VBox solutionClusterView;
    @FXML
    private Label titleLeft;
    @FXML
    private Label titleRight;
    @FXML
    private TextArea lineNumbersRight;
    @FXML
    private TextArea codeAreaLeft;
    @FXML
    private TextArea codeAreaRight;

    private List<SimilarSolutionCluster> clusters;

    public SideBySideViewController() {
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
     * Resizes the cluster tables' columns
     */
    public void resizeClusterTableColumns() {
        clusterTables.forEach(this::resizeTable);
    }

    /**
     * Resizes the columns of the given {@code TableView} to fit column content
     *
     * @param view - the {@code TableView} to be resized
     */
    public void resizeTable(TableView<?> view) {
        double columnsWidth = view.getColumns().stream().mapToDouble(TableColumnBase::getWidth).sum();
        double tableWidth = view.getWidth();
        if (tableWidth > columnsWidth) {
            TableColumn<?, ?> col = view.getColumns().get(view.getColumns().size() - 1);
            col.setPrefWidth(col.getWidth() + (tableWidth - columnsWidth) - 4);
        }
    }

    /**
     * Adds a custom listener to the given table that on table row selection
     * loads the corresponding solution pair code and removes current selection from other tables.
     *
     * @param tableView the tableview that gets added the listener
     */
    public void addCustomListener(TableView<SimilarSolutionPair> tableView) {
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
    public void loadSolutionPairSourceCodes(SimilarSolutionPair solutionPair, Label titleLeft, Label titleRight, TextArea lineNumbersLeft,
                                            TextArea lineNumbersRight, TextArea codeAreaLeft, TextArea codeAreaRight) {
        List<String> sourceCodeLines1 = null;
        List<String> sourceCodeLines2 = null;
        try {
            sourceCodeLines1 = solutionPair.getFirstSolution().getSourceCodeLines();
            sourceCodeLines2 = solutionPair.getSecondSolution().getSourceCodeLines();
        } catch (IOException e) {
            // TODO: error handling
            e.printStackTrace();
        }
        titleLeft.setText(solutionPair.getFirstSolution().getAuthor() + " - " + solutionPair.getFirstSolution().getExerciseName());
        titleRight.setText(solutionPair.getSecondSolution().getAuthor() + " - " + solutionPair.getSecondSolution().getExerciseName());

        setLineNumbersAndCodeLines(lineNumbersLeft, codeAreaLeft, sourceCodeLines1);
        setLineNumbersAndCodeLines(lineNumbersRight, codeAreaRight, sourceCodeLines2);
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
        lineNumbersArea.setText(lineNumbers.toString());
        codeLinesArea.setText(codeLines.toString());
    }

}
