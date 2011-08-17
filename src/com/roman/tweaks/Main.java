
package com.roman.tweaks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class Main extends PreferenceActivity implements OnPreferenceChangeListener {
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

    private static final String PREF_SCREEN_OFF = "pref_animate_off";

    private static final String PREF_SCREEN_ON = "pref_animate_on";

    private static final String OVERSCROLL_PREF = "pref_overscroll_effect";

    CheckBoxPreference mEnableGPS;

    CheckBoxPreference mShowRecentApps;

    CheckBoxPreference mAnimateScreenOff;

    CheckBoxPreference mAnimateScreenOn;

    ListPreference mOverscrollPref;

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

        checked = (Settings.System
                .getInt(getContentResolver(), "tweaks_crt_off", 0) == 1) ? true : false;
        mAnimateScreenOff = (CheckBoxPreference) prefs.findPreference(PREF_SCREEN_OFF);
        mAnimateScreenOff.setChecked(checked);

        checked = (Settings.System
                .getInt(getContentResolver(), "tweaks_crt_on", 0) == 1) ? true : false;
        mAnimateScreenOn = (CheckBoxPreference) prefs.findPreference(PREF_SCREEN_ON);
        mAnimateScreenOn.setChecked(checked);

        mOverscrollPref = (ListPreference) prefs.findPreference(OVERSCROLL_PREF);
        int overscrollEffect = Settings.System.getInt(getContentResolver(),
                "overscroll_effect", 1);
        mOverscrollPref.setValue(String.valueOf(overscrollEffect));
        mOverscrollPref.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mOverscrollPref) {
            int overscrollEffect = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), "overscroll_effect",
                    overscrollEffect);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference == mShowRecentApps) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System
                    .putInt(getContentResolver(), "tweaks_show_recent_apps", checked ? 1 : 0);
            return true;
        } else if (preference == mAnimateScreenOff) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System
                    .putInt(getContentResolver(), "tweaks_crt_off", checked ? 1 : 0);
            return true;
        } else if (preference == mAnimateScreenOn) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System
                    .putInt(getContentResolver(), "tweaks_crt_on", checked ? 1 : 0);
            return true;
        }

        return false;
    }

}
