package main.java.ee.ut.similaritydetector.ui.components;

import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;

public class SolutionClusterItem extends TitledPane {

    private final SimilarSolutionCluster cluster;

    public SolutionClusterItem(SimilarSolutionCluster cluster, Label leftLabel, Label rightLabel, TextArea textAreaLeft, TextArea textAreaRight) {
        this.cluster = cluster;

        // Creates the layout
        AnchorPane anchorPane = new AnchorPane();
        ListView<SolutionPairItem> listView = new ListView<>();
        anchorPane.getChildren().add(listView);
        AnchorPane.setTopAnchor(listView, 0.0);
        AnchorPane.setBottomAnchor(listView, 0.0);
        AnchorPane.setLeftAnchor(listView, 0.0);
        AnchorPane.setRightAnchor(listView, 0.0);

        // Only one item can be selected
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setEditable(false);

        // Creates and adds the solution pair items to listView
        for (SimilarSolutionPair pair : cluster.getSolutionPairs()) {
            SolutionPairItem listItem = new SolutionPairItem(pair);
            listView.getItems().add(listItem);
        }

        // TitledPane header and content
        this.setText(cluster.getName());
        this.setContent(anchorPane);

        // Listener for list item getting selected
        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            SolutionPairItem listItem = listView.getSelectionModel().getSelectedItem();
            listItem.loadSolutionPairSourceCodes(leftLabel, rightLabel, textAreaLeft, textAreaRight);
        });
    }

}
