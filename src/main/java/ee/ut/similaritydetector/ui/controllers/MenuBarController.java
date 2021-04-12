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
import ee.ut.similaritydetector.ui.utils.UserPreferences;
import javafx.stage.WindowEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static ee.ut.similaritydetector.ui.controllers.ResultsViewController.codeViewStage;
import static ee.ut.similaritydetector.ui.utils.AlertUtils.showAlert;

public class MenuBarController {

    private static final String darkThemeStylesheetPath = "/ee/ut/similaritydetector/style/dark_mode.scss";
    public static final String resourceBundlePath = "ee.ut.similaritydetector.language.menu_bar";

    @FXML
    private Menu fileMenu;
    @FXML
    private MenuItem closeAllTabsMenuItem;
    @FXML
    private MenuItem exitMenuItem;

    @FXML
    private Menu themeMenu;
    @FXML
    private RadioMenuItem lightTheme;
    @FXML
    private RadioMenuItem darkTheme;

    @FXML
    private Menu languageMenu;
    @FXML
    private RadioMenuItem estonianLang;
    @FXML
    private RadioMenuItem englishLang;

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
        Platform.runLater(() -> {
            ResourceBundle langBundle = ResourceBundle.getBundle(resourceBundlePath, UserPreferences.getInstance().getLocale());
            fileMenu.setText(langBundle.getString("file"));
            closeAllTabsMenuItem.setText(langBundle.getString("close_all_tabs"));
            exitMenuItem.setText(langBundle.getString("exit"));
            themeMenu.setText(langBundle.getString("theme"));
            lightTheme.setText(langBundle.getString("light"));
            darkTheme.setText(langBundle.getString("dark"));
            languageMenu.setText(langBundle.getString("language"));
            helpMenu.setText(langBundle.getString("help"));
            aboutMenuItem.setText(langBundle.getString("about"));
        });

        lightTheme.setOnAction(event -> Platform.runLater(this::activateLightTheme));
        darkTheme.setOnAction(event -> Platform.runLater(this::activateDarkTheme));
        // When scenes are switched then persists theme selection
        Platform.runLater(this::persistCurrentTheme);

        if (UserPreferences.getInstance().getLocale().equals(Locale.forLanguageTag("et_EE"))) {
            estonianLang.setSelected(true);
        } else {
            englishLang.setSelected(true);
        }
        estonianLang.setOnAction(event -> Platform.runLater(() -> changeLanguage("et_EE")));
        englishLang.setOnAction(event -> Platform.runLater(() -> changeLanguage("EN")));
    }

    @FXML
    private void exitMenuItemClicked() {
        MainViewController.stage.fireEvent(new WindowEvent(MainViewController.stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    /**
     * Activates dark theme on every currently opened window by adding a css stylesheet to the scenes.
     */
    private void activateDarkTheme() {
        darkTheme.setSelected(true);
        UserPreferences.getInstance().setDarkMode(true);
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
        lightTheme.setSelected(true);
        UserPreferences.getInstance().setDarkMode(false);
        Stage.getWindows().forEach(window -> window.getScene().getStylesheets().remove(String.valueOf(this.getClass().getResource(darkThemeStylesheetPath))));
        if (CodeViewController.getInstance() != null){
            CodeViewController.getInstance().getOpenCodePanes().forEach(CodePaneController::loadLightThemeHTML);
        }
    }

    /**
     * Persists the current theme through scene changes.
     */
    public void persistCurrentTheme() {
        if (UserPreferences.getInstance().isDarkMode()) {
            activateDarkTheme();
        } else {
            activateLightTheme();
        }
    }

    @FXML
    private void changeLanguage(String langTag) {
        Locale currentLocale = UserPreferences.getInstance().getLocale();
        Locale newLocale = Locale.forLanguageTag(langTag);
        if (currentLocale.equals(newLocale)) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("");
        ResourceBundle langBundle = ResourceBundle.getBundle(resourceBundlePath, UserPreferences.getInstance().getLocale());
        alert.setHeaderText(langBundle.getString("lang_change_message"));
        alert.setContentText(langBundle.getString("lang_change_context_message"));
        ButtonType restartButtonType = ButtonType.OK;
        Button restartButton = (Button) alert.getDialogPane().lookupButton(restartButtonType);
        restartButton.setText(langBundle.getString("restart"));
        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText(langBundle.getString("cancel"));

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        // Dark mode
        if (UserPreferences.getInstance().isDarkMode()) {
            alert.getDialogPane().getStylesheets().add(String.valueOf(this.getClass().getResource(
                    "/ee/ut/similaritydetector/style/dark_mode.scss")));
        }
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == restartButtonType) {
            UserPreferences.getInstance().setLocale(langTag);
            if (codeViewStage != null) {
                codeViewStage.close();
                codeViewStage = null;
            }
            MainViewController.stage.close();
            Platform.runLater( () -> {
                try {
                    new SimilarityDetectorLauncher().start( new Stage() );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            if (newLocale.equals(Locale.forLanguageTag("et_EE"))) {
                englishLang.setSelected(true);
            } else {
                estonianLang.setSelected(true);
            }
        }
    }

    @FXML
    private void showAboutInfo() {
        ResourceBundle langBundle = ResourceBundle.getBundle("ee.ut.similaritydetector.language.menu_bar", UserPreferences.getInstance().getLocale());
        showAlert( langBundle.getString("app_name") + " v1.0",
                "Mikko Maran\n" + "2021",
                Alert.AlertType.INFORMATION);
    }

}
