package ee.ut.similaritydetector.ui.utils;

public class UserData {

    private boolean isDarkMode;

    public UserData(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }
}
