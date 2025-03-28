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

package com.android.settings.notification.modes;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.notification.modes.ZenMode;
import com.android.settingslib.widget.LayoutPreference;

import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Consumer;

class ZenModeEditNamePreferenceController extends AbstractZenModePreferenceController {

    private final Consumer<String> mModeNameSetter;
    @Nullable private TextInputLayout mInputLayout;
    @Nullable private EditText mEditText;
    private boolean mIsSettingText;

    ZenModeEditNamePreferenceController(@NonNull Context context, @NonNull String key,
            @NonNull Consumer<String> modeNameSetter) {
        super(context, key);
        mModeNameSetter = modeNameSetter;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (mEditText == null) {
            LayoutPreference pref = checkNotNull(screen.findPreference(getPreferenceKey()));
            mInputLayout = checkNotNull(pref.findViewById(R.id.edit_input_layout));
            mEditText = checkNotNull(pref.findViewById(android.R.id.edit));

            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mIsSettingText) {
                        return;
                    }
                    mModeNameSetter.accept(s.toString());
                    updateErrorState(s.toString());
                }
            });
        }
    }

    @Override
    void updateState(Preference preference, @NonNull ZenMode zenMode) {
        if (mEditText != null) {
            mIsSettingText = true;
            try {
                String currentText = mEditText.getText().toString();
                String modeName = zenMode.getName();
                if (!modeName.equals(currentText)) {
                    mEditText.setText(modeName);
                }
                updateErrorState(modeName);
            } finally {
                mIsSettingText = false;
            }
        }
    }

    private void updateErrorState(String currentName) {
        checkState(mInputLayout != null);
        if (TextUtils.isEmpty(currentName)) {
            mInputLayout.setError(
                    mContext.getString(R.string.zen_mode_edit_name_empty_error));
        } else {
            mInputLayout.setError(null);
        }
    }
}
