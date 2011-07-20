
package com.roman.tweaks;

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
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.roman.tweaks.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.roman.tweaks.ColorPickerDialog.OnColorChangedListener;

public class Main extends PreferenceActivity {
    String pref;

    Context context;

    Dialog d;

    AlertDialog.Builder builder;

    private static final String PREF_ENABLE_GPS = "vibrant_enable_gps_pref";

    private static final String GPS_ENABLED_NAME = "gpsd";

    private static final String GPS_DISBLED_NAME = "disabled_gps";

    private static final String PREF_BLN = "bln_pref";

    CheckBoxPreference mEnableGPS;

    CheckBoxPreference mBLN;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        addPreferencesFromResource(R.xml.main_prefs);
        d = new Dialog(this);
        builder = new AlertDialog.Builder(this);
        PreferenceScreen prefs = getPreferenceScreen();

        //vibrant specific settings
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
        
        //bln
        mBLN = (CheckBoxPreference) prefs.findPreference(PREF_BLN);
        
        boolean checkBLN = Settings.System.getInt(getContentResolver(), Settings.System.TRACKBALL_NOTIFICATION_ON, 1) == 1;
        mBLN.setChecked(checkBLN);
        

        /*
         * ampm options
         */
        Preference clock_am_pm = (Preference) findPreference("clock_am_pm");

        try {
            Settings.System.getInt(getContentResolver(), "clock_am_pm_style");

        } catch (SettingNotFoundException e) {
            Settings.System.putInt(getContentResolver(), "clock_am_pm_style", 0);
        }

        clock_am_pm.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Integer selection = Integer.parseInt(newValue.toString());

                Settings.System.putInt(getContentResolver(), "clock_am_pm_style", selection);

                Intent timeIntent = new Intent();
                timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
                sendBroadcast(timeIntent);
                return true;
            }
        });

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

        findPreference("battery_options_pref").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {

                    public boolean onPreferenceClick(Preference preference) {
                        Intent i = new Intent(context, BatteryActivity.class);
                        startActivity(i);
                        return true;
                    }
                });

        /*
         * about preference
         */
        findPreference("about_pref").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(context, AboutActivity.class);
                startActivity(i);
                return true;
            }
        });

        /*
         * about preference
         */
        findPreference("signal_pref").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Main.this, SignalActivity.class);
                Main.this.startActivity(i);
                return true;
            }
        });

        /*
         * color picker
         */
        // initialColor is the initially-selected color to be shown in the
        // rectangle on the left of the arrow.
        // for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware
        // of the initial 0xff which is the alpha.

        final AmbilWarnaDialog colorPickerDialog = new AmbilWarnaDialog(this, 0xffffffff,
                new OnAmbilWarnaListener() {
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        Settings.System.putInt(context.getContentResolver(), "clock_color", color);

                        Intent timeIntent = new Intent();
                        timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
                        sendBroadcast(timeIntent);
                        sendTimeIntent();

                        String stringColor = color + "";
                        findPreference("clock_color_pref").setSummary(stringColor);
                    }

                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });

        ((Preference) findPreference("clock_color_pref"))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    public boolean onPreferenceClick(Preference preference) {
                        // Intent i = new Intent(context,
                        // ColorPickerDialog.class);
                        colorPickerDialog.show();
                        return true;
                    }
                });

        // run at the end
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
            Settings.System.putInt(getContentResolver(), Settings.System.TRACKBALL_NOTIFICATION_ON, value);            
        }

        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.pref_menu, menu);
        return true;
    }

    public void onResume() {
        super.onResume();
        // Preference custom_app_preference = (Preference)
        // findPreference("custom_app_pref");
        // custom_app_preference.setSummary(Settings.System.getString(
        // getContentResolver(), "custom_edt_app_name"));
        //
        // Preference lockscreen_timeout_pref = (Preference)
        // findPreference("lockscreen_timeout_pref");
        // int i = (Settings.System.getInt(getContentResolver(),
        // "custom_lockscreen_timeout", 5000) / 1000);
        //
        // lockscreen_timeout_pref.setSummary(i + " seconds");
        // refreshLockscreenPreferences();
    }

    public class ClockChangeListener implements OnColorChangedListener {

        private Context mContext;

        private int color;

        public ClockChangeListener(Context c) {
            mContext = c;
        }

        public void colorChanged(int color) {

        }

        public void setColor(int c) {
            color = c;
        }

        public int getColor() {
            return color;
        }

    }

    public void sendTimeIntent() {
        Intent timeIntent = new Intent();
        timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
        sendBroadcast(timeIntent);
    }

}
