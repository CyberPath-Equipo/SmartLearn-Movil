package com.cyberpath.smartlearn.util.preferences;

import android.app.Activity;

import androidx.appcompat.app.AppCompatDelegate;

import com.cyberpath.smartlearn.R;

public final class ThemeManager {

    private ThemeManager() {}

    public static void applyTheme(Activity activity) {

        int theme = PreferencesManager.getTemaApp(activity);

        switch (theme) {

            case PreferencesManager.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(R.style.Base_Theme_SmartLearn);
                break;

            case PreferencesManager.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
                activity.setTheme(R.style.Base_Theme_SmartLearn);
                break;

            case PreferencesManager.THEME_ACCESSIBLE:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(R.style.Theme_SmartLearn_Accessible);
                break;
        }
    }
}