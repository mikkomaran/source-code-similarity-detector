package main.java.ee.ut.similaritydetector.ui.utils;

public class UserData {

    private boolean isDarkMode;

    public UserData() {
        isDarkMode = false;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }
}
