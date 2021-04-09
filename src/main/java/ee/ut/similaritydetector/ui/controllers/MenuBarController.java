package ee.ut.similaritydetector.ui.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import ee.ut.similaritydetector.ui.utils.UserData;
import javafx.stage.WindowEvent;

import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAlert;

public class MenuBarController {

    private static final String darkThemeStylesheetPath = "/ee/ut/similaritydetector/style/dark_mode.scss";

    @FXML
    private Menu fileMenu;
    @FXML
    private MenuItem closeAllTabsMenuItem;
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

    public MenuItem getCloseAllTabsMenuItem() {
        return closeAllTabsMenuItem;
    }

    @FXML
    private void initialize() {
        lightTheme.setOnAction(event -> Platform.runLater(this::activateLightTheme));
        darkTheme.setOnAction(event -> Platform.runLater(this::activateDarkTheme));
        // When scenes are switched then persists theme
        if (MainViewController.stage != null) {
            if (UserData.getInstance().isDarkMode()) {
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
        MainViewController.stage.fireEvent(new WindowEvent(MainViewController.stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    /**
     * Activates dark theme on every currently opened window by adding a css stylesheet to the scenes.
     */
    private void activateDarkTheme() {
        UserData.getInstance().setDarkMode(true);
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
    private void activateLightTheme() {
        UserData.getInstance().setDarkMode(false);
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
            activateLightTheme();
        }
    }

    @FXML
    private void showAboutInfo() {
        showAlert("Source code similarity detector v1.0",
                "Mikko Maran\n" + "2021",
                Alert.AlertType.INFORMATION);
    }

}
