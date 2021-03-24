package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;
import main.java.ee.ut.similaritydetector.ui.components.AccordionTableView;

import java.util.ArrayList;
import java.util.List;

public class SideBySideViewController {

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
    private final List<TableView<SimilarSolutionPair>> clusterTables;

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

    public void createClusterItems() {
        for (SimilarSolutionCluster cluster : clusters) {
            AccordionTableView solutionClusterItem = new AccordionTableView(cluster, titleLeft, titleRight, lineNumbersLeft, lineNumbersRight, codeAreaLeft, codeAreaRight);
            VBox.setVgrow(solutionClusterItem, Priority.NEVER);
            solutionClusterView.getChildren().add(solutionClusterItem);

            // Gets the tableView and adds to list
            AnchorPane anchorPane = (AnchorPane) solutionClusterItem.getContent();
            clusterTables.add((TableView<SimilarSolutionPair>) anchorPane.getChildren().get(0));
        }
    }

    public void resizeClusterTableColumns() {
        clusterTables.forEach(SideBySideViewController::resizeTable);
    }

    public static void resizeTable(TableView<?> view) {
        double columnsWidth = view.getColumns().stream().mapToDouble(TableColumnBase::getWidth).sum();

        double tableWidth = view.getWidth();

        if (tableWidth > columnsWidth) {
            TableColumn<?, ?> col = view.getColumns().get(view.getColumns().size() - 1);
            col.setPrefWidth(col.getWidth() + (tableWidth - columnsWidth) - 4);
        }
    }

}
