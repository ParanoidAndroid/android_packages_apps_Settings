/*
 * Copyright (C) 2013 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.paranoid;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PieControls extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PIE_GRAVITY = "pie_gravity";
    private static final String PIE_MODE = "pie_mode";
    private static final String PIE_SIZE = "pie_size";
    private static final String PIE_TRIGGER = "pie_trigger";
    private static final String PIE_ANGLE = "pie_angle";
    private static final String PIE_GAP = "pie_gap";
    private static final String PIE_MENU = "pie_menu";
    private static final String PIE_SEARCH = "pie_search";
    private static final String PIE_CENTER = "pie_center";
    private static final String PIE_STICK = "pie_stick";

    private ListPreference mPieMode;
    private ListPreference mPieSize;
    private ListPreference mPieGravity;
    private ListPreference mPieTrigger;
    private ListPreference mPieAngle;
    private ListPreference mPieGap;

    private CheckBoxPreference mPieMenu;
    private CheckBoxPreference mPieSearch;
    private CheckBoxPreference mPieCenter;
    private CheckBoxPreference mPieStick;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pie_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        mPieMenu = (CheckBoxPreference) prefSet.findPreference(PIE_MENU);
        mPieMenu.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MENU, 1) == 1);

        mPieSearch = (CheckBoxPreference) prefSet.findPreference(PIE_SEARCH);
        mPieSearch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_SEARCH, 1) == 1);

        mPieCenter = (CheckBoxPreference) prefSet.findPreference(PIE_CENTER);
        mPieCenter.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_CENTER, 1) == 1);

        mPieStick = (CheckBoxPreference) prefSet.findPreference(PIE_STICK);
        mPieStick.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_STICK, 1) == 1);

        mPieGravity = (ListPreference) prefSet.findPreference(PIE_GRAVITY);
        int pieGravity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GRAVITY, 3);
        mPieGravity.setValue(String.valueOf(pieGravity));
        mPieGravity.setOnPreferenceChangeListener(this);

        mPieMode = (ListPreference) prefSet.findPreference(PIE_MODE);
        int pieMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MODE, 2);
        mPieMode.setValue(String.valueOf(pieMode));
        mPieMode.setOnPreferenceChangeListener(this);

        mPieSize = (ListPreference) prefSet.findPreference(PIE_SIZE);
        mPieTrigger = (ListPreference) prefSet.findPreference(PIE_TRIGGER);
        try {
            float pieSize = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_SIZE, 1.0f);
            mPieSize.setValue(String.valueOf(pieSize));
  
            float pieTrigger = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_TRIGGER);
            mPieTrigger.setValue(String.valueOf(pieTrigger));
        } catch(SettingNotFoundException ex) {
            // So what
        }

        mPieSize.setOnPreferenceChangeListener(this);
        mPieTrigger.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefSet.findPreference(PIE_GAP);
        int pieGap = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GAP, 2);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefSet.findPreference(PIE_ANGLE);
        int pieAngle = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPieMenu) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_MENU, mPieMenu.isChecked() ? 1 : 0);
        } else if (preference == mPieSearch) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_SEARCH, mPieSearch.isChecked() ? 1 : 0);
        } else if (preference == mPieCenter) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_CENTER, mPieCenter.isChecked() ? 1 : 0);
        } else if (preference == mPieStick) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_STICK, mPieStick.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_MODE, pieMode);
            return true;
        } else if (preference == mPieSize) {
            float pieSize = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_SIZE, pieSize);
            return true;
        } else if (preference == mPieGravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GRAVITY, pieGravity);
            return true;
        } else if (preference == mPieAngle) {
            int pieAngle = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ANGLE, pieAngle);
            return true;
        } else if (preference == mPieGap) {
            int pieGap = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GAP, pieGap);
            return true;
        } else if (preference == mPieTrigger) {
            float pieTrigger = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_TRIGGER, pieTrigger);
            return true;
        }
        return false;
    }
}
