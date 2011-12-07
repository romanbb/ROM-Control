
package com.roman.tweaks.activities;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.tweaks.R;

public class PullDownActivity extends PreferenceActivity {

    /*
     * this class contains customziations for the expanded statusbar
     */

    public static final String PREF_SWIPE_TO_CLEAR = "swipe_to_clear";
    public static final String PREF_LONG_CLICK_TO_CLEAR = "long_clock_to_clear";
    public static final String PREF_ALTERNATE_LAYOUT = "alternate_layout";

    private CheckBoxPreference mSwipeToClearPreference;
    private CheckBoxPreference mLongPressToClearPreference;
    private CheckBoxPreference mUseAlternateWidgetLayoutPreference;

    public void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);
        addPreferencesFromResource(R.xml.pulldown_prefs);

        mSwipeToClearPreference = (CheckBoxPreference) findPreference(PREF_SWIPE_TO_CLEAR);
        mSwipeToClearPreference.setChecked(Settings.System.getInt(getContentResolver(),
                "tweaks_swipe_to_clear_notifications", 1) == 1);

        mLongPressToClearPreference = (CheckBoxPreference) findPreference(PREF_LONG_CLICK_TO_CLEAR);
        mLongPressToClearPreference.setChecked(Settings.System.getInt(getContentResolver(),
                "tweaks_long_press_to_clear_notifications", 0) == 1);

        mUseAlternateWidgetLayoutPreference = (CheckBoxPreference) findPreference(PREF_ALTERNATE_LAYOUT);
        mUseAlternateWidgetLayoutPreference.setChecked(Settings.System.getInt(getContentResolver(),
                "tweaks_use_alternative_toggle_layout", 0) == 1);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSwipeToClearPreference) {
            boolean enable = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(), "tweaks_swipe_to_clear_notifications",
                    enable ? 1 : 0);
            return true;
        } else if (preference == mLongPressToClearPreference) {
            boolean enable = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_long_press_to_clear_notifications", enable ? 1 : 0);
            return true;
        } else if (preference == mUseAlternateWidgetLayoutPreference) {
            boolean enable = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(), "tweaks_use_alternative_toggle_layout",
                    enable ? 1 : 0);
            return true;
        } else
            return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
