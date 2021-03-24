package main.java.ee.ut.similaritydetector.ui.components;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;


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
    }

}
