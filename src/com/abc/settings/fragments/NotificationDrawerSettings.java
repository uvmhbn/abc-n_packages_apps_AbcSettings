/*
 * Copyright (C) 2016 The ABC rom
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
package com.abc.settings.fragments;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.abc.settings.preferences.CustomSeekBarPreference;

public class NotificationDrawerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_SYSUI_QQS_COUNT = "sysui_qqs_count_key";
    private static final String NOTIFICATION_MODE = "notification_mode";

    private CustomSeekBarPreference mSysuiQqsCount;
    private ListPreference mNotificationMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.abc_notification_drawer_settings);
        ContentResolver resolver = getActivity().getContentResolver();

        mSysuiQqsCount = (CustomSeekBarPreference) findPreference(KEY_SYSUI_QQS_COUNT);
        int SysuiQqsCount = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QQS_COUNT, 5);
        mSysuiQqsCount.setValue(SysuiQqsCount / 1);
        mSysuiQqsCount.setOnPreferenceChangeListener(this);

        mNotificationMode = (ListPreference) findPreference(NOTIFICATION_MODE);
        mNotificationMode.setOnPreferenceChangeListener(this);
        int headsupMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_USER_ENABLED,
                1, UserHandle.USER_CURRENT);
        int tickerMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_TICKER,
                0, UserHandle.USER_CURRENT);
        int notificationMode;
        if (headsupMode == 1) {
            notificationMode = 0;
        } else if (tickerMode != 0) {
            notificationMode = tickerMode == 1 ? 1 : 2;
        } else {
            notificationMode = 3;
        }
        mNotificationMode.setValue(String.valueOf(notificationMode));
        mNotificationMode.setSummary(mNotificationMode.getEntry());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mSysuiQqsCount) {
            int SysuiQqsCount = (Integer) newValue;
            Settings.Secure.putInt(resolver,
                    Settings.Secure.QQS_COUNT, SysuiQqsCount * 1);
            return true;
        } else if (preference.equals(mNotificationMode)) {
            int notificationMode = Integer.parseInt(((String) newValue).toString());
            int headsupMode;
            int tickerMode;
            if (notificationMode == 0) {
                headsupMode = 1;
                tickerMode = 0;
            } else if (notificationMode == 1) {
                headsupMode = 0;
                tickerMode = 1;
            } else if (notificationMode == 2) {
                headsupMode = 0;
                tickerMode = 2;
            }else {
                headsupMode = 0;
                tickerMode = 0;
            }
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_USER_ENABLED, headsupMode, UserHandle.USER_CURRENT);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TICKER, tickerMode, UserHandle.USER_CURRENT);
            int index = mNotificationMode.findIndexOfValue((String) newValue);
            mNotificationMode.setSummary(
                    mNotificationMode.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ABC;
    }
} 
