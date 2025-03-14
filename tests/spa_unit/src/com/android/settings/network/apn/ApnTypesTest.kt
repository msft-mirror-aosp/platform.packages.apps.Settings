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

package com.android.settings.network.apn

import android.telephony.data.ApnSetting
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.settings.network.apn.ApnTypes.isValid
import com.android.settings.network.apn.ApnTypes.toApnType
import com.android.settingslib.spa.widget.editor.SettingsDropdownCheckOption
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApnTypesTest {

    @Test
    fun isValid_hasSelected() {
        val options = listOf(
            SettingsDropdownCheckOption(text = APN_TYPE, selected = mutableStateOf(true)),
        )

        val isValid = options.isValid()

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValid_hasNotSelected() {
        val options = listOf(
            SettingsDropdownCheckOption(text = APN_TYPE, selected = mutableStateOf(false)),
        )

        val isValid = options.isValid()

        assertThat(isValid).isFalse()
    }

    @Test
    fun toApnType_hasSelected() {
        val options = listOf(
            SettingsDropdownCheckOption(text = APN_TYPE, selected = mutableStateOf(true)),
        )

        val apnType = options.toApnType()

        assertThat(apnType).isEqualTo(APN_TYPE)
    }

    @Test
    fun toApnType_hasNotSelected() {
        val options = listOf(
            SettingsDropdownCheckOption(text = APN_TYPE, selected = mutableStateOf(false)),
        )

        val apnType = options.toApnType()

        assertThat(apnType).isEmpty()
    }

    @Test
    fun getPreSelectedApnType_regular() {
        val apnType = ApnTypes.getPreSelectedApnType(CustomizedConfig())

        assertThat(apnType).isEqualTo("default,mms,supl,dun,hipri,fota,cbs,xcap")
    }

    @Test
    fun getPreSelectedApnType_readOnlyApnTypes() {
        val customizedConfig = CustomizedConfig(
            readOnlyApnTypes = listOf(ApnSetting.TYPE_DUN_STRING),
        )

        val apnType = ApnTypes.getPreSelectedApnType(customizedConfig)

        assertThat(apnType).isEqualTo("default,mms,supl,hipri,fota,cbs,xcap")
    }

    @Test
    fun hasAllApnTypes_allString() {
        val apnTypes = listOf(ApnSetting.TYPE_ALL_STRING)

        val hasAllApnTypes = ApnTypes.hasAllApnTypes(apnTypes)

        assertThat(hasAllApnTypes).isTrue()
    }

    @Test
    fun hasAllApnTypes_allTypes() {
        val apnTypes = listOf(
            ApnSetting.TYPE_DEFAULT_STRING,
            ApnSetting.TYPE_MMS_STRING,
            ApnSetting.TYPE_SUPL_STRING,
            ApnSetting.TYPE_DUN_STRING,
            ApnSetting.TYPE_HIPRI_STRING,
            ApnSetting.TYPE_FOTA_STRING,
            ApnSetting.TYPE_IMS_STRING,
            ApnSetting.TYPE_CBS_STRING,
            ApnSetting.TYPE_IA_STRING,
            ApnSetting.TYPE_EMERGENCY_STRING,
            ApnSetting.TYPE_MCX_STRING,
            ApnSetting.TYPE_XCAP_STRING,
            ApnSetting.TYPE_VSIM_STRING,
            ApnSetting.TYPE_BIP_STRING,
            ApnSetting.TYPE_ENTERPRISE_STRING,
            ApnSetting.TYPE_OEM_PAID_STRING,
            ApnSetting.TYPE_OEM_PRIVATE_STRING,
        )

        val hasAllApnTypes = ApnTypes.hasAllApnTypes(apnTypes)

        assertThat(hasAllApnTypes).isTrue()
    }

    @Test
    fun hasAllApnTypes_allTypesExceptDefault() {
        val apnTypes = listOf(
            ApnSetting.TYPE_MMS_STRING,
            ApnSetting.TYPE_SUPL_STRING,
            ApnSetting.TYPE_DUN_STRING,
            ApnSetting.TYPE_HIPRI_STRING,
            ApnSetting.TYPE_FOTA_STRING,
            ApnSetting.TYPE_IMS_STRING,
            ApnSetting.TYPE_CBS_STRING,
            ApnSetting.TYPE_IA_STRING,
            ApnSetting.TYPE_EMERGENCY_STRING,
            ApnSetting.TYPE_MCX_STRING,
            ApnSetting.TYPE_XCAP_STRING,
            ApnSetting.TYPE_VSIM_STRING,
            ApnSetting.TYPE_BIP_STRING,
            ApnSetting.TYPE_ENTERPRISE_STRING,
            ApnSetting.TYPE_OEM_PAID_STRING,
            ApnSetting.TYPE_OEM_PRIVATE_STRING,
        )

        val hasAllApnTypes = ApnTypes.hasAllApnTypes(apnTypes)

        assertThat(hasAllApnTypes).isFalse()
    }

    private companion object {
        const val APN_TYPE = "type"
    }
}
