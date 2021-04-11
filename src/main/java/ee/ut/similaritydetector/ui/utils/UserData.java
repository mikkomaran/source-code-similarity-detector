package ee.ut.similaritydetector.ui.utils;

/**
 * Utility class for storing the application's runtime user preferences.
 */
public class UserData {

    private static UserData instance;

    private boolean isDarkMode;

    public UserData(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }

    public static UserData getInstance() {
        if (instance == null) {
            instance = new UserData(false);
        }
        return instance;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }
}
