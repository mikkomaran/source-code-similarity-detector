package main.java.ee.ut.similaritydetector.ui.components;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import main.java.ee.ut.similaritydetector.backend.Solution;
import main.java.ee.ut.similaritydetector.ui.controllers.CodeViewController2;

import java.io.IOException;
import java.util.List;

public class CodePaneController {

    @FXML
    private AnchorPane root;
    @FXML
    private Tab tab;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextArea lineNumbers;
    @FXML
    private TextArea codeArea;

    private CodeViewController2 codeViewController;

    public CodePaneController() {
    }

    public void setCodeViewController(CodeViewController2 codeViewController) {
        this.codeViewController = codeViewController;
    }

    @FXML
    private void initialize() {
        // Binding code area height to the scroll pane's height
        codeArea.prefHeightProperty().bind(scrollPane.heightProperty().subtract(2));

        // Restricts selecting text from line numbers area
        restrictTextSelection(lineNumbers);
    }

    // Taken from: https://stackoverflow.com/questions/61665296/how-to-disable-text-selection-in-textarea-javafx [25.03.2021]
    private void restrictTextSelection(TextArea textArea) {
        textArea.setTextFormatter(new TextFormatter<String>(change -> {
            change.setAnchor(change.getCaretPosition());
            return change;
        }));
    }

    /**
     * Loads the source code of the given {@link Solution} to the view.
     */
    public void loadSolutionSourceCode(Solution solution) throws IOException {
        List<String> sourceCodeLines = solution.getSourceCodeLines();
        tab.setText(solution.getAuthor() + " - " + solution.getExerciseName());
        setLineNumbersAndCodeLines(sourceCodeLines);
        Platform.runLater(this::bindLineNumberVerticalScrollToCodeArea);
        tab.setOnClosed(event -> codeViewController.closeCodePane(root));
    }

    /**
     * Generates line numbers from the given source code lines.
     * Adds line numbers and code line to the view.
     *
     * @param sourceCodeLines the given code lines
     */
    private void setLineNumbersAndCodeLines(List<String> sourceCodeLines) {
        StringBuilder lineNumbers = new StringBuilder();
        StringBuilder codeLines = new StringBuilder();
        for (int i = 0, n = sourceCodeLines.size(); i < n; i++) {
            String line = sourceCodeLines.get(i);
            lineNumbers.append(i + 1 < 10 ? "  " : i + 1 < 100 ? " " : "").append(i + 1).append(System.lineSeparator());
            codeLines.append(line).append(System.lineSeparator());
        }
        lineNumbers.append(System.lineSeparator());
        this.lineNumbers.setText(lineNumbers.toString());
        this.codeArea.setText(codeLines.toString());
    }

    public void bindLineNumberVerticalScrollToCodeArea() {
        ScrollBar scrollBar = (ScrollBar) codeArea.lookup(".scroll-bar:vertical");
        scrollBar.valueProperty().addListener((src, ov, nv) -> lineNumbers.setScrollTop(codeArea.getScrollTop()));
    }

}
