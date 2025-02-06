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

package com.android.settings.biometrics.face

/**
 * Provide features for FaceSettings page.
 */
open class FaceSettingsFeatureProvider {
    /**
     * Get the description shown in the Face settings page.
     */
    open fun getSettingPageDescription(): Int {
        return 0
    }

    /**
     * Get the footer description for face class3 shown in the Face settings page.
     */
    open fun getSettingPageFooterDescriptionClass3(): Int {
        return com.android.settings.R.string.security_settings_face_settings_footer_class3
    }

    /**
     * Get the footer learn more description shown in the Face settings page.
     */
    open fun getSettingPageFooterLearnMoreDescription(): Int {
        return 0
    }

    /**
     * Get the footer learn more URL.
     */
    open fun getSettingPageFooterLearnMoreUrl(): Int {
        return 0
    }

    companion object {
        @JvmStatic
        val instance = FaceSettingsFeatureProvider()
    }
}