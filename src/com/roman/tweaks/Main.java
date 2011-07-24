
package com.roman.tweaks;

import com.roman.tweaks.activities.BatteryActivity;
import com.roman.tweaks.activities.ClockActivity;
import com.roman.tweaks.activities.SignalActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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

    CheckBoxPreference mEnableGPS;

    CheckBoxPreference mBLN;

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

        // vibrant specific settings
        if (Build.DEVICE.contains("vibrantmtd")) {
            this.addPreferencesFromResource(R.xml.vibrant_prefs);

            mEnableGPS = (CheckBoxPreference) prefs.findPreference(PREF_ENABLE_GPS);

            String output = "";
            if (ShellInterface.isSuAvailable())
                output = ShellInterface.getProcessOutput("ls /system/vendor/bin | grep gpsd");
            if (output == null)
                output = "was null";

            boolean checked = output.equals("gpsd");
            mEnableGPS.setChecked(checked);
        }

        // bln
        mBLN = (CheckBoxPreference) prefs.findPreference(PREF_BLN);
        mBattery = prefs.findPreference(PREF_BATTERY);
        mClock = prefs.findPreference(PREF_CLOCK);
        mSignal = prefs.findPreference(PREF_SIGNAL);

        boolean checkBLN = Settings.System.getInt(getContentResolver(),
                Settings.System.TRACKBALL_NOTIFICATION_ON, 1) == 1;
        mBLN.setChecked(checkBLN);

        /*
         * read ahead setter
         */
        ListPreference read_ahead_pref = (ListPreference) findPreference("read_ahead_pref");

        String readAhead = "";
        if (ShellInterface.isSuAvailable()) {
            readAhead = ShellInterface
                    .getProcessOutput("more /sys/devices/virtual/bdi/179:0/read_ahead_kb");
        }
        read_ahead_pref.setSummary(readAhead);

        if (read_ahead_pref.getEntry() == null) {
            read_ahead_pref.setValueIndex(4);
        }
        // read_ahead_pref.setValueIndex()

        read_ahead_pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ShellInterface.isSuAvailable()) {
                    ShellInterface.runCommand("/system/xbin/echo " + newValue
                            + " > /sys/devices/virtual/bdi/179:0/read_ahead_kb");
                }

                preference.setSummary(newValue.toString());
                return true;
            }
        });
    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference == mEnableGPS) {

            boolean enabled = !((CheckBoxPreference) preference).isChecked();

            String dir = "/system/vendor/bin/";
            String enableCommand = "mv \"" + dir + GPS_DISBLED_NAME + "\" \"" + dir
                    + GPS_ENABLED_NAME + "\"";
            String disableCommand = "mv \"" + dir + GPS_ENABLED_NAME + "\" \"" + dir
                    + GPS_DISBLED_NAME + "\"";

            if (ShellInterface.isSuAvailable()) {
                ShellInterface.getProcessOutput("mount -o rw,remount /system");
                // ShellInterface.getProcessOutput("cd /system/vendor/bin");
                if (enabled) {
                    ShellInterface.getProcessOutput(disableCommand);
                } else {
                    ShellInterface.getProcessOutput(enableCommand);
                }

                preference.setSummary("Reboot to apply the change!");
                ShellInterface.getProcessOutput("mount -o ro,remount /system");
                return true;
            }
        } else if (preference == mBLN) {
            boolean checked = mBLN.isChecked();
            int value = (checked ? 1 : 0);
            Settings.System.putInt(getContentResolver(), Settings.System.TRACKBALL_NOTIFICATION_ON,
                    value);
            return true;
        } else if (preference == mClock) {
            startActivity(new Intent(context, ClockActivity.class));
            return true;
        } else if (preference == mBattery) {
            startActivity(new Intent(context, BatteryActivity.class));
            return true;
        } else if (preference == mSignal) {
            startActivity(new Intent(context, SignalActivity.class));
            return true;
        }

        return false;
    }

}
