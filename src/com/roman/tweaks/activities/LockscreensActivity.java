
package com.roman.tweaks.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.tweaks.R;
import com.roman.tweaks.utils.ShortcutPickHelper;

public class LockscreensActivity extends PreferenceActivity implements OnPreferenceChangeListener,
        ShortcutPickHelper.OnPickListener {

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";
    private static final String LOCKSCREEN_QUADRANT_1_PREF = "pref_quadrant_1";
    private static final String LOCKSCREEN_QUADRANT_2_PREF = "pref_quadrant_2";
    private static final String LOCKSCREEN_QUADRANT_3_PREF = "pref_quadrant_3";
    private static final String LOCKSCREEN_QUADRANT_4_PREF = "pref_quadrant_4";
    private static final String LOCKSCREEN_CLOCK_PREF = "pref_clock";
    private static final String PREF_WAKE = "volume_wake";
    private static final String PREF_SGS2_MUSIC_LOC = "sgs_music_loc";

    private ListPreference mLockscreenStylePref;
    private ListPreference mVolumeWake;
    private ListPreference mSgs2MusicLoc;
    private Preference mHoneyQuadrant1Pref;
    private Preference mHoneyQuadrant2Pref;
    private Preference mHoneyQuadrant3Pref;
    private Preference mHoneyQuadrant4Pref;
    private Preference mCurrentCustomActivityPreference;
    private String mCurrentCustomActivityString;

    private ShortcutPickHelper mPicker;

    public void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);
        addPreferencesFromResource(R.xml.lockscreen_prefs);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Lockscreen Style and related related settings */
        mLockscreenStylePref = (ListPreference) prefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        int lockScreenStyle = Settings.System.getInt(getContentResolver(),
                "tweaks_lockscreen_style", 0);
        // mLockscreenStylePref.setValueIndex(lockScreenStyle);
        mLockscreenStylePref.setOnPreferenceChangeListener(this);

        // mHoneyQuadrant1Pref =
        // prefSet.findPreference(LOCKSCREEN_QUADRANT_1_PREF);
        // mHoneyQuadrant2Pref =
        // prefSet.findPreference(LOCKSCREEN_QUADRANT_2_PREF);
        // mHoneyQuadrant3Pref =
        // prefSet.findPreference(LOCKSCREEN_QUADRANT_3_PREF);
        // mHoneyQuadrant4Pref =
        // prefSet.findPreference(LOCKSCREEN_QUADRANT_4_PREF);
        //
        // mShowHoneyClock = (CheckBoxPreference)
        // prefSet.findPreference(LOCKSCREEN_CLOCK_PREF);

        mPicker = new ShortcutPickHelper(this, this);

        mVolumeWake = (ListPreference) prefSet.findPreference(PREF_WAKE);
        mVolumeWake.setValue(Settings.System.getInt(getContentResolver(), "tweaks_use_volume", 0)
                + "");
        mVolumeWake.setOnPreferenceChangeListener(this);

        mSgs2MusicLoc = (ListPreference) prefSet.findPreference(PREF_SGS2_MUSIC_LOC);
        mSgs2MusicLoc.setOnPreferenceChangeListener(this);
        mSgs2MusicLoc.setValue(Settings.System.getInt(getContentResolver(),
                "lockscreen_sgsmusic_controls", 1) + "");
        
       prefSet.removePreference(mSgs2MusicLoc);

    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mHoneyQuadrant1Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = "tweaks_lockscreen_hc_activity_1";
            mPicker.pickShortcut();
            return true;

        } else if (preference == mHoneyQuadrant2Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = "tweaks_lockscreen_hc_activity_2";
            mPicker.pickShortcut();
            return true;

        } else if (preference == mHoneyQuadrant3Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = "tweaks_lockscreen_hc_activity_3";
            mPicker.pickShortcut();
            return true;

        } else if (preference == mHoneyQuadrant4Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = "tweaks_lockscreen_hc_activity_4";
            mPicker.pickShortcut();
            return true;
        } else if (preference == mLockscreenStylePref) {
            mLockscreenStylePref.setValue(Settings.System.getInt(getContentResolver(),
                    "tweaks_lockscreen_style", 0) + "");
            return true;
        }

        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    public static String TAG = "Lockscreens";

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenStylePref) {

            int index = 0;
            CharSequence[] entries = ((ListPreference) preference).getEntryValues();

            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals((String) newValue)) {
                    index = i;
                    // Log.e("Roman", "break: " + i);
                    break;
                }
            }
            int newint = Integer.parseInt(entries[index] + "");

            boolean disable_lockscreens = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean("JUGGERNAUT_26_LOCKSCREEN_DISABLE", true);

            // Log.e(TAG, "Putting " + newint);
            if (disable_lockscreens && newint != 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setPositiveButton("Okay :(", new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setMessage("Since the lockscreens are causing reboots we're disabling them for now! We are working hard to resolve the issue and you'll know when it's fixed!");
                builder.setTitle("Sorry!");
                builder.create().show();

                ((ListPreference) preference).setValueIndex(0);

                return false;
            }

            Settings.System.putInt(getContentResolver(), "tweaks_lockscreen_style", newint);
            // Log.e("Roman", "new val: " + newint);
            // updateStylePrefs(mLockscreenStyle, mInCallStyle);
            return true;
        } else if (preference == mVolumeWake) {
            CharSequence[] entries = ((ListPreference) preference).getEntryValues();

            int newVal = 0;
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals((String) newValue)) {
                    newVal = Integer.parseInt(entries[i] + "");
                    break;
                }
            }

            Settings.System.putInt(getContentResolver(), "tweaks_use_volume", newVal);
            return true;
        } else if (preference == mSgs2MusicLoc) {
            CharSequence[] entries = ((ListPreference) preference).getEntryValues();

            int newVal = 0;
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals((String) newValue)) {
                    newVal = Integer.parseInt(entries[i] + "");
                    break;
                }
            }

            Settings.System.putInt(getContentResolver(), "lockscreen_sgsmusic_controls", newVal);
            return true;
        }

        return false;
    }

    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getContentResolver(), mCurrentCustomActivityString, uri)) {
            mCurrentCustomActivityPreference.setSummary(friendlyName);
        }
    }

}
