
package com.roman.tweaks;

import com.roman.tweaks.activities.BatteryActivity;
import com.roman.tweaks.activities.ClockActivity;
import com.roman.tweaks.activities.SignalActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class Main extends PreferenceActivity {
    String pref;

    Context context;

    Dialog d;

    AlertDialog.Builder builder;

    private static final String PREF_ENABLE_GPS = "vibrant_enable_gps_pref";

    private static final String GPS_ENABLED_NAME = "gpsd";

    private static final String GPS_DISBLED_NAME = "disabled_gps";

    private static final String PREF_BLN = "bln_pref";

    private static final String PREF_BATTERY = "battery_options_pref";

    private static final String PREF_CLOCK = "clock_options_pref";

    private static final String PREF_SIGNAL = "signal_options_pref";

    private static final String PREF_RECENT_APPS = "show_recent_apps";

    CheckBoxPreference mEnableGPS;

    CheckBoxPreference mShowRecentApps;

    Preference mBattery;

    Preference mClock;

    Preference mSignal;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        addPreferencesFromResource(R.xml.main_prefs);
        d = new Dialog(this);
        builder = new AlertDialog.Builder(this);
        PreferenceScreen prefs = getPreferenceScreen();

        mShowRecentApps = (CheckBoxPreference) prefs.findPreference(PREF_RECENT_APPS);
        boolean checked = (Settings.System
                .getInt(getContentResolver(), "tweaks_show_recent_apps", 0) == 1) ? true : false;
        mShowRecentApps.setChecked(checked);
        
        mClock = prefs.findPreference(PREF_CLOCK);
        mBattery = prefs.findPreference(PREF_BATTERY);
        mSignal = prefs.findPreference(PREF_SIGNAL);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference == mClock) {
            startActivity(new Intent(context, ClockActivity.class));
            return true;
        } else if (preference == mBattery) {
            startActivity(new Intent(context, BatteryActivity.class));
            return true;
        } else if (preference == mSignal) {
            startActivity(new Intent(context, SignalActivity.class));
            return true;
        } else if (preference == mShowRecentApps) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System
                    .putInt(getContentResolver(), "tweaks_show_recent_apps", checked ? 1 : 0);
            return true;
        }

        return false;
    }

}
