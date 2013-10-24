/*
 * Copyright (C) 2012 ParanoidAndroid Project
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

import android.app.ActivityManager;
import android.app.INotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.bluetooth.DeviceListPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class Halo extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_HALO_STATE = "halo_state";
    private static final String KEY_HALO_HIDE = "halo_hide";
    private static final String KEY_HALO_NINJA = "halo_ninja";
    private static final String KEY_HALO_MSGBOX = "halo_msgbox";
    private static final String KEY_HALO_MSGBOX_ANIMATION = "halo_msgbox_animation";
    private static final String KEY_HALO_NOTIFY_COUNT = "halo_notify_count";
    private static final String KEY_HALO_UNLOCK_PING = "halo_unlock_ping";
    private static final String KEY_HALO_REVERSED = "halo_reversed";
    private static final String KEY_HALO_SIZE = "halo_size";
    private static final String KEY_HALO_PAUSE = "halo_pause";
    private static final String KEY_HALO_COLORS = "halo_colors";
    private static final String KEY_HALO_CIRCLE_COLOR = "halo_circle_color";
    private static final String KEY_HALO_EFFECT_COLOR = "halo_effect_color";
    private static final String KEY_HALO_NOTIF_TITLE_COLOR = "halo_notif_title_color";
    private static final String KEY_HALO_NOTIF_DESC_COLOR = "halo_notif_desc_color";
    private static final String KEY_HALO_SPEECH_BUBBLE_COLOR = "halo_speech_bubble_color";
    private static final String KEY_HALO_TEXT_COLOR = "halo_text_color";

    private ListPreference mHaloState;
    private ListPreference mHaloSize;
    private ListPreference mHaloNotifyCount;
    private ListPreference mHaloMsgAnimate;

    private CheckBoxPreference mHaloNinja;
    private CheckBoxPreference mHaloMsgBox;
    private CheckBoxPreference mHaloUnlockPing;
    private CheckBoxPreference mHaloHide;
    private CheckBoxPreference mHaloReversed;
    private CheckBoxPreference mHaloPause;
    private CheckBoxPreference mHaloColors;
    
    private ColorPickerPreference mHaloCircleColor;
    private ColorPickerPreference mHaloEffectColor;
    private ColorPickerPreference mHaloNotifTitleColor;
    private ColorPickerPreference mHaloNotifDescColor;
    private ColorPickerPreference mHaloSpeechBubbleColor;
    private ColorPickerPreference mHaloTextColor;

    private Context mContext;
    private INotificationManager mNotificationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.halo_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        mNotificationManager = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));

        mHaloState = (ListPreference) prefSet.findPreference(KEY_HALO_STATE);
        mHaloState.setValue(String.valueOf((isHaloPolicyBlack() ? "1" : "0")));
        mHaloState.setOnPreferenceChangeListener(this);

        mHaloHide = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_HIDE);
        mHaloHide.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_HIDE, 0) == 1);

        mHaloNinja = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_NINJA);
        mHaloNinja.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_NINJA, 0) == 1);

        mHaloMsgBox = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_MSGBOX);
        mHaloMsgBox.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_MSGBOX, 1) == 1);

        mHaloUnlockPing = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_UNLOCK_PING);
        mHaloUnlockPing.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_UNLOCK_PING, 0) == 1);

        mHaloNotifyCount = (ListPreference) prefSet.findPreference(KEY_HALO_NOTIFY_COUNT);
        try {
            int haloCounter = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.HALO_NOTIFY_COUNT, 4);
            mHaloNotifyCount.setValue(String.valueOf(haloCounter));
        } catch(Exception ex) {
            // fail...
        }
        mHaloNotifyCount.setOnPreferenceChangeListener(this);

        mHaloMsgAnimate = (ListPreference) prefSet.findPreference(KEY_HALO_MSGBOX_ANIMATION);
        try {
            int haloMsgAnimation = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.HALO_MSGBOX_ANIMATION, 2);
            mHaloMsgAnimate.setValue(String.valueOf(haloMsgAnimation));
        } catch(Exception ex) {
            // fail...
        }
        mHaloMsgAnimate.setOnPreferenceChangeListener(this);

        mHaloReversed = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_REVERSED);
        mHaloReversed.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_REVERSED, 1) == 1);

        int isLowRAM = (ActivityManager.isLargeRAM()) ? 0 : 1;
        mHaloPause = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_PAUSE);
        mHaloPause.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_PAUSE, isLowRAM) == 1);

        mHaloSize = (ListPreference) prefSet.findPreference(KEY_HALO_SIZE);
        try {
            float haloSize = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.HALO_SIZE, 1.0f);
            mHaloSize.setValue(String.valueOf(haloSize));  
        } catch(Exception ex) {
            // So what
        }
        mHaloSize.setOnPreferenceChangeListener(this);
        
        mHaloColors = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_COLORS);
        mHaloColors.setChecked(Settings.System.getInt(mContext.getContentResolver(),
        		Settings.System.HALO_COLOR, 0) == 1);
        
        mHaloCircleColor = (ColorPickerPreference) prefSet.findPreference(KEY_HALO_CIRCLE_COLOR);
        mHaloCircleColor.setOnPreferenceChangeListener(this);
        int color = Settings.System.getInt(mContext.getContentResolver(), 
        		Settings.System.HALO_CIRCLE_COLOR, 0xff33b5b3);
        String hex = ColorPickerPreference.convertToARGB(color);
    	mHaloCircleColor.setSummary(hex);
    	mHaloCircleColor.setEnabled(mHaloColors.isChecked());
    	
    	mHaloEffectColor = (ColorPickerPreference) prefSet.findPreference(KEY_HALO_EFFECT_COLOR);
        mHaloEffectColor.setOnPreferenceChangeListener(this);
        color = Settings.System.getInt(mContext.getContentResolver(), 
        		Settings.System.HALO_EFFECT_COLOR, 0xff33b5b3);
        hex = ColorPickerPreference.convertToARGB(color);
    	mHaloEffectColor.setSummary(hex);
    	mHaloEffectColor.setNewPreviewColor(color);
    	mHaloEffectColor.setEnabled(mHaloColors.isChecked());
    	
    	mHaloNotifTitleColor = (ColorPickerPreference) prefSet.findPreference(KEY_HALO_NOTIF_TITLE_COLOR);
        mHaloNotifTitleColor.setOnPreferenceChangeListener(this);
        color = Settings.System.getInt(mContext.getContentResolver(), 
        		Settings.System.HALO_NOTIFICATION_TITLE_COLOR, 0xffffffff);
        hex = ColorPickerPreference.convertToARGB(color);
    	mHaloNotifTitleColor.setSummary(hex);
    	mHaloNotifTitleColor.setNewPreviewColor(color);
    	mHaloNotifTitleColor.setEnabled(mHaloColors.isChecked());

    	mHaloNotifDescColor = (ColorPickerPreference) prefSet.findPreference(KEY_HALO_NOTIF_DESC_COLOR);
        mHaloNotifDescColor.setOnPreferenceChangeListener(this);
        color = Settings.System.getInt(mContext.getContentResolver(), 
        		Settings.System.HALO_NOTIFICATION_DESC_COLOR, 0xff999999);
        hex = ColorPickerPreference.convertToARGB(color);
    	mHaloNotifDescColor.setSummary(hex);
    	mHaloNotifDescColor.setNewPreviewColor(color);
    	mHaloNotifDescColor.setEnabled(mHaloColors.isChecked());
    	
    	mHaloSpeechBubbleColor = (ColorPickerPreference) prefSet.findPreference(KEY_HALO_SPEECH_BUBBLE_COLOR);
        mHaloSpeechBubbleColor.setOnPreferenceChangeListener(this);
        color = Settings.System.getInt(mContext.getContentResolver(), 
        		Settings.System.HALO_SPEECH_BUBBLE_COLOR, 0xff086a99);
        hex = ColorPickerPreference.convertToARGB(color);
    	mHaloSpeechBubbleColor.setSummary(hex);
    	mHaloSpeechBubbleColor.setNewPreviewColor(color);
    	mHaloSpeechBubbleColor.setEnabled(mHaloColors.isChecked());
    	
    	mHaloTextColor = (ColorPickerPreference) prefSet.findPreference(KEY_HALO_TEXT_COLOR);
        mHaloTextColor.setOnPreferenceChangeListener(this);
        color = Settings.System.getInt(mContext.getContentResolver(), 
        		Settings.System.HALO_TEXT_COLOR, 0xffffffff);
        hex = ColorPickerPreference.convertToARGB(color);
    	mHaloTextColor.setSummary(hex);
    	mHaloTextColor.setNewPreviewColor(color);
    	mHaloTextColor.setEnabled(mHaloColors.isChecked());
    }

    private boolean isHaloPolicyBlack() {
        try {
            return mNotificationManager.isHaloPolicyBlack();
        } catch (android.os.RemoteException ex) {
                // System dead
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHaloHide) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_HIDE, mHaloHide.isChecked()
                    ? 1 : 0);
        } else if (preference == mHaloNinja) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_NINJA, mHaloNinja.isChecked()
                    ? 1 : 0);
        } else if (preference == mHaloMsgBox) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_MSGBOX, mHaloMsgBox.isChecked()
                    ? 1 : 0);
        } else if (preference == mHaloUnlockPing) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_UNLOCK_PING, mHaloUnlockPing.isChecked()
                    ? 1 : 0);
        } else if (preference == mHaloReversed) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_REVERSED, mHaloReversed.isChecked()
                    ? 1 : 0);
        } else if (preference == mHaloPause) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_PAUSE, mHaloPause.isChecked()
                    ? 1 : 0);
        } else if (preference == mHaloColors) {
        	Settings.System.putInt(mContext.getContentResolver(),
        			Settings.System.HALO_COLOR, mHaloColors.isChecked() ? 1 : 0);
        	mHaloCircleColor.setEnabled(mHaloColors.isChecked());
        	mHaloEffectColor.setEnabled(mHaloColors.isChecked());
        	mHaloNotifTitleColor.setEnabled(mHaloColors.isChecked());
        	mHaloNotifDescColor.setEnabled(mHaloColors.isChecked());
        	mHaloSpeechBubbleColor.setEnabled(mHaloColors.isChecked());
        	mHaloTextColor.setEnabled(mHaloColors.isChecked());
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHaloSize) {
            float haloSize = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.HALO_SIZE, haloSize);
            return true;
        } else if (preference == mHaloMsgAnimate) {
            int haloMsgAnimation = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.HALO_MSGBOX_ANIMATION, haloMsgAnimation);
            return true;
        } else if (preference == mHaloNotifyCount) {
            int haloNotifyCount = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.HALO_NOTIFY_COUNT, haloNotifyCount);
            return true;
        } else if (preference == mHaloState) {
            boolean state = Integer.valueOf((String) newValue) == 1;
            try {
                mNotificationManager.setHaloPolicyBlack(state);
            } catch (android.os.RemoteException ex) {
                // System dead
            }
            return true;
        } else if (preference == mHaloCircleColor) {
        	String hex = ColorPickerPreference.convertToARGB(
        			Integer.valueOf(String.valueOf(newValue)));
        	preference.setSummary(hex);
        	Settings.System.putInt(getActivity().getContentResolver(),
        			Settings.System.HALO_CIRCLE_COLOR, ColorPickerPreference.convertToColorInt(hex));
        } else if (preference == mHaloEffectColor) {
        	String hex = ColorPickerPreference.convertToARGB(
        			Integer.valueOf(String.valueOf(newValue)));
        	preference.setSummary(hex);
        	Settings.System.putInt(getActivity().getContentResolver(),
        			Settings.System.HALO_EFFECT_COLOR, ColorPickerPreference.convertToColorInt(hex));
        } else if (preference == mHaloNotifTitleColor) {
        	String hex = ColorPickerPreference.convertToARGB(
        			Integer.valueOf(String.valueOf(newValue)));
        	preference.setSummary(hex);
        	Settings.System.putInt(getActivity().getContentResolver(),
        			Settings.System.HALO_NOTIFICATION_TITLE_COLOR, ColorPickerPreference.convertToColorInt(hex));
        } else if (preference == mHaloNotifDescColor) {
        	String hex = ColorPickerPreference.convertToARGB(
        			Integer.valueOf(String.valueOf(newValue)));
        	preference.setSummary(hex);
        	Settings.System.putInt(getActivity().getContentResolver(),
        			Settings.System.HALO_NOTIFICATION_DESC_COLOR, ColorPickerPreference.convertToColorInt(hex));
        } else if (preference == mHaloSpeechBubbleColor) {
        	String hex = ColorPickerPreference.convertToARGB(
        			Integer.valueOf(String.valueOf(newValue)));
        	preference.setSummary(hex);
        	Settings.System.putInt(getActivity().getContentResolver(),
        			Settings.System.HALO_SPEECH_BUBBLE_COLOR, ColorPickerPreference.convertToColorInt(hex));
        } else if (preference == mHaloTextColor) {
        	String hex = ColorPickerPreference.convertToARGB(
        			Integer.valueOf(String.valueOf(newValue)));
        	preference.setSummary(hex);
        	Settings.System.putInt(getActivity().getContentResolver(),
        			Settings.System.HALO_TEXT_COLOR, ColorPickerPreference.convertToColorInt(hex));
        }
        return false;
    }
}
