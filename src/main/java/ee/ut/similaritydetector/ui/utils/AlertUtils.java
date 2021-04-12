package ee.ut.similaritydetector.ui.utils;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AlertUtils {

    /**
     * Creates an {@link Alert} with a given error, context message and {@link javafx.scene.control.Alert.AlertType}.
     *
     * @param errorMessage error message to show
     * @param contextMessage error's context message to show
     * @param alertType the {@link javafx.scene.control.Alert.AlertType} of the created {@link Alert}
     * @return the created {@link Alert}
     */
    public static Alert createAlert(String errorMessage, String contextMessage, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setHeaderText(errorMessage);
        alert.setContentText(contextMessage);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setTitle("");
        stage.getIcons().add(new Image(AlertUtils.class.getResourceAsStream("/ee/ut/similaritydetector/img/app_icon.png")));
        // Dark mode
        if (UserPreferences.getInstance().isDarkMode()) {
            alert.getDialogPane().getStylesheets().add(String.valueOf(AlertUtils.class.getResource(
                    "/ee/ut/similaritydetector/style/dark_mode.scss")));
        }
        return alert;
    }

    /**
     * Creates an {@link Alert} with a given error, context message and {@link javafx.scene.control.Alert.AlertType} and shows it.
     *
     * @param errorMessage error message to show
     * @param contextMessage error's context message to show
     * @param alertType the {@link javafx.scene.control.Alert.AlertType} of the created {@link Alert}
     */
    public static void showAlert(String errorMessage, String contextMessage, Alert.AlertType alertType) {
        createAlert(errorMessage, contextMessage, alertType).show();
    }


    /**
     * Creates an {@link Alert} with a given error, context message and {@link javafx.scene.control.Alert.AlertType} and shows it
     * while halting the application.
     *
     * @param errorMessage error message to show
     * @param contextMessage error's context message to show
     * @param alertType the {@link javafx.scene.control.Alert.AlertType} of the created {@link Alert}
     */
    public static void showAndWaitAlert(String errorMessage, String contextMessage, Alert.AlertType alertType) {
        createAlert(errorMessage, contextMessage, alertType).showAndWait();
    }

}
