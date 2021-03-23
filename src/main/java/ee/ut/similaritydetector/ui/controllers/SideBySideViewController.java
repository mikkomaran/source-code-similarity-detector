package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionCluster;
import main.java.ee.ut.similaritydetector.ui.components.AccordionItem;

import java.util.List;

public class SideBySideViewController {

    @FXML
    private Accordion accordion;
    @FXML
    private ScrollPane scrollPaneLeft;
    @FXML
    private ScrollPane scrollPaneRight;
    @FXML
    private Label leftLabel;
    @FXML
    private Label rightLabel;
    @FXML
    private TextArea textAreaLeft;
    @FXML
    private TextArea textAreaRight;

    private List<SimilarSolutionCluster> clusters;

    public SideBySideViewController() {
    }

    public void setClusters(List<SimilarSolutionCluster> clusters) {
        this.clusters = clusters;
    }

    @FXML
    private void initialize() {
    }

    public void createAccordionItems() {
        for (SimilarSolutionCluster cluster : clusters) {
            AccordionItem accordionItem = new AccordionItem(cluster, leftLabel, rightLabel, textAreaLeft, textAreaRight);
            accordion.getPanes().add(accordionItem);
        }
    }


}
