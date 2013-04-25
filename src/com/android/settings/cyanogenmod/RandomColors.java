
package com.android.settings.cyanogenmod;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class RandomColors extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREF_RANDOM_COLOR_ONE = "color_one";
    private static final String PREF_RANDOM_COLOR_TWO = "color_two";
    private static final String PREF_RANDOM_COLOR_THREE = "color_three";
    private static final String PREF_RANDOM_COLOR_FOUR = "color_four";
    private static final String PREF_RANDOM_COLOR_FIVE = "color_five";
    private static final String PREF_RANDOM_COLOR_SIX = "color_six";

    ColorPickerPreference mOne;
    ColorPickerPreference mTwo;
    ColorPickerPreference mThree;
    ColorPickerPreference mFour;
    ColorPickerPreference mFive;
    ColorPickerPreference mSix;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        PreferenceScreen prefSet = getPreferenceScreen();
        addPreferencesFromResource(R.xml.prefs_random_colors);

        mOne = (ColorPickerPreference) prefSet.findPreference(PREF_RANDOM_COLOR_ONE);
        mTwo = (ColorPickerPreference) prefSet.findPreference(PREF_RANDOM_COLOR_TWO);
        mThree = (ColorPickerPreference) prefSet.findPreference(PREF_RANDOM_COLOR_THREE);
        mFour = (ColorPickerPreference) prefSet.findPreference(PREF_RANDOM_COLOR_FOUR);
        mFive = (ColorPickerPreference) prefSet.findPreference(PREF_RANDOM_COLOR_FIVE);
        mSix = (ColorPickerPreference) prefSet.findPreference(PREF_RANDOM_COLOR_SIX);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
        updateSummaries();
    }

    private void registerListeners() {
        mOne.setOnPreferenceChangeListener(this);
        mTwo.setOnPreferenceChangeListener(this);
        mThree.setOnPreferenceChangeListener(this);
        mFour.setOnPreferenceChangeListener(this);
        mFive.setOnPreferenceChangeListener(this);
        mSix.setOnPreferenceChangeListener(this);
    }

    private void updateSummaries() {
        mOne.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(
                Settings.System.getString(getContentResolver(),
                Settings.System.RANDOM_COLOR_ONE))));
        mTwo.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(
                Settings.System.getString(getContentResolver(),
                Settings.System.RANDOM_COLOR_TWO))));
        mThree.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(
                Settings.System.getString(getContentResolver(),
                Settings.System.RANDOM_COLOR_THREE))));
        mFour.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(
                Settings.System.getString(getContentResolver(),
                Settings.System.RANDOM_COLOR_FOUR))));
        mFive.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(
                Settings.System.getString(getContentResolver(),
                Settings.System.RANDOM_COLOR_FIVE))));
        mSix.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(
                Settings.System.getString(getContentResolver(),
                Settings.System.RANDOM_COLOR_SIX))));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mOne) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RANDOM_COLOR_ONE, intHex);
            return true;
        } else if (preference == mTwo) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RANDOM_COLOR_TWO, intHex);
            return true;
        } else if (preference == mThree) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RANDOM_COLOR_THREE, intHex);
            return true;
        } else if (preference == mFour) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RANDOM_COLOR_FOUR, intHex);
            return true;
        } else if (preference == mFive) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RANDOM_COLOR_FIVE, intHex);
            return true;
        } else if (preference == mSix) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RANDOM_COLOR_SIX, intHex);
            return true;
        }
        return false;
    }

}

