/*
 * Copyright (C) 2022 ReloadedOS
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

package org.reloadedos.xtras.fragments;

import android.os.Bundle;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.Surface;
import android.view.View;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import org.reloadedos.framework.preference.SecureSettingListPreference;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusBarSettings extends DashboardFragment {

    private static final String KEY_CLOCK_POSITION = "status_bar_clock_position";
    private static final String KEY_CLOCK_AM_PM = "status_bar_am_pm";

    private SecureSettingListPreference mClockPositionPref;
    private SecureSettingListPreference mAmPmPref;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mClockPositionPref = (SecureSettingListPreference) findPreference(KEY_CLOCK_POSITION);
        mAmPmPref = (SecureSettingListPreference) findPreference(KEY_CLOCK_AM_PM);

        boolean hasNotch = hasCenteredCutout(getActivity());
        boolean isRtl = getResources().getConfiguration().getLayoutDirection()
                == View.LAYOUT_DIRECTION_RTL;

        // Adjust clock position pref for RTL and center notch
        int entries = hasNotch ? R.array.status_bar_clock_position_entries_notch
                : R.array.status_bar_clock_position_entries;
        int values = hasNotch ? (isRtl ? R.array.status_bar_clock_position_values_notch_rtl
                : R.array.status_bar_clock_position_values_notch)
                : (isRtl ? R.array.status_bar_clock_position_values_rtl
                : R.array.status_bar_clock_position_values);
        mClockPositionPref.setEntries(entries);
        mClockPositionPref.setEntryValues(values);

        // Disable AM/PM for 24-hour format
        if (DateFormat.is24HourFormat(getActivity())) {
            mAmPmPref.setEnabled(false);
            mAmPmPref.setSummary(R.string.status_bar_am_pm_disabled);
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.reloaded_xtras_statusbar;
    }

    @Override
    protected String getLogTag() {
        return "StatusBarSettings";
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.RELOADED;
    }

    /* returns whether the device has a centered display cutout or not. */
    private static boolean hasCenteredCutout(Context context) {
        Display display = context.getDisplay();
        DisplayCutout cutout = display.getCutout();
        if (cutout != null) {
            Point realSize = new Point();
            display.getRealSize(realSize);

            switch (display.getRotation()) {
                case Surface.ROTATION_0: {
                    Rect rect = cutout.getBoundingRectTop();
                    return !(rect.left <= 0 || rect.right >= realSize.x);
                }
                case Surface.ROTATION_90: {
                    Rect rect = cutout.getBoundingRectLeft();
                    return !(rect.top <= 0 || rect.bottom >= realSize.y);
                }
                case Surface.ROTATION_180: {
                    Rect rect = cutout.getBoundingRectBottom();
                    return !(rect.left <= 0 || rect.right >= realSize.x);
                }
                case Surface.ROTATION_270: {
                    Rect rect = cutout.getBoundingRectRight();
                    return !(rect.top <= 0 || rect.bottom >= realSize.y);
                }
            }
        }
        return false;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.reloaded_xtras_statusbar);
}
