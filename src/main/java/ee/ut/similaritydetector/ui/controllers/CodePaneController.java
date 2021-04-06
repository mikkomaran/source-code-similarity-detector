package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.backend.Solution;
import ee.ut.similaritydetector.ui.utils.UserData;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

import java.io.File;

public class CodePaneController {

    @FXML
    private AnchorPane root;
    @FXML
    private Tab codeTab;
    @FXML
    private WebView webView;

    private CodeViewController codeViewController;

    private Solution solution;

    public CodePaneController() {
    }

    public void setCodeViewController(CodeViewController codeViewController) {
        this.codeViewController = codeViewController;
    }

    public AnchorPane getRoot() {
        return root;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    @FXML
    private void initialize() {
        codeTab.setOnClosed(event -> onTabClosed());
    }

    private void onTabClosed() {
        codeViewController.closeCodeTab(this);
    }

    /**
     * Loads the source code of the given {@link Solution} to the webview.
     */
    public void loadSolutionSourceCode() throws Exception {
        codeTab.setText(solution.getAuthor() + " - " + solution.getExerciseName());
        UserData userData = (UserData) MainViewController.stage.getUserData();
        if (userData.isDarkMode()) {
            loadDarkThemeHTML();
        } else {
            loadLightThemeHTML();
        }
    }

    /**
     * Loads the syntax highlighted source code HTML for light theme.
     */
    public void loadLightThemeHTML() {
        File htmlFile = solution.getSourceCodeHTMLLight();
        webView.getEngine().setUserStyleSheetLocation(getClass().getResource("/ee/ut/similaritydetector/style/webview_style_light.css").toString());
        webView.getEngine().load(htmlFile.toURI().toString());
    }

    /**
     * Loads the syntax highlighted source code HTML for dark theme.
     */
    public void loadDarkThemeHTML() {
        File htmlFile = solution.getSourceCodeHTMLDark();
        webView.getEngine().setUserStyleSheetLocation(getClass().getResource("/ee/ut/similaritydetector/style/webview_style_dark.css").toString());
        webView.getEngine().load(htmlFile.toURI().toString());
    }

}
