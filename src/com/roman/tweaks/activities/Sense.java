
package com.roman.tweaks.activities;

import java.io.File;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.roman.tweaks.R;
import com.roman.tweaks.ShellInterface;

public class Sense extends PreferenceActivity {

    public static final String TAG = "Sense Tweaks";

    public static final int SELECT_ACTIVITY = 2;
    private static final String PREF_CUSTOM_ACTIVITY = "pref_rosie_activity";
    private static final String PREF_RECENT_APPS = "show_recent_apps";
    private static final String PREF_RESET_ROSIE = "use_custom_rosie_activity";
    private static final String PREF_ENABLE_SCREENSHOTS = "enable_screenshots";
    private static final String PREF_ENABLE_UNLOCK_ANIM = "enable_unlock_animation";
    private static final String PREF_NUM_COLUMNS_APP_DRAWER = "app_drawer_app_columns";
    private static final String PREF_SUPER_QUICK_SETTINGS = "super_quick_settings";
    private static final String PREF_USE_PAGINATED_APPS = "paginated_apps";
    private static final String PREF_KILL_ROSIE = "kill_rosie";

    CheckBoxPreference mShowRecentApps;
    CheckBoxPreference mEnableScreenshots;
    CheckBoxPreference mEnableUnlockAnimation;
    CheckBoxPreference mEnableFiveColumns;
    CheckBoxPreference mEnableQuickQuickSettings;
    Preference mRosieActivity;
    CheckBoxPreference mUseRosieCustomActivity;
    CheckBoxPreference mUsePaginatedAppDrawer;
    Preference mKillRosie;

    @Override
    protected void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);

        Log.e(TAG, "before xml");

        addPreferencesFromResource(R.xml.sense_prefs);

        Log.e(TAG, "after xml");

        PreferenceScreen prefs = getPreferenceScreen();

        mRosieActivity = prefs.findPreference(PREF_CUSTOM_ACTIVITY);
        mKillRosie = prefs.findPreference(PREF_KILL_ROSIE);
        mUseRosieCustomActivity = (CheckBoxPreference) prefs.findPreference(PREF_RESET_ROSIE);
        mShowRecentApps = (CheckBoxPreference) prefs.findPreference(PREF_RECENT_APPS);
        mEnableScreenshots = (CheckBoxPreference) prefs.findPreference(PREF_ENABLE_SCREENSHOTS);
        mEnableUnlockAnimation = (CheckBoxPreference) prefs.findPreference(PREF_ENABLE_UNLOCK_ANIM);
        mEnableFiveColumns = (CheckBoxPreference) prefs.findPreference(PREF_NUM_COLUMNS_APP_DRAWER);
        mEnableQuickQuickSettings = (CheckBoxPreference) prefs.findPreference(PREF_SUPER_QUICK_SETTINGS);
        mUsePaginatedAppDrawer = (CheckBoxPreference) prefs.findPreference(PREF_USE_PAGINATED_APPS);

        String activityName = Settings.System.getString(getContentResolver(), "tweaks_rosie_activity_name");
        mRosieActivity.setSummary(activityName == null ? "Browser" : activityName);

        boolean checked = (Settings.System.getInt(getContentResolver(), "tweaks_show_recent_apps", 0) == 1) ? true : false;
        mShowRecentApps.setChecked(checked);

        checked = (Settings.System.getInt(getContentResolver(), "tweaks_rosie_remap_personalize", 1) == 1) ? true : false;
        mUseRosieCustomActivity.setChecked(checked);

        checked = (Settings.System.getInt(getContentResolver(), "tweaks_enable_screenshot", 1) == 1) ? true : false;
        mEnableScreenshots.setChecked(checked);

        /* unlock animation */
        checked = (Settings.System.getInt(getContentResolver(), "tweaks_rosie_skip_unlock_animation", 0) == 0);
        mEnableUnlockAnimation.setChecked(checked);

        /* columns */
        checked = (Settings.System.getInt(getContentResolver(), "tweaks_rosie_app_drawer_columns", 4) == 5);
        mEnableFiveColumns.setChecked(checked);

        /* quick quick settings */
        checked = (Settings.System.getInt(getContentResolver(), "tweaks_auto_quick_settings", 0) == 1);
        mEnableQuickQuickSettings.setChecked(checked);

        /* page drawer */
        checked = (Settings.System.getInt(getContentResolver(), "tweaks_rosie_use_pages", 1) == 1);
        mUsePaginatedAppDrawer.setChecked(checked);

        /* check rosie */
        if (!new File("/system/app/Rosie.apk").exists()) {
            prefs.removePreference(prefs.findPreference("sense_tweaks_id"));
            prefs.removePreference(prefs.findPreference("sense_restart_rosie_cat"));
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen,
            Preference preference) {
        if (preference == mShowRecentApps) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_show_recent_apps", checked ? 1 : 0);
            return true;

        } else if (preference == mRosieActivity) {
            // launch native android activity picker
            preference
                    .setSummary("**** List is loading, please be patient!!****");

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, SELECT_ACTIVITY);

            return true;

        } else if (preference == mUseRosieCustomActivity) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_rosie_remap_personalize", checked ? 1 : 0);
            if(!checked)
                Settings.System.putString(getContentResolver(),
                        "tweaks_rosie_activity_name", "Personalize");
            return true;

        } else if (preference == mEnableScreenshots) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_enable_screenshot", checked ? 1 : 0);
            return true;

        } else if (preference == mEnableUnlockAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_rosie_skip_unlock_animation", !checked ? 1 : 0);
            return true;

        } else if (preference == mEnableFiveColumns) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_rosie_app_drawer_columns", checked ? 5 : 4);
            return true;
        } else if (preference == mEnableQuickQuickSettings) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_auto_quick_settings", checked ? 1 : 0);

            return true;
        } else if (preference == mUsePaginatedAppDrawer) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(),
                    "tweaks_rosie_use_pages", checked ? 1 : 0);
            return true;

        } else if (preference == mKillRosie) {
            preference.setSummary("She's dead, Jim!");

            if (ShellInterface.isSuAvailable())
                ShellInterface.runCommand("pkill -TERM -f com.htc.launcher");

            return true;
        }

        return false;
    }

    protected void onResume() {
        super.onResume();
        String activityName = Settings.System.getString(getContentResolver(),
                "tweaks_rosie_activity_name");
        mRosieActivity.setSummary(activityName == null ? "Internet"
                : activityName);
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

}
