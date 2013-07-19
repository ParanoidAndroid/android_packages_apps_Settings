/*
 * Copyright (C) 2013 Android Open Kang Project
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

package com.android.settings.cyanogenmod.autosms;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Telephony.Sms.Intents;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

public class AutoSmsService extends Service {
    private TelephonyManager mTelephony;

    private ContentObserver mQuietHoursObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            MessagingHelper.scheduleService(AutoSmsService.this);
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        private boolean mIncomingCall = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                mIncomingCall = true;
            }
            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                // Don't message if call was answered
                mIncomingCall = false;
            }
            if (state == TelephonyManager.CALL_STATE_IDLE && mIncomingCall) {
                // Call Received and now inactive
                mIncomingCall = false;
                int setting = MessagingHelper.getAutoCallResponseSetting(AutoSmsService.this);
                if (setting != MessagingHelper.DEFAULT_DISABLED
                        && MessagingHelper.inQuietHours(AutoSmsService.this)) {
                    MessagingHelper.sendAutoResponse(AutoSmsService.this, incomingNumber, setting);
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    private BroadcastReceiver mSmsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int setting = MessagingHelper.getAutoSmsResponseSetting(context);
            if (setting != MessagingHelper.DEFAULT_DISABLED && MessagingHelper.inQuietHours(context)) {
                SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
                SmsMessage msg = msgs[0];
                String incomingNumber = msg.getOriginatingAddress();
                MessagingHelper.sendAutoResponse(context, incomingNumber, setting);
            }
        }
    };

    @Override
    public void onCreate() {
        mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephony.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.SMS_RECEIVED_ACTION);
        registerReceiver(mSmsReceiver, filter);

        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.QUIET_HOURS_ENABLED), false, mQuietHoursObserver);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.QUIET_HOURS_START), false, mQuietHoursObserver);
        resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.QUIET_HOURS_END), false, mQuietHoursObserver);
    }

    @Override
    public void onDestroy() {
        if (mTelephony != null) {
            mTelephony.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        mPhoneStateListener = null;
        unregisterReceiver(mSmsReceiver);
        getContentResolver().unregisterContentObserver(mQuietHoursObserver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
