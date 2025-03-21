/**
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.localepicker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocaleStore;
import com.android.internal.app.SystemLocaleCollector;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A locale picker fragment to show region country and numbering system.
 *
 * <p>It shows suggestions at the top, then the rest of the locales.
 * Allows the user to search for locales using both their native name and their name in the
 * default locale.</p>
 */
public class RegionAndNumberingSystemPickerFragment extends DashboardFragment {

    public static final String EXTRA_TARGET_LOCALE = "extra_target_locale";
    public static final String EXTRA_IS_NUMBERING_SYSTEM = "extra_is_numbering_system";

    private static final String TAG = "RegionAndNumberingSystemPickerFragment";
    private static final String KEY_PREFERENCE_SYSTEM_LOCALE_LIST = "system_locale_list";
    private static final String KEY_PREFERENCE_SYSTEM_LOCALE_SUGGESTED_LIST =
            "system_locale_suggested_list";

    @Nullable
    private SystemLocaleAllListPreferenceController mSystemLocaleAllListPreferenceController;
    @Nullable
    private SystemLocaleSuggestedListPreferenceController mSuggestedListPreferenceController;
    @Nullable
    private LocaleStore.LocaleInfo mLocaleInfo;
    private RecyclerView mRecyclerView;
    private AppBarLayout mAppBarLayout;
    private Activity mActivity;
    private boolean mIsNumberingMode;

    @Override
    public void onCreate(@NonNull Bundle icicle) {
        super.onCreate(icicle);
        mActivity = getActivity();
        if (mActivity == null || mActivity.isFinishing()) {
            Log.d(TAG, "onCreate, no activity or activity is finishing");
            return;
        }

        if (mLocaleInfo == null) {
            Log.d(TAG, "onCreate, can not get localeInfo");
            return;
        }

        mActivity.setTitle(mLocaleInfo.getFullNameNative());
    }

    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
            @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        mAppBarLayout = mActivity.findViewById(R.id.app_bar);
        mAppBarLayout.setExpanded(false /*expanded*/, false /*animate*/);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_view);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.system_language_picker;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private List<AbstractPreferenceController> buildPreferenceControllers(
            @NonNull Context context, @Nullable Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        mLocaleInfo = (LocaleStore.LocaleInfo) getArguments().getSerializable(EXTRA_TARGET_LOCALE);
        mIsNumberingMode = getArguments().getBoolean(EXTRA_IS_NUMBERING_SYSTEM);
        mSuggestedListPreferenceController = new SystemLocaleSuggestedListPreferenceController(
                context, KEY_PREFERENCE_SYSTEM_LOCALE_SUGGESTED_LIST, mLocaleInfo,
                mIsNumberingMode);
        mSystemLocaleAllListPreferenceController = new SystemLocaleAllListPreferenceController(
                context, KEY_PREFERENCE_SYSTEM_LOCALE_LIST, mLocaleInfo, mIsNumberingMode);
        controllers.add(mSuggestedListPreferenceController);
        controllers.add(mSystemLocaleAllListPreferenceController);

        return controllers;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.system_language_picker);

    @Override
    public int getMetricsCategory() {
        return 0;
    }
}
