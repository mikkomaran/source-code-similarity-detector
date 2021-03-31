package ee.ut.similaritydetector.ui.components;

import ee.ut.similaritydetector.backend.Solution;
import ee.ut.similaritydetector.ui.controllers.CodeViewController;

import ee.ut.similaritydetector.ui.controllers.MainViewController;
import ee.ut.similaritydetector.ui.utils.UserData;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

import java.io.File;

public class CodePaneController2 {

    @FXML
    private AnchorPane root;
    @FXML
    private Tab codeTab;
    @FXML
    private WebView webView;

    private CodeViewController codeViewController;

    public CodePaneController2() {
    }

    public void setCodeViewController(CodeViewController codeViewController) {
        this.codeViewController = codeViewController;
    }

    @FXML
    private void initialize() {

    }

    /**
     * Loads the source code of the given {@link Solution} to the webview.
     */
    public void loadSolutionSourceCode(Solution solution) throws Exception {
        codeTab.setText(solution.getAuthor() + " - " + solution.getExerciseName());
        File htmlFile;
        UserData userData = (UserData) MainViewController.stage.getUserData();
        if (userData.isDarkMode()) {
            htmlFile = solution.getSourceCodeHTMLDark();
            webView.getEngine().setUserStyleSheetLocation(getClass().getResource("../../style/webview_style_dark.css").toString());
        } else {
            htmlFile = solution.getSourceCodeHTMLLight();
            webView.getEngine().setUserStyleSheetLocation(getClass().getResource("../../style/webview_style_light.css").toString());
        }
        webView.getEngine().load(htmlFile.toURI().toString());
        codeTab.setOnClosed(event -> codeViewController.closeCodeTab(root));
    }

}
