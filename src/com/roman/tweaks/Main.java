
package com.roman.tweaks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class Main extends PreferenceActivity implements
        OnPreferenceChangeListener {
    String pref;
    Context context;
    Dialog d;
    AlertDialog.Builder builder;

    public static final String TAG = "Roman";

    public static final int SELECT_ACTIVITY = 2;

    private static final String PREF_BATTERY = "battery_options_pref";
    private static final String PREF_CLOCK = "clock_options_pref";
    private static final String PREF_SIGNAL = "signal_options_pref";
    private static final String PREF_SCREEN_OFF = "pref_animate_off";
    private static final String PREF_SCREEN_ON = "pref_animate_on";
    private static final String OVERSCROLL_PREF = "pref_overscroll_effect";
    private static final String TWITTER_PREF = "twitter_link";
    private static final String THREAD_PREF = "thread_link";

    CheckBoxPreference mEnableGPS;
    CheckBoxPreference mShowRecentApps;
    CheckBoxPreference mAnimateScreenOff;
    CheckBoxPreference mAnimateScreenOn;
    ListPreference mOverscrollPref;
    Preference mBattery;
    Preference mClock;
    Preference mSignal;
    Preference mTwitter;
    Preference mThread;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();

        Log.e(Main.class.toString(), "HI!");

        if (isKanged("Bulletproof")) {
            addPreferencesFromResource(R.xml.kanged);
            mTwitter = findPreference(TWITTER_PREF);
            mThread = findPreference(THREAD_PREF);

        }

        addPreferencesFromResource(R.xml.main_prefs);

        PreferenceScreen prefs = getPreferenceScreen();

        d = new Dialog(this);
        builder = new AlertDialog.Builder(this);

        mClock = prefs.findPreference(PREF_CLOCK);
        mBattery = prefs.findPreference(PREF_BATTERY);
        mSignal = prefs.findPreference(PREF_SIGNAL);

        boolean checked = (Settings.System.getInt(getContentResolver(),
                "tweaks_crt_off", 1) == 1) ? true : false;
        Log.e(TAG, "Inintial CRT_OFF == " + checked);
        mAnimateScreenOff = (CheckBoxPreference) prefs
                .findPreference(PREF_SCREEN_OFF);
        mAnimateScreenOff.setChecked(checked);

        checked = (Settings.System.getInt(getContentResolver(),
                "tweaks_crt_on", 1) == 1) ? true : false;
        Log.e(TAG, "Inintial CRT_ON == " + checked);
        mAnimateScreenOn = (CheckBoxPreference) prefs
                .findPreference(PREF_SCREEN_ON);
        mAnimateScreenOn.setChecked(checked);

        mOverscrollPref = (ListPreference) prefs
                .findPreference(OVERSCROLL_PREF);
        int overscrollEffect = Settings.System.getInt(getContentResolver(),
                "overscroll_effect", 1);
        mOverscrollPref.setValue(String.valueOf(overscrollEffect));
        mOverscrollPref.setOnPreferenceChangeListener(this);

    }

    // flags for setPowerState
    private static final int SCREEN_ON_BIT = 0x00000001;
    private static final int SCREEN_BRIGHT_BIT = 0x00000002;
    private static final int BUTTON_BRIGHT_BIT = 0x00000004;
    private static final int KEYBOARD_BRIGHT_BIT = 0x00000008;
    private static final int BATTERY_LOW_BIT = 0x00000010;
    private int mButtonBrightnessOverride = 0;
    private int mButtonTimeoutSetting = 0;
    private int mLightSensorButtonBrightness = 0;
    private boolean mUseSoftwareAutoBrightness = false;

    private int applyButtonState(int state) {
        int brightness = -1;

        // 1
        if ((state & BATTERY_LOW_BIT) != 0) {
            // do not override brightness if the battery is low
            return state;
        }

        // 2
        if (mButtonBrightnessOverride >= 0) {
            brightness = mButtonBrightnessOverride;
        } else if (mLightSensorButtonBrightness >= 0 && mButtonTimeoutSetting == -2) {
            // 3
            brightness = mLightSensorButtonBrightness;
        }else if (mButtonTimeoutSetting != -3 && mButtonTimeoutSetting == -1) {
            brightness = 0xFF;
        } else if(mButtonTimeoutSetting == 0) {
            brightness = 0;            
        }

        if (brightness > 0) {
            return state | BUTTON_BRIGHT_BIT;
        } else if (brightness == 0) {
            return state & ~BUTTON_BRIGHT_BIT;
        } else {
            return state;
        }
    }

    public boolean isKanged(String lol) {
        String modversion = "";

        StringBuilder sup = new StringBuilder();
        sup.append("B");

        Process ifc = null;
        try {
            sup.append("u");
            ifc = Runtime.getRuntime().exec("getprop ro.modversion");
            BufferedReader bis = new BufferedReader(new InputStreamReader(
                    ifc.getInputStream()));
            sup.append("l");

            modversion = bis.readLine();
            // Log.d("Tweaks", "Modversion: " + modversion);
        } catch (java.io.IOException e) {
            return true;
        } finally {
            sup.append("l");
        }
        ifc.destroy();

        sup.append("e");

        if (modversion.contains(lol) && modversion.contains("Bulletproof")
                && modversion.contains(sup)) {
            return false;
        }

        return true;
    }

    public void openUserActivity(Context sup) {
        String activityUri = Settings.System.getString(
                sup.getContentResolver(), "tweaks_rosie_activity_intent");

        if (activityUri == null) {
            String packageName = "com.android.browser";
            String className = "com.android.browser.BrowserActivity";
            Intent internetIntent = new Intent(Intent.ACTION_VIEW);
            internetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            internetIntent.setClassName(packageName, className);
            sup.startActivity(internetIntent);
        } else {
            try {
                sup.startActivity(Intent.getIntent(activityUri));
            } catch (URISyntaxException e) {
                Toast.makeText(sup, "Invalid activity intent",
                        Toast.LENGTH_SHORT);
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_ACTIVITY
                && resultCode != Activity.RESULT_CANCELED) {
            // launch the application that we just picked
            // startActivity(data);

            PackageManager pm = getPackageManager();
            ResolveInfo ac = pm.resolveActivity(data,
                    PackageManager.MATCH_DEFAULT_ONLY);

            String appName = ac.loadLabel(pm).toString();

            String uri = data.toUri(Intent.URI_INTENT_SCHEME);
            // uri = uri.substring(7, uri.length());

            Settings.System.putString(getContentResolver(),
                    "tweaks_rosie_activity_name", appName);
            Settings.System.putString(getContentResolver(),
                    "tweaks_rosie_activity_intent", uri);
        }
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

    public boolean onPreferenceTreeClick(PreferenceScreen screen,
            Preference preference) {
        if (preference == mShowRecentApps) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_show_recent_apps", checked ? 1 : 0);
            return true;
        } else if (preference == mAnimateScreenOff) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(), "tweaks_crt_off",
                    checked ? 1 : 0);
            Log.e(TAG,
                    "CRT off set to : "
                            + (Settings.System.getInt(getContentResolver(),
                                    "tweaks_crt_off", 0) == 1 ? "on" : "off"));
            return true;
        } else if (preference == mAnimateScreenOn) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(), "tweaks_crt_on",
                    checked ? 1 : 0);
            Log.e(TAG,
                    "CRT on set to : "
                            + (Settings.System.getInt(getContentResolver(),
                                    "tweaks_crt_on", 0) == 1 ? "on" : "off"));
            return true;
        } else if (preference == mTwitter) {

            Intent twitterIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://twitter.com/romanbb"));
            startActivity(twitterIntent);
            return true;
        } else if (preference == mThread) {

            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://goo.gl/EGY58"));
            startActivity(i);
            return true;
        }

        return false;
    }

}
