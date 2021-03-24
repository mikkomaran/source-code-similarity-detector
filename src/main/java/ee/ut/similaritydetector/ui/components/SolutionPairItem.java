package main.java.ee.ut.similaritydetector.ui.components;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import main.java.ee.ut.similaritydetector.backend.SimilarSolutionPair;

import java.io.IOException;
import java.util.List;

public class SolutionPairItem extends Label {

    private final SimilarSolutionPair solutionPair;

    public SolutionPairItem(SimilarSolutionPair solutionPair) {
        this.solutionPair = solutionPair;

        this.setText(solutionPair.toString());
    }

    public void loadSolutionPairSourceCodes(Label leftLabel, Label rightLabel, TextArea textAreaLeft, TextArea textAreaRight) {
        List<String> sourceCodeLines1 = null;
        List<String> sourceCodeLines2 = null;
        try {
            sourceCodeLines1 = solutionPair.getFirstSolution().getSourceCodeLines();
            sourceCodeLines2 = solutionPair.getSecondSolution().getSourceCodeLines();
        } catch (IOException e) {
            // TODO: ???
            e.printStackTrace();
        }
        leftLabel.setText(solutionPair.getFirstSolution().getAuthor() + " - " + solutionPair.getFirstSolution().getExerciseName());
        rightLabel.setText(solutionPair.getSecondSolution().getAuthor() + " - " + solutionPair.getSecondSolution().getExerciseName());

        //TODO: add source codes as text
        setTextAreaText(textAreaLeft, sourceCodeLines1);
        setTextAreaText(textAreaRight, sourceCodeLines2);
    }

    private void setTextAreaText(TextArea textAreaRight, List<String> sourceCodeLines2) {
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0, n = sourceCodeLines2.size(); i < n; i++) {
            String line = sourceCodeLines2.get(i);
            sb2.append(i + 1).append("    ").append(line).append(System.lineSeparator());
        }
        textAreaRight.setText(sb2.toString());
    }
}
