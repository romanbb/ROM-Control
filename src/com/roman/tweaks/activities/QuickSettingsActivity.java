
package com.roman.tweaks.activities;

import com.roman.tweaks.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class QuickSettingsActivity extends PreferenceActivity {

    private static final String SETTINGS_PREF = "tweaks_widgets_hidden";

    static final String WHICH_PREF = "which_prefs";

    Preference hiddenSelectionPref;
    CheckBoxPreference test;

    Handler handler = new Handler();
    Camera mCamera;

    public void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);

        addPreferencesFromResource(R.xml.quick_setting_pref);
        hiddenSelectionPref = findPreference(WHICH_PREF);
        test = (CheckBoxPreference) findPreference("test");
    }

    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference == hiddenSelectionPref) {
            final boolean[] items = new boolean[getResources().getStringArray(
                    R.array.quick_settings_entries).length];
            ArrayList<Integer> whichToHide = new ArrayList<Integer>();

            // now build it
            String vals = android.provider.Settings.System.getString(getContentResolver(),
                    SETTINGS_PREF);
            if (vals == null)
                vals = "";

            // Log.e("Roman", vals);
            StringTokenizer st = new StringTokenizer(vals);

            while (st.hasMoreTokens())
                whichToHide.add(Integer.parseInt(st.nextToken()));

            for (int i = 0; i < items.length; i++)
                items[i] = whichToHide.contains(i);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Which settings to hide?");
            builder.setMultiChoiceItems(R.array.quick_settings_entries, items,
                    new DialogInterface.OnMultiChoiceClickListener() {

                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            items[which] = isChecked;
                            publishVals(items);
                        }
                    })

            .setNegativeButton("For Science!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        } else if (preference == test) {
            if (((CheckBoxPreference) preference).isChecked()) {
                mCameraOpenTask.run();
            } else {
                mCameraCloseTask.run();
            }
        }
        return false;
    }

    private Runnable mCameraOpenTask = new Runnable() {
        public void run() {
            mCamera = Camera.open();
            if (mCamera != null) {
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(params);
                mCamera.release();
            }
        }
    };

    private Runnable mCameraCloseTask = new Runnable() {

        public void run() {
            mCamera = Camera.open();
            if (mCamera != null) {
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                mCamera.release();
            }

        }
    };

    public void publishVals(boolean[] items) {
        String s = "";

        for (int i = 0; i < items.length; i++)
            if (items[i])
                s += "" + i + " ";

        // Log.e("Roman", "Publishing: " + s);
        Settings.System.putString(getContentResolver(), SETTINGS_PREF, s.trim());
    }
}
