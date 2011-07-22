
package com.roman.tweaks.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.tweaks.R;

public class ClockActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    String pref;

    Context mContext;

    Dialog d;

    AlertDialog.Builder builder;

    private static final String PREF_CLOCK_DISPLAY_STYLE = "clock_am_pm";

    private static final String PREF_CLOCK_COLOR = "clock_color_pref";

    ListPreference mClockStyle;

    Preference mColorPref;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        addPreferencesFromResource(R.xml.clock_prefs);
        PreferenceScreen prefs = getPreferenceScreen();

        mClockStyle = (ListPreference) prefs.findPreference(PREF_CLOCK_DISPLAY_STYLE);
        mColorPref = prefs.findPreference(PREF_CLOCK_COLOR);

        /**
         * 3 should hide, others should follow pattern in Clock in SystemUI
         */
        int styleValue = (Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK, 1) == 0) ? 3 : Settings.System.getInt(
                getContentResolver(), Settings.System.STATUS_BAR_AM_PM, 2);
        mClockStyle.setValueIndex(styleValue);
        mClockStyle.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference == mColorPref) {
            ColorPickerDialog cp = new ColorPickerDialog(this, mColorChangeListener,
                    Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_CLOCK_COLOR, Color.WHITE));
            cp.show();
        }

        return false;
    }

    ColorPickerDialog.OnColorChangedListener mColorChangeListener = new ColorPickerDialog.OnColorChangedListener() {

        @Override
        public void colorUpdate(int color) {
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CLOCK_COLOR,
                    color);
        }

        @Override
        public void colorChanged(int color) {
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CLOCK_COLOR,
                    color);

        }
    };

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mClockStyle) {
            int statusBarAmPm = Integer.valueOf((String) newValue);

            if (statusBarAmPm == 3)
                Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CLOCK, 0);
            else {
                Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CLOCK, 1);
                Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_AM_PM,
                        statusBarAmPm);
            }
            return true;
        }
        return false;
    }
}
