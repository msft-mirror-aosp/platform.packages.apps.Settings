/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.settings.display.darkmode;

import android.content.Context;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.util.AttributeSet;

import com.android.settings.R;
import com.android.settingslib.PrimarySwitchPreference;

/**
 * component for the display settings dark ui summary
 */
public class DarkModePreference extends PrimarySwitchPreference {

    private DarkModeObserver mDarkModeObserver;
    private boolean isCatalystEnabled;

    public DarkModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets if catalyst is enabled on the preference.
     */
    public void setCatalystEnabled(boolean catalystEnabled) {
        isCatalystEnabled = catalystEnabled;
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (!isCatalystEnabled) {
            Context context = getContext();
            mDarkModeObserver = new DarkModeObserver(context);
            Runnable callback = () -> {
                PowerManager powerManager = context.getSystemService(PowerManager.class);
                final boolean batterySaver = powerManager.isPowerSaveMode();
                final boolean active = (context.getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_YES) != 0;
                setSwitchEnabled(!batterySaver);
                updateSummary(batterySaver, active);
            };
            mDarkModeObserver.subscribe(callback);
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if (!isCatalystEnabled) {
            mDarkModeObserver.unsubscribe();
        }
    }

    private void updateSummary(boolean batterySaver, boolean active) {
        if (batterySaver) {
            final int stringId = active
                    ? R.string.dark_ui_mode_disabled_summary_dark_theme_on
                    : R.string.dark_ui_mode_disabled_summary_dark_theme_off;
            setSummary(getContext().getString(stringId));
        } else {
            setSummary(AutoDarkTheme.getStatus(getContext(), active));
        }
    }
}
