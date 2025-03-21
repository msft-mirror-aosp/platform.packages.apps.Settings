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

package com.android.settings.network.telephony;

import static com.android.settings.network.telephony.EnabledNetworkModePreferenceControllerHelperKt.getNetworkModePreferenceType;

import android.content.Context;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.RadioAccessFamily;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.satellite.SatelliteManager;
import android.telephony.satellite.SatelliteModemStateCallback;
import android.telephony.satellite.SelectedNbIotSatelliteSubscriptionCallback;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.network.CarrierConfigCache;
import com.android.settings.network.telephony.mode.NetworkModes;

/**
 * Preference controller for "Preferred network mode"
 */
public class PreferredNetworkModePreferenceController extends BasePreferenceController
        implements ListPreference.OnPreferenceChangeListener, DefaultLifecycleObserver {
    private static final String TAG = "PrefNetworkModeCtrl";

    private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    private CarrierConfigCache mCarrierConfigCache;
    private TelephonyManager mTelephonyManager;
    private boolean mIsGlobalCdma;
    private SatelliteManager mSatelliteManager;
    private Preference mPreference;
    private boolean mIsSatelliteSessionStarted = false;
    private boolean mIsCurrentSubscriptionForSatellite = false;

    @VisibleForTesting
    final SelectedNbIotSatelliteSubscriptionCallback mSelectedNbIotSatelliteSubscriptionCallback =
            new SelectedNbIotSatelliteSubscriptionCallback() {
                @Override
                public void onSelectedNbIotSatelliteSubscriptionChanged(int selectedSubId) {
                    mIsCurrentSubscriptionForSatellite = selectedSubId == mSubId;
                    updateState(mPreference);
                }
            };

    @VisibleForTesting
    final SatelliteModemStateCallback mSatelliteModemStateCallback =
            new SatelliteModemStateCallback() {
                @Override
                public void onSatelliteModemStateChanged(int state) {
                    switch (state) {
                        case SatelliteManager.SATELLITE_MODEM_STATE_OFF:
                        case SatelliteManager.SATELLITE_MODEM_STATE_UNAVAILABLE:
                        case SatelliteManager.SATELLITE_MODEM_STATE_UNKNOWN:
                            if (mIsSatelliteSessionStarted) {
                                mIsSatelliteSessionStarted = false;
                                updateState(mPreference);
                            }
                            break;
                        default:
                            if (!mIsSatelliteSessionStarted) {
                                mIsSatelliteSessionStarted = true;
                                updateState(mPreference);
                            }
                            break;
                    }
                }
            };

    public PreferredNetworkModePreferenceController(Context context, String key) {
        super(context, key);
        mCarrierConfigCache = CarrierConfigCache.getInstance(context);
        mSatelliteManager = context.getSystemService(SatelliteManager.class);
    }

    @Override
    public int getAvailabilityStatus() {
        return getNetworkModePreferenceType(mContext, mSubId)
                == NetworkModePreferenceType.PreferredNetworkMode
                ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public void updateState(Preference preference) {
        if (preference == null) {
            return;
        }
        super.updateState(preference);
        preference.setEnabled(!(mIsCurrentSubscriptionForSatellite && mIsSatelliteSessionStarted));
        final ListPreference listPreference = (ListPreference) preference;
        final int networkMode = getPreferredNetworkMode();
        listPreference.setValue(Integer.toString(networkMode));
        listPreference.setSummary(getPreferredNetworkModeSummaryResId(networkMode));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object object) {
        final int newPreferredNetworkMode = Integer.parseInt((String) object);

        mTelephonyManager.setAllowedNetworkTypesForReason(
                TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_USER,
                RadioAccessFamily.getRafFromNetworkType(newPreferredNetworkMode));

        final ListPreference listPreference = (ListPreference) preference;
        listPreference.setSummary(getPreferredNetworkModeSummaryResId(newPreferredNetworkMode));
        return true;
    }

    public void init(int subId) {
        mSubId = subId;
        final PersistableBundle carrierConfig = mCarrierConfigCache.getConfigForSubId(mSubId);
        mTelephonyManager = mContext.getSystemService(TelephonyManager.class)
                .createForSubscriptionId(mSubId);

        mIsGlobalCdma = mTelephonyManager.isLteCdmaEvdoGsmWcdmaEnabled()
                && carrierConfig.getBoolean(CarrierConfigManager.KEY_SHOW_CDMA_CHOICES_BOOL);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (com.android.settings.flags.Flags.satelliteOemSettingsUxMigration()) {
            if (mSatelliteManager != null) {
                try {
                    mSatelliteManager.registerForModemStateChanged(
                            mContext.getMainExecutor(), mSatelliteModemStateCallback);
                    mSatelliteManager.registerForSelectedNbIotSatelliteSubscriptionChanged(
                            mContext.getMainExecutor(),
                            mSelectedNbIotSatelliteSubscriptionCallback);
                } catch (IllegalStateException e) {
                    Log.w(TAG, "IllegalStateException : " + e);
                }
            }
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        if (com.android.settings.flags.Flags.satelliteOemSettingsUxMigration()) {
            if (mSatelliteManager != null) {
                try {
                    mSatelliteManager.unregisterForModemStateChanged(mSatelliteModemStateCallback);
                    mSatelliteManager.unregisterForSelectedNbIotSatelliteSubscriptionChanged(
                            mSelectedNbIotSatelliteSubscriptionCallback);
                } catch (IllegalStateException e) {
                    Log.w(TAG, "IllegalStateException : " + e);
                }
            }
        }
    }

    private int getPreferredNetworkMode() {
        if (mTelephonyManager == null) {
            Log.w(TAG, "TelephonyManager is null");
            return NetworkModes.NETWORK_MODE_UNKNOWN;
        }
        return RadioAccessFamily.getNetworkTypeFromRaf(
                (int) mTelephonyManager.getAllowedNetworkTypesForReason(
                        TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_USER));
    }

    private int getPreferredNetworkModeSummaryResId(int networkMode) {
        switch (networkMode) {
            case TelephonyManager.NETWORK_MODE_TDSCDMA_GSM_WCDMA:
                return R.string.preferred_network_mode_tdscdma_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_TDSCDMA_GSM:
                return R.string.preferred_network_mode_tdscdma_gsm_summary;
            case TelephonyManager.NETWORK_MODE_WCDMA_PREF:
                return R.string.preferred_network_mode_wcdma_perf_summary;
            case TelephonyManager.NETWORK_MODE_GSM_ONLY:
                return R.string.preferred_network_mode_gsm_only_summary;
            case TelephonyManager.NETWORK_MODE_TDSCDMA_WCDMA:
                return R.string.preferred_network_mode_tdscdma_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_WCDMA_ONLY:
                return R.string.preferred_network_mode_wcdma_only_summary;
            case TelephonyManager.NETWORK_MODE_GSM_UMTS:
                return R.string.preferred_network_mode_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_CDMA_EVDO:
                return mTelephonyManager.isLteCdmaEvdoGsmWcdmaEnabled()
                        ? R.string.preferred_network_mode_cdma_summary
                        : R.string.preferred_network_mode_cdma_evdo_summary;
            case TelephonyManager.NETWORK_MODE_CDMA_NO_EVDO:
                return R.string.preferred_network_mode_cdma_only_summary;
            case TelephonyManager.NETWORK_MODE_EVDO_NO_CDMA:
                return R.string.preferred_network_mode_evdo_only_summary;
            case TelephonyManager.NETWORK_MODE_LTE_TDSCDMA:
                return R.string.preferred_network_mode_lte_tdscdma_summary;
            case TelephonyManager.NETWORK_MODE_LTE_ONLY:
                return R.string.preferred_network_mode_lte_summary;
            case TelephonyManager.NETWORK_MODE_LTE_TDSCDMA_GSM:
                return R.string.preferred_network_mode_lte_tdscdma_gsm_summary;
            case TelephonyManager.NETWORK_MODE_LTE_TDSCDMA_GSM_WCDMA:
                return R.string.preferred_network_mode_lte_tdscdma_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_LTE_GSM_WCDMA:
                return R.string.preferred_network_mode_lte_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_LTE_CDMA_EVDO:
                return R.string.preferred_network_mode_lte_cdma_evdo_summary;
            case TelephonyManager.NETWORK_MODE_TDSCDMA_ONLY:
                return R.string.preferred_network_mode_tdscdma_summary;
            case TelephonyManager.NETWORK_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                return R.string.preferred_network_mode_lte_tdscdma_cdma_evdo_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
                if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA
                        || mIsGlobalCdma
                        || MobileNetworkUtils.isWorldMode(mContext, mSubId)) {
                    return R.string.preferred_network_mode_lte_cdma_evdo_gsm_wcdma_summary;
                } else {
                    return R.string.preferred_network_mode_lte_summary;
                }
            case TelephonyManager.NETWORK_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                return R.string.preferred_network_mode_tdscdma_cdma_evdo_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_GLOBAL:
                return R.string.preferred_network_mode_cdma_evdo_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_LTE_TDSCDMA_WCDMA:
                return R.string.preferred_network_mode_lte_tdscdma_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_LTE_WCDMA:
                return R.string.preferred_network_mode_lte_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_NR_ONLY:
                return R.string.preferred_network_mode_nr_only_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE:
                return R.string.preferred_network_mode_nr_lte_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_CDMA_EVDO:
                return R.string.preferred_network_mode_nr_lte_cdma_evdo_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_GSM_WCDMA:
                return R.string.preferred_network_mode_nr_lte_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_CDMA_EVDO_GSM_WCDMA:
                return R.string.preferred_network_mode_global_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_WCDMA:
                return R.string.preferred_network_mode_nr_lte_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_TDSCDMA:
                return R.string.preferred_network_mode_nr_lte_tdscdma_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_TDSCDMA_GSM:
                return R.string.preferred_network_mode_nr_lte_tdscdma_gsm_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_TDSCDMA_WCDMA:
                return R.string.preferred_network_mode_nr_lte_tdscdma_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_TDSCDMA_GSM_WCDMA:
                return R.string.preferred_network_mode_nr_lte_tdscdma_gsm_wcdma_summary;
            case TelephonyManager.NETWORK_MODE_NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                return R.string.preferred_network_mode_nr_lte_tdscdma_cdma_evdo_gsm_wcdma_summary;
            default:
                return R.string.preferred_network_mode_global_summary;
        }
    }
}
