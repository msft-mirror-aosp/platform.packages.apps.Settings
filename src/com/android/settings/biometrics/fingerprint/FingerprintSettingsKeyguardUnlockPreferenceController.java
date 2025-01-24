/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.settings.biometrics.fingerprint;

import static android.provider.Settings.Secure.FINGERPRINT_KEYGUARD_ENABLED;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.android.settings.Utils;
import com.android.settings.biometrics.activeunlock.ActiveUnlockStatusUtils;

public class FingerprintSettingsKeyguardUnlockPreferenceController
        extends FingerprintSettingsPreferenceController {

    private static final int ON = 1;
    private static final int OFF = 0;
    private static final int DEFAULT = ON;

    private FingerprintManager mFingerprintManager;

    public FingerprintSettingsKeyguardUnlockPreferenceController(
            @NonNull Context context, @NonNull String key) {
        super(context, key);
        mFingerprintManager = Utils.getFingerprintManagerOrNull(context);
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
                FINGERPRINT_KEYGUARD_ENABLED, DEFAULT, getUserId()) == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putIntForUser(mContext.getContentResolver(),
                FINGERPRINT_KEYGUARD_ENABLED, isChecked ? ON : OFF, getUserId());
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!FingerprintSettings.isFingerprintHardwareDetected(mContext)) {
            preference.setEnabled(false);
        } else if (!mFingerprintManager.hasEnrolledTemplates(getUserId())) {
            preference.setEnabled(false);
        } else if (getRestrictingAdmin() != null) {
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        final ActiveUnlockStatusUtils activeUnlockStatusUtils =
                new ActiveUnlockStatusUtils(mContext);
        if (activeUnlockStatusUtils.isAvailable()) {
            return getAvailabilityFromRestrictingAdmin();
        }
        if (!Utils.hasFingerprintHardware(mContext)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return getAvailabilityFromRestrictingAdmin();
    }

    private int getAvailabilityFromRestrictingAdmin() {
        return getRestrictingAdmin() != null ? DISABLED_FOR_USER : AVAILABLE;
    }
}
