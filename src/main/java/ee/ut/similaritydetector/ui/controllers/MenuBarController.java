package main.java.ee.ut.similaritydetector.ui.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.stage.Window;
import main.java.ee.ut.similaritydetector.ui.utils.UserData;

import java.util.Arrays;

public class MenuBarController {

    private static final String darkThemeStylesheetPath = "../../../../../../resources/ee/ut/similaritydetector/style/dark_mode.scss";
    private static final String[] fxmlFilePaths = {
            "../../../../../../resources/ee/ut/similaritydetector/fxml/code_view.fxml",
            "../../../../../../resources/ee/ut/similaritydetector/fxml/main_view.fxml",
            "../../../../../../resources/ee/ut/similaritydetector/fxml/menu_bar.fxml",
            "../../../../../../resources/ee/ut/similaritydetector/fxml/results_view.fxml"
    };

    @FXML
    private Menu fileMenu;

    @FXML
    private Menu editMenu;

    @FXML
    private Menu themeMenu;
    @FXML
    private RadioMenuItem classicTheme;
    @FXML
    public RadioMenuItem darkTheme;

    @FXML
    private Menu helpMenu;

    public MenuBarController() {
    }

    @FXML
    private void initialize() {
        classicTheme.setOnAction(event -> activateClassicTheme());
        darkTheme.setOnAction(event -> activateDarkTheme());
        if (MainViewController.stage != null) {
            UserData userData = (UserData) MainViewController.stage.getUserData();
            if (userData.isDarkMode()) {
                darkTheme.setSelected(true);
            }
        }
    }

    public void activateDarkTheme() {
        UserData userData = (UserData) MainViewController.stage.getUserData();
        userData.setDarkMode(true);
        MainViewController.stage.setUserData(userData);
        Stage.getWindows().forEach(window -> {
            ObservableList<String> stylesheets = window.getScene().getStylesheets();
            if (! stylesheets.contains(String.valueOf(this.getClass().getResource(darkThemeStylesheetPath)))) {
                stylesheets.add(String.valueOf(this.getClass().getResource(darkThemeStylesheetPath)));
            }
        });

    }

    public void activateClassicTheme() {
        UserData userData = (UserData) MainViewController.stage.getUserData();
        userData.setDarkMode(false);
        MainViewController.stage.setUserData(userData);
        Stage.getWindows().forEach(window -> window.getScene().getStylesheets().remove(String.valueOf(this.getClass().getResource(darkThemeStylesheetPath))));
    }

    public void persistDarkTheme() {
        if (darkTheme.isSelected()) {
            activateDarkTheme();
        }
    }

}
