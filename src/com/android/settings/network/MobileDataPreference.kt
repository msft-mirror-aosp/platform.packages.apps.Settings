/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.settings.network

import android.content.Context
import android.telephony.SubscriptionManager
import com.android.settings.R
import com.android.settings.network.telephony.MobileDataRepository
import com.android.settings.network.telephony.SubscriptionRepository
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.NoOpKeyedObservable
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.ReadWritePermit
import com.android.settingslib.metadata.SensitivityLevel
import com.android.settingslib.metadata.SwitchPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MobileDataPreference :
    SwitchPreference(
        KEY,
        R.string.mobile_data_settings_title,
        R.string.mobile_data_settings_summary,
    ),
    PreferenceAvailabilityProvider {

    override fun isAvailable(context: Context) =
        SubscriptionRepository(context).getSelectableSubscriptionInfoList().any {
            it.simSlotIndex > -1
        }

    override fun storage(context: Context): KeyValueStore = MobileDataStorage(context)

    override fun getReadPermit(context: Context, myUid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override fun getWritePermit(context: Context, value: Boolean?, myUid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override val sensitivityLevel
        get() = SensitivityLevel.LOW_SENSITIVITY

    @Suppress("UNCHECKED_CAST")
    private class MobileDataStorage(private val context: Context) :
        NoOpKeyedObservable<String>(), KeyValueStore {

        override fun contains(key: String) = key == KEY

        override fun <T : Any> getValue(key: String, valueType: Class<T>): T {
            val subId = SubscriptionManager.getDefaultDataSubscriptionId()
            val flow = MobileDataRepository(context).isMobileDataEnabledFlow(subId)
            return runBlocking { flow.first() } as T
        }

        override fun <T : Any> setValue(key: String, valueType: Class<T>, value: T?) {
            val subId = SubscriptionManager.getDefaultDataSubscriptionId()
            MobileDataRepository(context).setMobileDataEnabled(subId, value as Boolean)
        }
    }

    companion object {
        const val KEY = "mobile_data"
    }
}
