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
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.abc.settings.preferences.CustomSeekBarPreference;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.List;
import java.util.ArrayList;

public class OtherSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String APPS_SECURITY = "apps_security";
    private static final String SMS_OUTGOING_CHECK_MAX_COUNT = "sms_outgoing_check_max_count";
    private static final String SCREENSHOT_DELAY = "screenshot_delay";
    private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";

    private ListPreference mSmsCount;
    private ListPreference mMsob;
    private int mSmsCountValue;
    private CustomSeekBarPreference mScreenshotDelay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.abc_other_settings);
        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        PreferenceCategory appsSecCategory = (PreferenceCategory) findPreference(APPS_SECURITY);

        mSmsCount = (ListPreference) findPreference(SMS_OUTGOING_CHECK_MAX_COUNT);
        mSmsCountValue = Settings.Global.getInt(resolver,
                Settings.Global.SMS_OUTGOING_CHECK_MAX_COUNT, 30);
        mSmsCount.setValue(Integer.toString(mSmsCountValue));
        mSmsCount.setSummary(mSmsCount.getEntry());
        mSmsCount.setOnPreferenceChangeListener(this);
        if (!Utils.isVoiceCapable(getActivity())) {
            appsSecCategory.removePreference(mSmsCount);
            prefScreen.removePreference(appsSecCategory);
        }

        mScreenshotDelay = (CustomSeekBarPreference) findPreference(SCREENSHOT_DELAY);
        int screenshotDelay = Settings.System.getIntForUser(resolver,
                Settings.System.SCREENSHOT_DELAY, 1000, UserHandle.USER_CURRENT);
        mScreenshotDelay.setValue(screenshotDelay / 1);
        mScreenshotDelay.setOnPreferenceChangeListener(this);

        mMsob = (ListPreference) prefScreen.findPreference(PREF_MEDIA_SCANNER_ON_BOOT);
        mMsob.setOnPreferenceChangeListener(this);
        mMsob.setValue(Integer.toString(Settings.System.getInt(getActivity()
                        .getContentResolver(), Settings.System.PREF_MEDIA_SCANNER_ON_BOOT,
                0)));
        mMsob.setSummary(mMsob.getEntry());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mSmsCount) {
            mSmsCountValue = Integer.valueOf((String) newValue);
            int index = mSmsCount.findIndexOfValue((String) newValue);
            mSmsCount.setSummary(
                    mSmsCount.getEntries()[index]);
            Settings.Global.putInt(resolver,
                    Settings.Global.SMS_OUTGOING_CHECK_MAX_COUNT, mSmsCountValue);
            return true;
        } else if (preference == mScreenshotDelay) {
            int screenshotDelay = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.SCREENSHOT_DELAY, screenshotDelay * 1, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mMsob) {
            int val = Integer.parseInt((String) newValue);
            int index = mMsob.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.PREF_MEDIA_SCANNER_ON_BOOT,val);
            mMsob.setSummary(mMsob.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ABC;
    }
}
