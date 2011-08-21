
package com.roman.tweaks.activities;

import com.roman.tweaks.R;
import com.roman.tweaks.utils.ShortcutPickHelper;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;

public class LockscreenActivity extends PreferenceActivity implements OnPreferenceChangeListener,
        ShortcutPickHelper.OnPickListener {

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";

    private static final String LOCKSCREEN_QUADRANT_1_PREF = "pref_quadrant_1";

    private static final String LOCKSCREEN_QUADRANT_2_PREF = "pref_quadrant_2";

    private static final String LOCKSCREEN_QUADRANT_3_PREF = "pref_quadrant_3";

    private static final String LOCKSCREEN_QUADRANT_4_PREF = "pref_quadrant_4";

    private static final String LOCKSCREEN_SILENCE_Q1_PREF = "pref_silent_quadrant_1";

    private static final String LOCKSCREEN_SILENCE_Q2_PREF = "pref_silent_quadrant_2";

    private static final String LOCKSCREEN_SILENCE_Q3_PREF = "pref_silent_quadrant_3";

    private static final String LOCKSCREEN_SILENCE_Q4_PREF = "pref_silent_quadrant_4";

    private static final String LOCKSCREEN_CLOCK_PREF = "pref_clock";

    private LockscreenStyle mLockscreenStyle;

    private ListPreference mLockscreenStylePref;

    private CheckBoxPreference mShowHoneyClock;

    private Preference mHoneyQuadrant1Pref;

    private Preference mHoneyQuadrant2Pref;

    private Preference mHoneyQuadrant3Pref;

    private Preference mHoneyQuadrant4Pref;

    private Preference mSilenceQuad1Pref;

    private Preference mSilenceQuad2Pref;

    private Preference mSilenceQuad3Pref;

    private Preference mSilenceQuad4Pref;

    private Preference mCurrentCustomActivityPreference;

    private String mCurrentCustomActivityString;

    private ShortcutPickHelper mPicker;

    private String mQ1Setting;

    private String mQ2Setting;

    private String mQ3Setting;

    private String mQ4Setting;

    private static final String TOGGLE_SILENT = "silent_mode";

    enum LockscreenStyle {
        Slider, Rotary, RotaryRevamped, Lense, Honeycomb;

        static public LockscreenStyle getStyleById(int id) {
            switch (id) {
                case 1:
                    return Slider;
                case 2:
                    return Rotary;
                case 3:
                    return RotaryRevamped;
                case 4:
                    return Lense;
                case 5:
                    return Honeycomb;
                default:
                    return RotaryRevamped;
            }
        }

        static public LockscreenStyle getStyleById(String id) {
            return getStyleById(Integer.valueOf(id));
        }

        static public int getIdByStyle(LockscreenStyle lockscreenstyle) {
            switch (lockscreenstyle) {
                case Slider:
                    return 1;
                case Rotary:
                    return 2;
                case RotaryRevamped:
                    return 3;
                case Lense:
                    return 4;
                case Honeycomb:
                    return 5;
                default:
                    return 3;
            }
        }
    }

    public void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);
        addPreferencesFromResource(R.xml.lockscreen_prefs);

        PreferenceScreen prefSet = getPreferenceScreen();

        mQ1Setting = Settings.System.getString(getContentResolver(), LOCKSCREEN_QUADRANT_1_PREF);
        mQ2Setting = Settings.System.getString(getContentResolver(), LOCKSCREEN_QUADRANT_2_PREF);
        mQ3Setting = Settings.System.getString(getContentResolver(), LOCKSCREEN_QUADRANT_3_PREF);
        mQ4Setting = Settings.System.getString(getContentResolver(), LOCKSCREEN_QUADRANT_4_PREF);

        /* Lockscreen Style and related related settings */
        mLockscreenStylePref = (ListPreference) prefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        mLockscreenStyle = LockscreenStyle.getStyleById(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCKSCREEN_STYLE_PREF, 3));
        mLockscreenStylePref
                .setValue(String.valueOf(LockscreenStyle.getIdByStyle(mLockscreenStyle)));
        mLockscreenStylePref.setOnPreferenceChangeListener(this);

        mHoneyQuadrant1Pref = prefSet.findPreference(LOCKSCREEN_QUADRANT_1_PREF);
        mHoneyQuadrant2Pref = prefSet.findPreference(LOCKSCREEN_QUADRANT_2_PREF);
        mHoneyQuadrant3Pref = prefSet.findPreference(LOCKSCREEN_QUADRANT_3_PREF);
        mHoneyQuadrant4Pref = prefSet.findPreference(LOCKSCREEN_QUADRANT_4_PREF);

        mSilenceQuad1Pref = prefSet.findPreference(LOCKSCREEN_SILENCE_Q1_PREF);
        mSilenceQuad2Pref = prefSet.findPreference(LOCKSCREEN_SILENCE_Q2_PREF);
        mSilenceQuad3Pref = prefSet.findPreference(LOCKSCREEN_SILENCE_Q3_PREF);
        mSilenceQuad4Pref = prefSet.findPreference(LOCKSCREEN_SILENCE_Q4_PREF);

        mShowHoneyClock = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_CLOCK_PREF);

        mPicker = new ShortcutPickHelper(this, this);
    }

    public void refreshSettings() {
        if (mQ1Setting.equals(TOGGLE_SILENT)) {
            mHoneyQuadrant1Pref.setSummary("Silent mode");
        } else {
            mHoneyQuadrant1Pref.setSummary(mPicker.getFriendlyNameForUri(mQ1Setting));
        }

        if (mQ2Setting.equals(TOGGLE_SILENT)) {
            mHoneyQuadrant2Pref.setSummary("Silent mode");
        } else {
            mHoneyQuadrant2Pref.setSummary(mPicker.getFriendlyNameForUri(mQ2Setting));
        }

        if (mQ3Setting.equals(TOGGLE_SILENT)) {
            mHoneyQuadrant3Pref.setSummary("Silent mode");
        } else {
            mHoneyQuadrant3Pref.setSummary(mPicker.getFriendlyNameForUri(mQ3Setting));
        }

        if (mQ4Setting.equals(TOGGLE_SILENT)) {
            mHoneyQuadrant4Pref.setSummary("Silent mode");
        } else {
            mHoneyQuadrant4Pref.setSummary(mPicker.getFriendlyNameForUri(mQ4Setting));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mHoneyQuadrant1Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_1;
            mPicker.pickShortcut();
            return true;

        } else if (preference == mHoneyQuadrant2Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_2;
            mPicker.pickShortcut();
            return true;

        } else if (preference == mHoneyQuadrant3Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_3;
            mPicker.pickShortcut();
            return true;

        } else if (preference == mHoneyQuadrant4Pref) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_4;
            mPicker.pickShortcut();
            return true;
        } else if (preference == mShowHoneyClock) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            int value = (checked ? 1 : 0);

            Settings.System.putInt(getContentResolver(), Settings.System.CLOCK_FONT, value);

            return true;
        } else if (preference == mSilenceQuad1Pref) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_1, TOGGLE_SILENT);
            return true;

        } else if (preference == mSilenceQuad2Pref) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_2, TOGGLE_SILENT);
            return true;

        } else if (preference == mSilenceQuad3Pref) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_3, TOGGLE_SILENT);
            return true;

        } else if (preference == mSilenceQuad4Pref) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_HONEY_4, TOGGLE_SILENT);
            return true;

        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String val = newValue.toString();
        if (preference == mLockscreenStylePref) {
            mLockscreenStyle = LockscreenStyle.getStyleById((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_STYLE_PREF,
                    LockscreenStyle.getIdByStyle(mLockscreenStyle));
            // updateStylePrefs(mLockscreenStyle, mInCallStyle);
            return true;
        }

        return false;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getContentResolver(), mCurrentCustomActivityString, uri)) {
            mCurrentCustomActivityPreference.setSummary(friendlyName);
        }
    }

}
