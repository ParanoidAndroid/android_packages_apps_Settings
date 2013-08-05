/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.android.settings.cyanogenmod.autosms.MessagingHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class QuietHours extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener  {

    private static final String TAG = "QuietHours";
    private static final String KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled";
    private static final String KEY_QUIET_HOURS_MUTE = "quiet_hours_mute";
    private static final String KEY_QUIET_HOURS_STILL = "quiet_hours_still";
    private static final String KEY_QUIET_HOURS_DIM = "quiet_hours_dim";
    private static final String KEY_QUIET_HOURS_HAPTIC = "quiet_hours_haptic";
    private static final String KEY_QUIET_HOURS_NOTE = "quiet_hours_note";
    private static final String KEY_QUIET_HOURS_TIMERANGE = "quiet_hours_timerange";
    private static final String KEY_AUTO_SMS = "auto_sms";
    private static final String KEY_AUTO_SMS_CALL = "auto_sms_call";
    private static final String KEY_AUTO_SMS_MESSAGE = "auto_sms_message";

    private CheckBoxPreference mQuietHoursEnabled;
    private Preference mQuietHoursNote;
    private CheckBoxPreference mQuietHoursMute;
    private CheckBoxPreference mQuietHoursStill;
    private CheckBoxPreference mQuietHoursDim;
    private CheckBoxPreference mQuietHoursHaptic;
    private TimeRangePreference mQuietHoursTimeRange;
    private ListPreference mAutoSms;
    private ListPreference mAutoSmsCall;
    private Preference mAutoSmsMessage;

    private int mSmsPref;
    private int mCallPref;

    private String mAutoText = null;
    private String mDefaultText = null;

    private SharedPreferences mPrefs;
    private OnSharedPreferenceChangeListener mPreferencesChangeListener;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {
            addPreferencesFromResource(R.xml.quiet_hours_settings);

            mContext = getActivity().getApplicationContext();

            ContentResolver resolver = mContext.getContentResolver();

            PreferenceScreen prefSet = getPreferenceScreen();

            mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            mPreferencesChangeListener = new OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals(KEY_AUTO_SMS_CALL) || key.equals(KEY_AUTO_SMS)) {
                        MessagingHelper.scheduleService(mContext);
                    }
                }
            };
            mPrefs.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);

            mDefaultText = getResources()
                    .getString(R.string.quiet_hours_auto_sms_null);

            // Load the preferences
            mQuietHoursNote = prefSet.findPreference(KEY_QUIET_HOURS_NOTE);
            mQuietHoursEnabled = (CheckBoxPreference) prefSet.findPreference(KEY_QUIET_HOURS_ENABLED);
            mQuietHoursTimeRange = (TimeRangePreference) prefSet.findPreference(KEY_QUIET_HOURS_TIMERANGE);
            mQuietHoursMute = (CheckBoxPreference) prefSet.findPreference(KEY_QUIET_HOURS_MUTE);
            mQuietHoursStill = (CheckBoxPreference) prefSet.findPreference(KEY_QUIET_HOURS_STILL);
            mQuietHoursHaptic = (CheckBoxPreference) prefSet.findPreference(KEY_QUIET_HOURS_HAPTIC);
            mQuietHoursDim = (CheckBoxPreference) findPreference(KEY_QUIET_HOURS_DIM);
            mAutoSms = (ListPreference) findPreference(KEY_AUTO_SMS);
            mAutoSmsCall = (ListPreference) findPreference(KEY_AUTO_SMS_CALL);
            mAutoSmsMessage = (Preference) findPreference(KEY_AUTO_SMS_MESSAGE);

            // Remove the "Incoming calls behaviour" note if the device does not support phone calls
            if (mQuietHoursNote != null && getResources().getBoolean(com.android.internal.R.bool.config_voice_capable) == false) {
                prefSet.removePreference(mQuietHoursNote);
                prefSet.removePreference((PreferenceGroup) findPreference("sms_respond"));
            }

            // Set the preference state and listeners where applicable
            mQuietHoursEnabled.setChecked(Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_ENABLED, 0) == 1);
            mQuietHoursTimeRange.setTimeRange(Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_START, 0),
                    Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_END, 0));
            mQuietHoursTimeRange.setOnPreferenceChangeListener(this);
            mQuietHoursMute.setChecked(Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_MUTE, 0) == 1);
            mQuietHoursStill.setChecked(Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_STILL, 0) == 1);
            mQuietHoursHaptic.setChecked(Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_HAPTIC, 0) == 1);
            mAutoSms.setValue((mPrefs.getString(KEY_AUTO_SMS, "0")));
            mAutoSms.setOnPreferenceChangeListener(this);
            mAutoSmsCall.setValue((mPrefs.getString(KEY_AUTO_SMS_CALL, "0")));
            mAutoSmsCall.setOnPreferenceChangeListener(this);
            mSmsPref = Integer.parseInt(mPrefs.getString(KEY_AUTO_SMS, "0"));
            mCallPref = Integer.parseInt(mPrefs.getString(KEY_AUTO_SMS_CALL, "0"));
            updateAutoSmsEnabledState();

            // Remove the notification light setting if the device does not support it 
            if (mQuietHoursDim != null && getResources().getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed) == false) {
                getPreferenceScreen().removePreference(mQuietHoursDim);
            } else {
                mQuietHoursDim.setChecked(Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_DIM, 0) == 1);
            }
            updateAutoText();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefs.unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = mContext.getContentResolver();

        if (preference == mQuietHoursEnabled) {
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_ENABLED,
                    mQuietHoursEnabled.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursMute) {
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_MUTE,
                    mQuietHoursMute.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursStill) {
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_STILL,
                    mQuietHoursStill.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursDim) {
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_DIM,
                    mQuietHoursDim.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursHaptic) {
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_HAPTIC,
                    mQuietHoursHaptic.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mAutoSmsMessage) {
            final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.autosms_edittext, null);
            final EditText input = (EditText) view.findViewById(R.id.autosms_edittext);
            input.setText(mAutoText);
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.quiet_hours_auto_string_title)
                    .setMessage(R.string.quiet_hours_auto_string_explain)
                    .setView(view)
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();
                                    if (TextUtils.isEmpty(value)) {
                                        value = mDefaultText;
                                    }
                                    SharedPreferences.Editor editor = mPrefs.edit();
                                    editor.putString(KEY_AUTO_SMS_MESSAGE, value).commit();
                                    updateAutoText();
                                }
                            });
            alert.show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = mContext.getContentResolver();
        if (preference == mQuietHoursEnabled) {
            MessagingHelper.scheduleService(mContext);
            return true;
        } else if (preference == mQuietHoursTimeRange) {
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_START,
                    mQuietHoursTimeRange.getStartTime());
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_END,
                    mQuietHoursTimeRange.getEndTime());
            MessagingHelper.scheduleService(mContext);
            return true;
        } else if (preference == mAutoSms) {
            mSmsPref = Integer.parseInt((String) newValue);
            updateAutoSmsEnabledState();
            MessagingHelper.scheduleService(mContext);
            return true;
        } else if (preference == mAutoSmsCall) {
            mCallPref = Integer.parseInt((String) newValue);
            updateAutoSmsEnabledState();
            MessagingHelper.scheduleService(mContext);
            return true;
        }
        return false;
    }

    private void updateAutoSmsEnabledState() {
        if (mSmsPref != 0 || mCallPref != 0) {
            mAutoSmsMessage.setEnabled(true);
        } else {
            mAutoSmsMessage.setEnabled(false);
        }
    }

    private void updateAutoText() {
        mAutoText = mPrefs.getString(KEY_AUTO_SMS_MESSAGE, mDefaultText);
        if (TextUtils.isEmpty(mAutoText)) {
            mAutoText = mDefaultText;
        }

    }
}
