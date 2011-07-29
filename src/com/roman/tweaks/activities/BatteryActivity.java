
package com.roman.tweaks.activities;

import com.roman.tweaks.AmbilWarnaDialog;
import com.roman.tweaks.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.roman.tweaks.R;

import android.content.Context;
import android.content.CustomIntents;
import android.content.Intent;
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

    private static final int COLOR_BATTERY = 1;

    private static final int COLOR_AUTO_CHARGING = 2;

    private static final int COLOR_AUTO_MEDIUM = 3;

    private static final int COLOR_AUTO_REGULAR = 4;

    private static final int COLOR_AUTO_LOW = 5;

    private int batteryColorPickerFlag;

    AmbilWarnaDialog batteryColorPickerDialog = null;

    private static final String PREF_BATTERY_TEXT_STYLE = "battery_text_style_pref";

    private static final String PREF_SHOW_BATTERY_ICON = "show_battery_icon";

    private static final String PREF_SHOW_CM_BATTERY_BAR = "show_cm_battery_icon";

    private static final String PREF_SHOW_MIUI_BATTERY = "show_miui_battery";

    private static final String PREF_COLOR_AUTOMATICALLY = "battery_automatically_color_pref";

    private static final String PREF_COLOR_STATIC = "battery_color_pref";

    private static final String PREF_COLOR_CHARGING = "battery_color_auto_charging";

    private static final String PREF_COLOR_REGULAR = "battery_color_auto_regular";

    private static final String PREF_COLOR_MEDIUM = "battery_color_auto_medium";

    private static final String PREF_COLOR_LOW = "battery_color_auto_low";

    ListPreference mBatteryTextStyle;

    CheckBoxPreference mShowBatteryIcon;

    CheckBoxPreference mShowCMBatteryIcon;

    CheckBoxPreference mColorAutomatically;

    CheckBoxPreference mShowMiuiBattery;

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
        mShowCMBatteryIcon = (CheckBoxPreference) prefs.findPreference(PREF_SHOW_CM_BATTERY_BAR);
        mShowMiuiBattery = (CheckBoxPreference) prefs.findPreference(PREF_SHOW_MIUI_BATTERY);
        mColorAutomatically = (CheckBoxPreference) prefs.findPreference(PREF_COLOR_AUTOMATICALLY);
        mColorStatic = prefs.findPreference(PREF_COLOR_STATIC);
        mColorCharging = prefs.findPreference(PREF_COLOR_CHARGING);
        mColorRegular = prefs.findPreference(PREF_COLOR_REGULAR);
        mColorMedium = prefs.findPreference(PREF_COLOR_MEDIUM);
        mColorLow = prefs.findPreference(PREF_COLOR_LOW);

        int batteryStyleIndex = Settings.System.getInt(getContentResolver(),
                PREF_BATTERY_TEXT_STYLE, 1);
        mBatteryTextStyle.setValueIndex(batteryStyleIndex);
        mBatteryTextStyle.setOnPreferenceChangeListener(this);

        mShowBatteryIcon.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY, 0) == 1);

        mShowCMBatteryIcon.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CM_BATTERY_ICON, 0) == 1);

        mShowMiuiBattery.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_TWEAKS_MIUI_BATTERY, 0) == 1);

        refreshOptions();
    }

    public void sendTimeIntent() {
        Intent timeIntent = new Intent();
        timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
        sendBroadcast(timeIntent);
        refreshOptions();
    }

    public void sendBatteryUpdateIntent() {
        Intent i = new Intent();
        i.setAction(CustomIntents.ACTION_BATTERY_UPDATE);
        sendBroadcast(i);
        sendBroadcast(i);
        refreshOptions();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryTextStyle) {
            // preference = (ListPreference) preference;

            int val = Integer.valueOf((String) newValue);

            Settings.System
                    .putInt(getContentResolver(), Settings.System.STATUS_BAR_CM_BATTERY, val);
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
            sCurrentPrefColorFlag = Settings.System.STATUS_BAR_CM_BATTERY_TEXT_COLOR_AUTO_CHARGING;

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorRegular) {
            sCurrentPrefColorFlag = Settings.System.STATUS_BAR_CM_BATTERY_TEXT_COLOR_AUTO_REG;

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorMedium) {
            sCurrentPrefColorFlag = Settings.System.STATUS_BAR_CM_BATTERY_TEXT_COLOR_AUTO_MEDIUM;

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorLow) {
            sCurrentPrefColorFlag = Settings.System.STATUS_BAR_CM_BATTERY_TEXT_COLOR_AUTO_LOW;

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorStatic) {
            sCurrentPrefColorFlag = Settings.System.STATUS_BAR_CM_BATTERY_TEXT_COLOR_STATIC;

            generateDialog(sCurrentPrefColorFlag).show();
            return true;

        } else if (preference == mColorAutomatically) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_CM_BATTERY_TEXT_DO_AUTO_COLOR, value);

            preference.setSummary((checked ? R.string.automatic_battery_enabled
                    : R.string.automatic_battery_disabled));

            refreshOptions();
            return true;

        } else if (preference == mShowBatteryIcon) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_BATTERY, value);

            return true;

        } else if (preference == mShowCMBatteryIcon) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            if (checked) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.STATUS_BAR_CM_BATTERY_ICON, 1);
            } else {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.STATUS_BAR_CM_BATTERY_ICON, 0);
            }
            return true;
        } else if (preference == mShowMiuiBattery) {
            int val = ((CheckBoxPreference) preference).isChecked() ? 1 : 0;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_TWEAKS_MIUI_BATTERY, val);

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
