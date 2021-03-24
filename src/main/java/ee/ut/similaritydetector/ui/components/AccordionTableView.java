package main.java.ee.ut.similaritydetector.ui.components;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;

import java.io.IOException;
import java.util.List;

public class AccordionTableView extends TitledPane {

    public AccordionTableView(SimilarSolutionCluster cluster, Label titleLeft, Label titleRight, TextArea lineNumbersLeft,
                              TextArea lineNumbersRight, TextArea codeAreaLeft, TextArea codeAreaRight) {

        // Creates the table layout
        AnchorPane anchorPane = new AnchorPane();
        TableView<SimilarSolutionPair> tableView = new TableView<>();
        anchorPane.getChildren().add(tableView);

        AnchorPane.setTopAnchor(tableView, 0.0);
        AnchorPane.setBottomAnchor(tableView, 0.0);
        AnchorPane.setLeftAnchor(tableView, 0.0);
        AnchorPane.setRightAnchor(tableView, 0.0);

        // Table constraints
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.setEditable(false);
        //tableView.addEventFilter(ScrollEvent.ANY, Event::consume);
        //tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add("no-header");

        // Create columns for tableview
        TableColumn<SimilarSolutionPair, String> column1 = new TableColumn<>("Author 1");
        column1.setCellValueFactory(new PropertyValueFactory<>("author1"));

        TableColumn<SimilarSolutionPair, String> column2 = new TableColumn<>("Author 2");
        column2.setCellValueFactory(new PropertyValueFactory<>("author2"));

        TableColumn<SimilarSolutionPair, String> column3 = new TableColumn<>("Similarity");
        column3.setCellValueFactory(new PropertyValueFactory<>("similarityPercentage"));
        column3.getStyleClass().add("percentage-column");

        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        tableView.getColumns().add(column3);

        // Adds the solution pair items to tableview
        for (SimilarSolutionPair pair : cluster.getSolutionPairs()) {
            tableView.getItems().add(pair);
        }

        // Sets titledPane header and content and is collapsed in the beginning
        this.setText(cluster.getName());
        this.setContent(anchorPane);
        this.setExpanded(false);

        // Listener for list item getting selected
        tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            SimilarSolutionPair solutionPair = tableView.getSelectionModel().getSelectedItem();
            loadSolutionPairSourceCodes(solutionPair, titleLeft, titleRight, lineNumbersLeft, lineNumbersRight, codeAreaLeft, codeAreaRight);
        });
    }

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
