package com.cyberpath.smartlearn.util.preferences;

import android.app.Activity;

import androidx.appcompat.app.AppCompatDelegate;

import com.cyberpath.smartlearn.R;

public final class ThemeManager {

    private ThemeManager() {
    }

    public static void applyTheme(Activity activity) {
        int theme = PreferencesManager.getTemaResuelto(activity);
        int tamanoTexto = PreferencesManager.getTamanoTexto(activity);

        switch (theme) {
            case PreferencesManager.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                activity.setTheme(getStandardThemeByTextSize(tamanoTexto));
                break;
            case PreferencesManager.THEME_ACCESSIBLE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(getAccessibleThemeByTextSize(tamanoTexto));
                break;
            case PreferencesManager.THEME_LIGHT:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(getStandardThemeByTextSize(tamanoTexto));
                break;
        }
    }

    private static int getStandardThemeByTextSize(int textSize) {
        switch (textSize) {
            case PreferencesManager.TEXT_SIZE_SMALL:
                return R.style.Base_Theme_SmartLearn_Text_Small;
            case PreferencesManager.TEXT_SIZE_LARGE:
                return R.style.Base_Theme_SmartLearn_Text_Large;
            case PreferencesManager.TEXT_SIZE_MEDIUM:
            default:
                return R.style.Base_Theme_SmartLearn_Text_Medium;
        }
    }

    private static int getAccessibleThemeByTextSize(int textSize) {
        switch (textSize) {
            case PreferencesManager.TEXT_SIZE_SMALL:
                return R.style.Theme_SmartLearn_Accessible_Text_Small;
            case PreferencesManager.TEXT_SIZE_LARGE:
                return R.style.Theme_SmartLearn_Accessible_Text_Large;
            case PreferencesManager.TEXT_SIZE_MEDIUM:
            default:
                return R.style.Theme_SmartLearn_Accessible_Text_Medium;
        }
    }
}