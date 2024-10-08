/*
 * Copyright (C) 2023 The Android Open Source Project
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
import android.provider.Settings
import android.telephony.SubscriptionManager
import com.android.settingslib.spaprivileged.settingsprovider.settingsGlobalChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

/**
 * Flow for mobile data enabled changed event.
 *
 * Note: This flow can only notify enabled status changes, cannot provide the latest status.
 */
fun Context.mobileDataEnabledFlow(subId: Int, sendInitialValue: Boolean = true): Flow<Unit> {
    val flow = settingsGlobalChangeFlow(Settings.Global.MOBILE_DATA, sendInitialValue)
    return when (subId) {
        SubscriptionManager.INVALID_SUBSCRIPTION_ID -> flow
        else -> {
            val subIdFlow = settingsGlobalChangeFlow(
                name = Settings.Global.MOBILE_DATA + subId,
                sendInitialValue = false,
            )
            merge(flow, subIdFlow)
        }
    }
}
