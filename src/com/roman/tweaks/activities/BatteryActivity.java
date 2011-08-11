
package com.roman.tweaks.activities;

import com.roman.tweaks.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class BatteryActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    Context context;

    private static final String PREF_BATTERY_TEXT_STYLE = "battery_text_style_pref";

    private static final String PREF_SHOW_BATTERY_ICON = "show_battery_icon";

    private static final String PREF_COLOR_AUTOMATICALLY = "battery_automatically_color_pref";

    private static final String PREF_COLOR_STATIC = "battery_color_pref";

    private static final String PREF_COLOR_CHARGING = "battery_color_auto_charging";

    private static final String PREF_COLOR_REGULAR = "battery_color_auto_regular";

    private static final String PREF_COLOR_MEDIUM = "battery_color_auto_medium";

    private static final String PREF_COLOR_LOW = "battery_color_auto_low";

    private static final String CONST_BATTERY_TEXT = "tweaks_battery_text_style";

    ListPreference mBatteryTextStyle;

    CheckBoxPreference mShowBatteryIcon;

    CheckBoxPreference mColorAutomatically;

    Preference mColorStatic;

    Preference mColorCharging;

    Preference mColorRegular;

    Preference mColorMedium;

    Preference mColorLow;

    private static String sCurrentPrefColorFlag;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        addPreferencesFromResource(R.xml.battery_prefs);

        PreferenceScreen prefs = getPreferenceScreen();

        mBatteryTextStyle = (ListPreference) prefs.findPreference(PREF_BATTERY_TEXT_STYLE);
        mShowBatteryIcon = (CheckBoxPreference) prefs.findPreference(PREF_SHOW_BATTERY_ICON);
        mColorAutomatically = (CheckBoxPreference) prefs.findPreference(PREF_COLOR_AUTOMATICALLY);
        mColorStatic = prefs.findPreference(PREF_COLOR_STATIC);
        mColorCharging = prefs.findPreference(PREF_COLOR_CHARGING);
        mColorRegular = prefs.findPreference(PREF_COLOR_REGULAR);
        mColorMedium = prefs.findPreference(PREF_COLOR_MEDIUM);
        mColorLow = prefs.findPreference(PREF_COLOR_LOW);

        int batteryStyleIndex = Settings.System.getInt(getContentResolver(),
                CONST_BATTERY_TEXT, 1);
        mBatteryTextStyle.setValueIndex(batteryStyleIndex);
        mBatteryTextStyle.setOnPreferenceChangeListener(this);

        mShowBatteryIcon.setChecked(Settings.System.getInt(getContentResolver(),
                "tweaks_show_battery", 1) == 1);

        refreshOptions();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryTextStyle) {
            // preference = (ListPreference) preference;

            int val = Integer.valueOf((String) newValue);

            Settings.System
                    .putInt(getContentResolver(), CONST_BATTERY_TEXT, val);
            return true;
        }
        return false;
    }

    public ColorPickerDialog generateDialog(String preference) {
        ColorPickerDialog cp = new ColorPickerDialog(this, mColorChangeListener,
                Settings.System.getInt(getContentResolver(), preference,
                        Settings.System.getInt(getContentResolver(), preference, Color.WHITE)));
        return cp;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        // String key = preference.getKey();

        if (preference == mColorCharging) {
            sCurrentPrefColorFlag = "tweaks_batt_color_auto_charging";

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorRegular) {
            sCurrentPrefColorFlag = "tweaks_batt_color_auto_regular";

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorMedium) {
            sCurrentPrefColorFlag = "tweaks_batt_color_auto_medium";

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorLow) {
            sCurrentPrefColorFlag = "tweaks_batt_color_auto_low";

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorStatic) {
            sCurrentPrefColorFlag = "tweaks_batt_color_static";

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorAutomatically) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            Settings.System.putInt(getContentResolver(),
                    "tweaks_batt_color_auto_enabled", value);

            preference.setSummary((checked ? R.string.automatic_battery_enabled
                    : R.string.automatic_battery_disabled));

            refreshOptions();
            return true;

        } else if (preference == mShowBatteryIcon) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            Settings.System.putInt(getContentResolver(), "tweaks_show_battery", value);

            return true;

        }

        return false;
    }

    ColorPickerDialog.OnColorChangedListener mColorChangeListener = new ColorPickerDialog.OnColorChangedListener() {

        @Override
        public void colorUpdate(int color) {
            Settings.System.putInt(getContentResolver(), sCurrentPrefColorFlag, color);
        }

        @Override
        public void colorChanged(int color) {
            Settings.System.putInt(getContentResolver(), sCurrentPrefColorFlag, color);

        }
    };

    public void refreshOptions() {
        if (((CheckBoxPreference) mColorAutomatically).isChecked()) {
            findPreference("battery_color_pref").setEnabled(false);
            findPreference("battery_color_auto_charging").setEnabled(true);
            findPreference("battery_color_auto_regular").setEnabled(true);
            findPreference("battery_color_auto_medium").setEnabled(true);
            findPreference("battery_color_auto_low").setEnabled(true);
        } else {
            findPreference("battery_color_pref").setEnabled(true);
            findPreference("battery_color_auto_charging").setEnabled(false);
            findPreference("battery_color_auto_regular").setEnabled(false);
            findPreference("battery_color_auto_medium").setEnabled(false);
            findPreference("battery_color_auto_low").setEnabled(false);
        }
    }

}
