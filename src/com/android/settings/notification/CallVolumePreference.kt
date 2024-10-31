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

package com.android.settings.notification

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.STREAM_BLUETOOTH_SCO
import android.media.AudioManager.STREAM_VOICE_CALL
import android.os.UserHandle
import android.os.UserManager.DISALLOW_ADJUST_VOLUME
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.RestrictedLockUtilsInternal
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.NoOpKeyedObservable
import com.android.settingslib.metadata.PersistentPreference
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceIconProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceRestrictionProvider
import com.android.settingslib.metadata.RangeValue
import com.android.settingslib.preference.PreferenceBinding

// LINT.IfChange
open class CallVolumePreference :
    PreferenceMetadata,
    PreferenceBinding,
    PersistentPreference<Int>,
    RangeValue,
    PreferenceAvailabilityProvider,
    PreferenceIconProvider,
    PreferenceRestrictionProvider {
    override val key: String
        get() = KEY

    override val title: Int
        get() = R.string.call_volume_option_title

    override fun getIcon(context: Context) = R.drawable.ic_local_phone_24_lib

    override fun isAvailable(context: Context) =
        context.resources.getBoolean(R.bool.config_show_call_volume) &&
                !createAudioHelper(context).isSingleVolume()

    override fun isRestricted(context: Context) =
        RestrictedLockUtilsInternal.hasBaseUserRestriction(
            context,
            DISALLOW_ADJUST_VOLUME,
            UserHandle.myUserId()
        ) || RestrictedLockUtilsInternal.checkIfRestrictionEnforced(
            context,
            DISALLOW_ADJUST_VOLUME,
            UserHandle.myUserId()
        ) != null

    override fun storage(context: Context): KeyValueStore {
        val helper = createAudioHelper(context)
        return object : NoOpKeyedObservable<String>(), KeyValueStore {
            override fun contains(key: String) = key == KEY

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any> getValue(key: String, valueType: Class<T>) =
                helper.getStreamVolume(getAudioStream(context)) as T

            override fun <T : Any> setValue(key: String, valueType: Class<T>, value: T?) {
                helper.setStreamVolume(getAudioStream(context), value as Int)
            }
        }
    }

    override fun getMinValue(context: Context) =
        createAudioHelper(context).getMinVolume(getAudioStream(context))

    override fun getMaxValue(context: Context) =
        createAudioHelper(context).getMaxVolume(getAudioStream(context))

    override fun createWidget(context: Context) = VolumeSeekBarPreference(context)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        (preference as VolumeSeekBarPreference).setStream(getAudioStream(preference.context))
    }

    open fun createAudioHelper(context: Context) = AudioHelper(context)

    @Suppress("DEPRECATION")
    fun getAudioStream(context: Context): Int {
        val audioManager = context.getSystemService(AudioManager::class.java)
        return when {
            audioManager.isBluetoothScoOn() -> STREAM_BLUETOOTH_SCO
            else -> STREAM_VOICE_CALL
        }
    }

    companion object {
        const val KEY = "call_volume"
    }
}
// LINT.ThenChange(CallVolumePreferenceController.java)
