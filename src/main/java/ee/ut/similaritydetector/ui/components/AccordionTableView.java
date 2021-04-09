package ee.ut.similaritydetector.ui.components;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import ee.ut.similaritydetector.backend.SimilarSolutionPair;
import javafx.scene.text.Text;

import java.util.Comparator;
import java.util.stream.Collectors;


public class AccordionTableView extends TitledPane {

    private final String exerciseName;

    public String getExerciseName() {
        return exerciseName;
    }

    public AccordionTableView(SimilarSolutionCluster cluster) {
        exerciseName = cluster.getExerciseName();

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
        tableView.getStyleClass().add("no-header");
        int cellSize = 25;
        tableView.setMinHeight(cellSize);

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

        // Adds the solution pair items to tableview sorted by percentages descending
        tableView.getItems().addAll(cluster.getSolutionPairs().stream().sorted(
                Comparator.comparingDouble(SimilarSolutionPair::getSimilarity).reversed())
                .collect(Collectors.toList()));

        // To remove empty rows from the bottom of the table we have to set fixed cell size
        tableView.setFixedCellSize(cellSize);
        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(1));
        tableView.minHeight(cellSize + 1);

        // TitledPane header
        // If title text is too long then wrap text manually with line breaks
        // Couldn't find a better solution for this
        Platform.runLater(() -> {
            StringBuilder headerText = new StringBuilder();
            StringBuilder lineText = new StringBuilder();
            for (String word : cluster.getName().split(" ")) {
                lineText.append(word).append(" ");
                Text text = new Text(lineText.toString());
                if (text.getLayoutBounds().getWidth() > 400 - 50) {
                    headerText.append(System.lineSeparator());
                    lineText.delete(0, lineText.length());
                    lineText.append(word);
                }
                headerText.append(word).append(" ");
            }
            this.setText(headerText.toString());
        });

        // TitledPane's content
        this.setContent(anchorPane);


        // TitledPane restrictions
        this.setExpanded(false);
        this.setMinWidth(0);
        this.setMaxWidth(400);
        this.setWrapText(true);
    }
}
