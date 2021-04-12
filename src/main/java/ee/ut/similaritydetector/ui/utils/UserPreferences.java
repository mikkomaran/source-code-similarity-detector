package ee.ut.similaritydetector.ui.utils;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility class for loading and saving the application's user preferences during runtime.
 */
public class UserPreferences {

    private static final String preferencesFilePath = "/ee/ut/similaritydetector/preferences.properties";

    private static UserPreferences instance;

    private final Properties preferences;

    private boolean isDarkMode;

    private Locale locale;

    public UserPreferences(boolean isDarkMode, Locale locale, Properties preferences) {
        this.isDarkMode = isDarkMode;
        this.locale = locale;
        this.preferences = preferences;
    }

    public static UserPreferences getInstance() {
        if (instance == null) {
            Properties preferences = new Properties();
            try (InputStream inputStream = UserPreferences.class.getResourceAsStream(preferencesFilePath)) {
                preferences.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String theme = preferences.getProperty("theme", "light");
            String language = preferences.getProperty("language", "et_EE");
            boolean isDarkMode = theme.equals("dark");
            Locale locale = Locale.forLanguageTag(language);
            instance = new UserPreferences(isDarkMode, locale, preferences);
        }
        return instance;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        preferences.setProperty("theme", darkMode ? "dark" : "light");
    }

    public void setLocale(String langTag) {
        locale = Locale.forLanguageTag(langTag);
        preferences.setProperty("language", langTag);
    }

    public Locale getLocale() {
        return locale;
    }

}
