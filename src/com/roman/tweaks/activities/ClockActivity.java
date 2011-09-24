
package com.roman.tweaks.activities;

import com.roman.tweaks.R;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.util.Log;

public class ClockActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    String pref;

    Context mContext;

    Dialog d;

    AlertDialog.Builder builder;

    private static final String PREF_CLOCK_DISPLAY_STYLE = "clock_am_pm";

    private static final String PREF_CLOCK_STYLE = "clock_style";

    private static final String PREF_CLOCK_COLOR = "clock_color_pref";

    private static final String RPEF_HIDE_ALARM = "alarm";

    ListPreference mAmPmStyle;
    ListPreference mClockStyle;
    CheckBoxPreference mHideAlarm;
    Preference mColorPref;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        addPreferencesFromResource(R.xml.clock_prefs);
        PreferenceScreen prefs = getPreferenceScreen();

        mClockStyle = (ListPreference) prefs.findPreference(PREF_CLOCK_STYLE);
        mAmPmStyle = (ListPreference) prefs.findPreference(PREF_CLOCK_DISPLAY_STYLE);
        mHideAlarm = (CheckBoxPreference) prefs.findPreference(RPEF_HIDE_ALARM);
        mColorPref = prefs.findPreference(PREF_CLOCK_COLOR);

        int styleValue = Settings.System.getInt(getContentResolver(),
                "tweaks_clock_ampm_style", 2);
        mAmPmStyle.setValueIndex(styleValue);
        mAmPmStyle.setOnPreferenceChangeListener(this);

        int clockVal = Settings.System.getInt(getContentResolver(),
                "tweaks_clock_style", 1);
        mClockStyle.setValueIndex(clockVal);
        mClockStyle.setOnPreferenceChangeListener(this);

        int alarm = Settings.System.getInt(getContentResolver(),
                "tweaks_show_alarm_icon", 1);
        mHideAlarm.setChecked(alarm == 0);

    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference == mColorPref) {
            ColorPickerDialog cp = new ColorPickerDialog(this, mColorChangeListener,
                    Settings.System.getInt(getContentResolver(),
                            "tweaks_clock_color", Color.WHITE));
            cp.show();
            return true;
        } else if (preference == mHideAlarm) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(), "tweaks_show_alarm_icon",
                    checked ? 0 : 1);

            return true;
        }

        return false;
    }

    ColorPickerDialog.OnColorChangedListener mColorChangeListener = new ColorPickerDialog.OnColorChangedListener() {

        public void colorUpdate(int color) {
            Settings.System.putInt(getContentResolver(), "tweaks_clock_color",
                    color);
        }

        public void colorChanged(int color) {
            Settings.System.putInt(getContentResolver(), "tweaks_clock_color",
                    color);

        }
    };

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAmPmStyle) {
            int statusBarAmPm = Integer.valueOf((String) newValue);

            Settings.System.putInt(getContentResolver(), "tweaks_clock_ampm_style",
                    statusBarAmPm);
            return true;

        } else if (preference == mClockStyle) {
            int val = Integer.valueOf((String) newValue);

            Settings.System.putInt(getContentResolver(), "tweaks_clock_style", val);

            return true;
        }

        return false;
    }
}
