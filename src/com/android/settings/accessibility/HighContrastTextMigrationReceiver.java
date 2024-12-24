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

package com.android.settings.accessibility;

import static com.android.settings.SettingsActivity.EXTRA_FRAGMENT_ARG_KEY;
import static com.android.settings.SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS;
import static com.android.settings.accessibility.AccessibilityUtil.State.OFF;
import static com.android.settings.accessibility.AccessibilityUtil.State.ON;

import android.annotation.IntDef;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.graphics.hwui.flags.Flags;
import com.android.settings.R;

import com.google.common.annotations.VisibleForTesting;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Handling smooth migration to the new high contrast text appearance
 */
public class HighContrastTextMigrationReceiver extends BroadcastReceiver {
    private static final String TAG = HighContrastTextMigrationReceiver.class.getSimpleName();
    @VisibleForTesting
    static final String NOTIFICATION_CHANNEL = "accessibility_notification_channel";
    @VisibleForTesting
    static final String ACTION_RESTORED =
            "com.android.settings.accessibility.ACTION_HIGH_CONTRAST_TEXT_RESTORED";
    @VisibleForTesting
    static final int NOTIFICATION_ID = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            PromptState.UNKNOWN,
            PromptState.PROMPT_SHOWN,
            PromptState.PROMPT_UNNECESSARY,
    })
    public @interface PromptState {
        int UNKNOWN = 0;
        int PROMPT_SHOWN = 1;
        int PROMPT_UNNECESSARY = 2;
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (!Flags.highContrastTextSmallTextRect()) {
            return;
        }

        if (ACTION_RESTORED.equals(intent.getAction())) {
            Log.i(TAG, "HCT attempted to be restored from backup; showing notification for userId: "
                    + context.getUserId());
            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_HCT_RECT_PROMPT_STATUS,
                    PromptState.PROMPT_SHOWN);
            showNotification(context);
        } else if (Intent.ACTION_PRE_BOOT_COMPLETED.equals(intent.getAction())) {
            final boolean hasSeenPromptIfNecessary = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_HCT_RECT_PROMPT_STATUS, PromptState.UNKNOWN)
                    != PromptState.UNKNOWN;
            if (hasSeenPromptIfNecessary) {
                Log.i(TAG, "Has seen HCT prompt if necessary; skip HCT migration for userId: "
                        + context.getUserId());
                return;
            }

            final boolean isHctEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, OFF) == ON;
            if (isHctEnabled) {
                Log.i(TAG, "HCT enabled before OTA update; performing migration for userId: "
                        + context.getUserId());
                Settings.Secure.putInt(context.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED,
                        OFF);
                Settings.Secure.putInt(context.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HCT_RECT_PROMPT_STATUS,
                        PromptState.PROMPT_SHOWN);
                showNotification(context);
            } else {
                Log.i(TAG,
                        "HCT was not enabled before OTA update; not performing migration for "
                                + "userId: " + context.getUserId());
                Settings.Secure.putInt(context.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HCT_RECT_PROMPT_STATUS,
                        PromptState.PROMPT_UNNECESSARY);
            }
        }
    }

    private void showNotification(Context context) {
        Notification.Builder notificationBuilder = new Notification.Builder(context,
                NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_settings_24dp)
                .setContentTitle(context.getString(
                        R.string.accessibility_notification_high_contrast_text_title))
                .setContentText(context.getString(
                        R.string.accessibility_notification_high_contrast_text_content))
                .setFlag(Notification.FLAG_NO_CLEAR, true);

        Intent settingsIntent = new Intent(Settings.ACTION_TEXT_READING_SETTINGS);
        settingsIntent.setPackage(context.getPackageName());
        if (settingsIntent.resolveActivity(context.getPackageManager()) != null) {
            Bundle fragmentArgs = new Bundle();
            fragmentArgs.putString(EXTRA_FRAGMENT_ARG_KEY,
                    TextReadingPreferenceFragment.HIGH_TEXT_CONTRAST_KEY);
            settingsIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, fragmentArgs);
            PendingIntent settingsPendingIntent = PendingIntent.getActivity(context,
                    /* requestCode = */ 0, settingsIntent, PendingIntent.FLAG_IMMUTABLE);

            Notification.Action settingsAction = new Notification.Action.Builder(
                    /* icon= */ null,
                    context.getString(
                            R.string.accessibility_notification_high_contrast_text_action),
                    settingsPendingIntent
            ).build();

            notificationBuilder
                    .setContentIntent(settingsPendingIntent)
                    .addAction(settingsAction)
                    .setAutoCancel(true);
        }
        NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);
        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL,
                context.getString(R.string.accessibility_settings),
                NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
