/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.accessibility;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Accessibility settings for the vibration.
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class VibrationSettings extends DashboardFragment {

    private static final String TAG = "VibrationSettings";

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return VibrationScreen.KEY;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.ACCESSIBILITY_VIBRATION;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_accessibility_vibration;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.accessibility_vibration_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @VisibleForTesting
    static boolean isPageSearchEnabled(Context context) {
        final int supportedIntensityLevels = context.getResources().getInteger(
                R.integer.config_vibration_supported_intensity_levels);
        final boolean hasVibrator = context.getSystemService(Vibrator.class).hasVibrator();
        return hasVibrator && supportedIntensityLevels == 1;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.accessibility_vibration_settings) {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return VibrationSettings.isPageSearchEnabled(context);
                }
            };
}
