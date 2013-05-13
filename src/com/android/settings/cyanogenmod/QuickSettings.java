/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import static com.android.internal.util.cm.QSConstants.TILE_BLUETOOTH;
import static com.android.internal.util.cm.QSConstants.TILE_MOBILEDATA;
import static com.android.internal.util.cm.QSConstants.TILE_NETWORKMODE;
import static com.android.internal.util.cm.QSConstants.TILE_NFC;
import static com.android.internal.util.cm.QSConstants.TILE_WIFIAP;
import static com.android.internal.util.cm.QSConstants.TILE_LTE;
import static com.android.internal.util.cm.QSUtils.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import net.margaritov.preference.colorpicker.ColorPickerView;

import com.android.internal.telephony.Phone;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class QuickSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "QuickSettings";

    private static final String SEPARATOR = "OV=I=XseparatorX=I=VO";
    private static final String EXP_RING_MODE = "pref_ring_mode";
    private static final String EXP_NETWORK_MODE = "pref_network_mode";
    private static final String EXP_SCREENTIMEOUT_MODE = "pref_screentimeout_mode";
    private static final String DYNAMIC_ALARM = "dynamic_alarm";
    private static final String DYNAMIC_BUGREPORT = "dynamic_bugreport";
    private static final String DYNAMIC_IME = "dynamic_ime";
    private static final String DYNAMIC_USBTETHER = "dynamic_usbtether";
    private static final String DYNAMIC_WIFI = "dynamic_wifi";
    private static final String COLLAPSE_PANEL = "collapse_panel";
    private static final String GENERAL_SETTINGS = "pref_general_settings";
    private static final String STATIC_TILES = "static_tiles";
    private static final String DYNAMIC_TILES = "pref_dynamic_tiles";
    private static final String TILE_BACKGROUND_STYLE = "tile_background_style";
    private static final String TILE_BACKGROUND_COLOR = "tile_background_color";
    private static final String TILE_TEXT_COLOR = "tile_text_color";
    private static final String RANDOM_COLORS = "random_colors";
    private static final String FLIP_TILE = "flip_tile";

    MultiSelectListPreference mRingMode;
    ListPreference mNetworkMode;
    ListPreference mScreenTimeoutMode;
    CheckBoxPreference mDynamicAlarm;
    CheckBoxPreference mDynamicBugReport;
    CheckBoxPreference mDynamicWifi;
    CheckBoxPreference mDynamicIme;
    CheckBoxPreference mDynamicUsbTether;
    CheckBoxPreference mCollapsePanel;
    CheckBoxPreference mFlipTile;
    ColorPickerPreference mTileBgColor;
    ColorPickerPreference mTileTextColor;
    ListPreference mTileBgStyle;
    PreferenceCategory mGeneralSettings;
    PreferenceCategory mStaticTiles;
    PreferenceCategory mDynamicTiles;
    PreferenceScreen mRandomColors;

    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_settings_panel_settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
	    mActivity = getActivity();

        PreferenceScreen prefSet = getPreferenceScreen();
        PackageManager pm = getPackageManager();
        mRandomColors = (PreferenceScreen) prefSet.findPreference(RANDOM_COLORS);
        ContentResolver resolver = getActivity().getContentResolver();
        mGeneralSettings = (PreferenceCategory) prefSet.findPreference(GENERAL_SETTINGS);
        mStaticTiles = (PreferenceCategory) prefSet.findPreference(STATIC_TILES);
        mDynamicTiles = (PreferenceCategory) prefSet.findPreference(DYNAMIC_TILES);

        mCollapsePanel = (CheckBoxPreference) prefSet.findPreference(COLLAPSE_PANEL);
        mCollapsePanel.setChecked(Settings.System.getInt(resolver, Settings.System.QS_COLLAPSE_PANEL, 0) == 1);

        mTileBgStyle = (ListPreference) findPreference(TILE_BACKGROUND_STYLE);
        mTileBgStyle.setOnPreferenceChangeListener(this);

        mTileBgColor = (ColorPickerPreference) findPreference(TILE_BACKGROUND_COLOR);
        mTileBgColor.setOnPreferenceChangeListener(this);
        mTileBgColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                resolver, Settings.System.QUICK_SETTINGS_BACKGROUND_COLOR, 0xFF000000)));

        mTileTextColor = (ColorPickerPreference) findPreference(TILE_TEXT_COLOR);
        mTileTextColor.setOnPreferenceChangeListener(this);
        mTileTextColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                resolver, Settings.System.QUICK_SETTINGS_TEXT_COLOR, 0xFFFFFFFF)));

        mFlipTile = (CheckBoxPreference) prefSet.findPreference(FLIP_TILE);
        mFlipTile.setChecked(Settings.System.getInt(resolver,
                Settings.System.QUICK_SETTINGS_TILES_FLIP, 1) == 1);

        // Add the sound mode
        mRingMode = (MultiSelectListPreference) prefSet.findPreference(EXP_RING_MODE);
        String storedRingMode = Settings.System.getString(resolver,
                Settings.System.EXPANDED_RING_MODE);
        if (storedRingMode != null) {
            String[] ringModeArray = TextUtils.split(storedRingMode, SEPARATOR);
            mRingMode.setValues(new HashSet<String>(Arrays.asList(ringModeArray)));
            updateSummary(storedRingMode, mRingMode, R.string.pref_ring_mode_summary);
        }
        mRingMode.setOnPreferenceChangeListener(this);

        // Add the network mode preference
        mNetworkMode = (ListPreference) prefSet.findPreference(EXP_NETWORK_MODE);
        if(mNetworkMode != null){
            mNetworkMode.setSummary(mNetworkMode.getEntry());
            mNetworkMode.setOnPreferenceChangeListener(this);
        }

        // Screen timeout mode
        mScreenTimeoutMode = (ListPreference) prefSet.findPreference(EXP_SCREENTIMEOUT_MODE);
        mScreenTimeoutMode.setSummary(mScreenTimeoutMode.getEntry());
        mScreenTimeoutMode.setOnPreferenceChangeListener(this);

        // Add the dynamic tiles checkboxes
        mDynamicAlarm = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_ALARM);
        mDynamicAlarm.setChecked(Settings.System.getInt(resolver, Settings.System.QS_DYNAMIC_ALARM, 1) == 1);
        mDynamicBugReport = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_BUGREPORT);
        mDynamicBugReport.setChecked(Settings.System.getInt(resolver, Settings.System.QS_DYNAMIC_BUGREPORT, 1) == 1);
        mDynamicIme = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_IME);
        if (mDynamicIme != null) {
            mDynamicIme.setChecked(Settings.System.getInt(resolver, Settings.System.QS_DYNAMIC_IME, 1) == 1);
        }
        mDynamicUsbTether = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_USBTETHER);
        if (mDynamicUsbTether != null) {
            if (deviceSupportsUsbTether(getActivity())) {
                mDynamicUsbTether.setChecked(Settings.System.getInt(resolver, Settings.System.QS_DYNAMIC_USBTETHER, 1) == 1);
            } else {
                mDynamicTiles.removePreference(mDynamicUsbTether);
                mDynamicUsbTether = null;
            }
        }
        mDynamicWifi = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_WIFI);
        if (mDynamicWifi != null) {
            if (deviceSupportsWifiDisplay(getActivity())) {
                mDynamicWifi.setChecked(Settings.System.getInt(resolver, Settings.System.QS_DYNAMIC_WIFI, 1) == 1);
            } else {
                mDynamicTiles.removePreference(mDynamicWifi);
                mDynamicWifi = null;
            }
        }

        // Don't show mobile data options if not supported
        if (!deviceSupportsMobileData(getActivity())) {
            QuickSettingsUtil.TILES.remove(TILE_MOBILEDATA);
            QuickSettingsUtil.TILES.remove(TILE_WIFIAP);
            QuickSettingsUtil.TILES.remove(TILE_NETWORKMODE);
            if(mNetworkMode != null) {
                mStaticTiles.removePreference(mNetworkMode);
            }
        } else {
            // We have telephony support however, some phones run on networks not supported
            // by the networkmode tile so remove both it and the associated options list
            int network_state = -99;
            try {
                network_state = Settings.Global.getInt(resolver,
                        Settings.Global.PREFERRED_NETWORK_MODE);
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "Unable to retrieve PREFERRED_NETWORK_MODE", e);
            }

            switch (network_state) {
                // list of supported network modes
                case Phone.NT_MODE_WCDMA_PREF:
                case Phone.NT_MODE_WCDMA_ONLY:
                case Phone.NT_MODE_GSM_UMTS:
                case Phone.NT_MODE_GSM_ONLY:
                    break;
                default:
                    QuickSettingsUtil.TILES.remove(TILE_NETWORKMODE);
                    mStaticTiles.removePreference(mNetworkMode);
                    break;
            }
        }

        // Don't show the bluetooth options if not supported
        if (!deviceSupportsBluetooth()) {
            QuickSettingsUtil.TILES.remove(TILE_BLUETOOTH);
        }

        // Dont show the NFC tile if not supported
        if (!deviceSupportsNfc(getActivity())) {
            QuickSettingsUtil.TILES.remove(TILE_NFC);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateVisibility();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mDynamicAlarm) {
            Settings.System.putInt(resolver, Settings.System.QS_DYNAMIC_ALARM,
                    mDynamicAlarm.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mDynamicBugReport) {
            Settings.System.putInt(resolver, Settings.System.QS_DYNAMIC_BUGREPORT,
                    mDynamicBugReport.isChecked() ? 1 : 0);
            return true;
        } else if (mDynamicIme != null && preference == mDynamicIme) {
            Settings.System.putInt(resolver, Settings.System.QS_DYNAMIC_IME,
                    mDynamicIme.isChecked() ? 1 : 0);
            return true;
        } else if (mDynamicUsbTether != null && preference == mDynamicUsbTether) {
            Settings.System.putInt(resolver, Settings.System.QS_DYNAMIC_USBTETHER,
                    mDynamicUsbTether.isChecked() ? 1 : 0);
            return true;
        } else if (mDynamicWifi != null && preference == mDynamicWifi) {
            Settings.System.putInt(resolver, Settings.System.QS_DYNAMIC_WIFI,
                    mDynamicWifi.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mCollapsePanel) {
            Settings.System.putInt(resolver, Settings.System.QS_COLLAPSE_PANEL,
                    mCollapsePanel.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mFlipTile) {
            Settings.System.putInt(resolver, Settings.System.QUICK_SETTINGS_TILES_FLIP,
                    mFlipTile.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class MultiSelectListPreferenceComparator implements Comparator<String> {
        private MultiSelectListPreference pref;

        MultiSelectListPreferenceComparator(MultiSelectListPreference p) {
            pref = p;
        }

        @Override
        public int compare(String lhs, String rhs) {
            return Integer.compare(pref.findIndexOfValue(lhs),
                    pref.findIndexOfValue(rhs));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRingMode) {
            ArrayList<String> arrValue = new ArrayList<String>((Set<String>) newValue);
            Collections.sort(arrValue, new MultiSelectListPreferenceComparator(mRingMode));
            Settings.System.putString(resolver, Settings.System.EXPANDED_RING_MODE,
                    TextUtils.join(SEPARATOR, arrValue));
            updateSummary(TextUtils.join(SEPARATOR, arrValue), mRingMode, R.string.pref_ring_mode_summary);
            return true;
        } else if (preference == mNetworkMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mNetworkMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.EXPANDED_NETWORK_MODE, value);
            mNetworkMode.setSummary(mNetworkMode.getEntries()[index]);
            return true;
        } else if (preference == mScreenTimeoutMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mScreenTimeoutMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_SCREENTIMEOUT_MODE, value);
            mScreenTimeoutMode.setSummary(mScreenTimeoutMode.getEntries()[index]);
            return true;

        } else if (preference == mTileTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.QUICK_SETTINGS_TEXT_COLOR, intHex);
            return true;

        } else if (preference == mTileBgColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.QUICK_SETTINGS_BACKGROUND_COLOR, intHex);
            return true;

		} else if (preference == mTileBgStyle) {
            int value = Integer.valueOf((String) newValue);
            int index = mTileBgStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, value);
            preference.setSummary(mTileBgStyle.getEntries()[index]);
            updateVisibility();
            return true;
        }
        return false;
    }

    private void updateVisibility() {
        int visible = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, 2);
        if (visible == 2) {
            mRandomColors.setEnabled(false);
            mTileBgColor.setEnabled(false);
        } else if (visible == 1) {
            mRandomColors.setEnabled(false);
            mTileBgColor.setEnabled(true);
        } else {
            mRandomColors.setEnabled(true);
            mTileBgColor.setEnabled(false);
        }
    }

    private void updateSummary(String val, MultiSelectListPreference pref, int defSummary) {
        // Update summary message with current values
        final String[] values = parseStoredValue(val);
        if (values != null) {
            final int length = values.length;
            final CharSequence[] entries = pref.getEntries();
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < (length); i++) {
                CharSequence entry = entries[Integer.parseInt(values[i])];
                if ((length - i) > 1) {
                    summary.append(entry).append(" | ");
                } else {
                    summary.append(entry);
                }
            }
            pref.setSummary(summary);
        } else {
            pref.setSummary(defSummary);
        }
    }

    public static String[] parseStoredValue(CharSequence val) {
        if (TextUtils.isEmpty(val)) {
            return null;
        } else {
            return val.toString().split(SEPARATOR);
        }
    }
}
