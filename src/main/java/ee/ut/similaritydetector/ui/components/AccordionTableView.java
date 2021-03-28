package main.java.ee.ut.similaritydetector.ui.components;

import javafx.beans.binding.Bindings;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;

import java.util.Comparator;
import java.util.stream.Collectors;


public class AccordionTableView extends TitledPane {

    public AccordionTableView(SimilarSolutionCluster cluster) {

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

        // TitledPane header and content
        this.setText(cluster.getName());
        this.setContent(anchorPane);

        // TitledPane restrictions
        this.setExpanded(false);
        this.setPrefWidth(400);
        this.setMinWidth(Region.USE_PREF_SIZE);
        this.setWrapText(true);
    }

}