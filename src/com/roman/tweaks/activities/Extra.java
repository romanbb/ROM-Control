
package com.roman.tweaks.activities;

import com.roman.tweaks.ShellInterface;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class Extra extends PreferenceActivity {

    protected void onCreate(Bundle savedInstanceState) {
//
//        /*
//         * read ahead setter
//         */
//        ListPreference read_ahead_pref = (ListPreference) findPreference("read_ahead_pref");
//
//        String readAhead = "";
//        if (ShellInterface.isSuAvailable()) {
//            readAhead = ShellInterface
//                    .getProcessOutput("more /sys/devices/virtual/bdi/179:0/read_ahead_kb");
//        }
//        read_ahead_pref.setSummary(readAhead);
//
//        if (read_ahead_pref.getEntry() == null) {
//            read_ahead_pref.setValueIndex(4);
//        }
//        // read_ahead_pref.setValueIndex()
//
//        read_ahead_pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if (ShellInterface.isSuAvailable()) {
//                    ShellInterface.runCommand("/system/xbin/echo " + newValue
//                            + " > /sys/devices/virtual/bdi/179:0/read_ahead_kb");
//                }
//
//                preference.setSummary(newValue.toString());
//                return true;
//            }
//        });
    }

}
