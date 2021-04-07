package ee.ut.similaritydetector.ui.controllers;

import ee.ut.similaritydetector.ui.SimilarityDetectorLauncher;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ee.ut.similaritydetector.ui.utils.UserData;

public class MenuBarController {

    private static final String darkThemeStylesheetPath = "/ee/ut/similaritydetector/style/dark_mode.scss";

    @FXML
    private Menu fileMenu;
    @FXML
    private MenuItem exitMenuItem;

    @FXML
    private Menu editMenu;

    @FXML
    private Menu themeMenu;
    @FXML
    private RadioMenuItem lightTheme;
    @FXML
    private RadioMenuItem darkTheme;

    @FXML
    private Menu helpMenu;
    @FXML
    private MenuItem aboutMenuItem;

    public MenuBarController() {
    }

    @FXML
    private void initialize() {
        lightTheme.setOnAction(event -> Platform.runLater(this::activateClassicTheme));
        darkTheme.setOnAction(event -> Platform.runLater(this::activateDarkTheme));
        // When scenes are switched then persists theme
        if (MainViewController.stage != null) {
            UserData userData = (UserData) MainViewController.stage.getUserData();
            if (userData.isDarkMode()) {
                darkTheme.setSelected(true);
            } else {
                lightTheme.setSelected(true);
            }
        }
        // When application is started
        else {
            Platform.runLater(this::activateDarkTheme);
        }
    }

    @FXML
    private void exitMenuItemClicked() {
        MainViewController.stage.close();
    }

    /**
     * Activates dark theme on every currently opened window by adding a css stylesheet to the scenes.
     */
    private void activateDarkTheme() {
        UserData userData = (UserData) MainViewController.stage.getUserData();
        userData.setDarkMode(true);
        MainViewController.stage.setUserData(userData);
        Stage.getWindows().forEach(window -> {
            ObservableList<String> stylesheets = window.getScene().getStylesheets();
            if (! stylesheets.contains(String.valueOf(this.getClass().getResource(darkThemeStylesheetPath)))) {
                stylesheets.add(String.valueOf(this.getClass().getResource(darkThemeStylesheetPath)));
            }
        });
        if (CodeViewController.getInstance() != null){
            CodeViewController.getInstance().getOpenCodePanes().forEach(CodePaneController::loadDarkThemeHTML);
        }
    }

    /**
     * Activates light theme on every currently opened window by adding a css stylesheet to the scenes.
     */
    private void activateClassicTheme() {
        UserData userData = (UserData) MainViewController.stage.getUserData();
        userData.setDarkMode(false);
        MainViewController.stage.setUserData(userData);
        Stage.getWindows().forEach(window -> window.getScene().getStylesheets().remove(String.valueOf(this.getClass().getResource(darkThemeStylesheetPath))));
        if (CodeViewController.getInstance() != null){
            CodeViewController.getInstance().getOpenCodePanes().forEach(CodePaneController::loadLightThemeHTML);
        }
    }

    /**
     * Persists the current theme through scene changes.
     */
    public void persistCurrentTheme() {
        if (darkTheme.isSelected()) {
            activateDarkTheme();
        } else {
            activateClassicTheme();
        }
    }

    @FXML
    private void showAboutInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        alert.setHeaderText("Source code similarity detector v1.0");
        alert.setContentText("Mikko Maran\n" +
                             "2021");
        // Dark mode
        if (((UserData) MainViewController.stage.getUserData()).isDarkMode()) {
            alert.getDialogPane().getStylesheets().add(String.valueOf(this.getClass().getResource(
                    "/ee/ut/similaritydetector/style/dark_mode.scss")));
        }
        alert.show();
    }

}
