
package com.roman.tweaks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.rommanager.api.IClockworkRecoveryScriptBuilder;
import com.koushikdutta.rommanager.api.IROMManagerAPIService;

public class Main extends PreferenceActivity implements OnPreferenceChangeListener {
    String pref;

    Context mContext;

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

    private static final String PREF_ROMVERSION = "rom_version";

    public static final int DOWNLOAD_CANCEL_DIALOG = 666;

    public static final String BROADCAST_DL_CANCEL = "com.roman.tweaks.Main.SHOW_DL_DIALOG";

    public static final String BROADCAST_DL_DOWNLOADING = "com.roman.tweaks.Main.DL_DOWNLOADING";

    public static final String BROADCAST_DL_FLASH = "com.roman.tweaks.Main.DL_FLASH";

    public static final String BROADCAST_DL_FAIL = "com.roman.tweaks.Main.DL_FAIL";

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

    Preference mROMVersion;

    String modversion;

    String modversion_stripped;

    String webversion;

    private Handler mHandler = new Handler();

    DownloadReceiver dlReceiver;

    IROMManagerAPIService mService;

    Handler handler = new Handler();

    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IROMManagerAPIService.Stub.asInterface(service);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.main_prefs);
        mContext = this.getApplicationContext();
        if (isKanged("Juggernaut")) {
            addPreferencesFromResource(R.xml.kanged);
            mTwitter = findPreference(TWITTER_PREF);
            mThread = findPreference(THREAD_PREF);

        }
        // PreferenceScreen prefs = getPreferenceScreen();

        d = new Dialog(this);
        builder = new AlertDialog.Builder(this);

        boolean checked = (Settings.System.getInt(getContentResolver(), "tweaks_crt_off", 1) == 1) ? true
                : false;
        // Log.e(TAG, "Inintial CRT_OFF == " + checked);
        mAnimateScreenOff = (CheckBoxPreference) findPreference(PREF_SCREEN_OFF);
        mAnimateScreenOff.setChecked(checked);

        checked = (Settings.System.getInt(getContentResolver(), "tweaks_crt_on", 1) == 1) ? true
                : false;
        // Log.e(TAG, "Inintial CRT_ON == " + checked);
        mAnimateScreenOn = (CheckBoxPreference) findPreference(PREF_SCREEN_ON);
        mAnimateScreenOn.setChecked(checked);

        mOverscrollPref = (ListPreference) findPreference(OVERSCROLL_PREF);
        int overscrollEffect = Settings.System.getInt(getContentResolver(), "overscroll_effect", 1);
        mOverscrollPref.setValue(String.valueOf(overscrollEffect));
        mOverscrollPref.setOnPreferenceChangeListener(this);

        /* ROM info */
        mROMVersion = findPreference(PREF_ROMVERSION);
        modversion_stripped = modversion.replaceFirst("Juggernaut-v", "");
        mROMVersion.setSummary(modversion);

        /** stuff to remove **/
        ((PreferenceGroup) findPreference("other_cat")).removePreference(mOverscrollPref);
        ((PreferenceGroup) findPreference("other_cat")).removePreference(mAnimateScreenOn);
        ((PreferenceGroup) findPreference("statusbar_cat"))
                .removePreference(findPreference("quick_settings"));

    }

    int attempts = 0;

    private Runnable flashTask = new Runnable() {

        public void run() {
            // connect to rom manager. this should ideally be done only when
            // necessary.

            if (mService == null) {
                if (attempts++ < 3) {
                    handler.postDelayed(flashTask, 500);
                    mROMVersion
                            .setSummary("Couldn't connect to ROM Manager. Retrying. Please don't exit the window!");
                }
                return;

            }
            mROMVersion.setSummary(null);
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) mContext
                    .getSystemService(ns);
            mNotificationManager.cancel(1);

            File externalStorageDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            final String md5_expected = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString("file_md5", "");
            final String fileName = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString("file_name", "");
            File f = new File(externalStorageDir.getAbsolutePath() + "/" + fileName);


            Log.i(TAG, "flashtask, filename: " + f.getAbsolutePath());
            if (!f.exists()) {
                Log.e(TAG, f.getAbsolutePath() + " doesn't exist!");
                mROMVersion.setSummary("File was deleted somehow.");
                mHandler.postDelayed(new Runnable() {

                    public void run() {
                        new CheckVersionTask(mContext, modversion_stripped, mROMVersion).execute();
                    }
                }, 5000);
                // mROMVersion.setSummary("An error occured, please tap to try again");
                return;
            }
            
            String md5 = md5(f);


            if (md5 != null && !md5_expected.equals(md5)) {

            } else if (!f.exists()) {
                mROMVersion.setSummary("MD5 mismatch. Click to redownload.");
                mHandler.postDelayed(new Runnable() {

                    public void run() {
                        new CheckVersionTask(mContext, modversion_stripped, mROMVersion).execute();
                    }
                }, 5000);
                return;
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this);
            AlertDialog alert;
            if (false) { //mService.isPremium()
                dialog.setMessage(
                        "Looks like MD5s match on the download. We're ready to cross to the other side. \n\n"
                                + "Are you ready to flash? A backup will automatically be created for you.")
                        .setCancelable(false).setTitle("Read me!")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                try {
                                    IClockworkRecoveryScriptBuilder builder = mService
                                            .createClockworkRecoveryScriptBuilder();

                                    builder.print("");
                                    builder.print("");
                                    builder.print("");
                                    builder.print("We're going to do a backup for you! You're welcome!");
                                    builder.print("");
                                    builder.print("");
                                    // builder.backup();
                                    builder.print("");
                                    builder.print("");
                                    builder.print("");
                                    builder.print("Flashing Juggernaut!");
                                    builder.installZip("/emmc/Download/" + fileName);
                                    builder.print("");
                                    builder.print("");
                                    builder.print("");
                                    builder.print("All done!");

                                    builder.runScript();
                                } catch (RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    Log.e(TAG, "Error with runScript()");
                                }

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alert = dialog.create();
                alert.show();
            } else {
                Log.e(TAG, "ROM Manager Premium found");
                dialog.setMessage(
                        "We STRONGLY encourage you to make a backup prior to flashing."
                                + "\n\nYou will find an option to backup your ROM up after you click Flash ROM."
                                + "\n\nIf you'd like to flash manually click reboot recovery"
                                + "\n\nHow would you like to proceed?")
                        .setCancelable(false)
                        .setTitle("Read me!")
                        .setPositiveButton("Flash ROM!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                try {
                                    mService.installZip("/emmc/Download/" + fileName);

                                } catch (RemoteException e) {
                                    // TODO Auto-generated catch
                                    // block
                                    e.printStackTrace();
                                    Log.e(TAG, "Error with runScript()");
                                }

                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNeutralButton("Reboot Recovery",
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            mService.rebootRecovery();
                                        } catch (RemoteException e) {
                                            // TODO Auto-generated catch
                                            // block
                                            e.printStackTrace();
                                        }
                                    }
                                });
                alert = dialog.create();
                alert.show();
            }

            // } else {
            // Toast.makeText(mContext, "MD5 mismatch.", Toast.LENGTH_LONG);
            // }

        }
    };

    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent()");
        String action = intent.getAction();

        if (action.equals(BROADCAST_DL_CANCEL)) {
            showDlDialog();
        } else if (action.equals(BROADCAST_DL_DOWNLOADING)) {

            mROMVersion.setSummary(modversion_stripped
                    + " | Downloading new version. Click to cancel.");
            mROMVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    showDlDialog();
                    return true;
                }
            });
        } else if (action.equals(BROADCAST_DL_FLASH)) {
            // mROMVersion.setSummary("Click to flash!");
            handler.postDelayed(flashTask, 1000);

        } else if (action.equals(BROADCAST_DL_FAIL)) {
            mROMVersion.setSummary("Download failed. Please try again.");
            // Toast.makeText(mContext, "download failed, please retry again!",
            // Toast.LENGTH_LONG);
        }

    }

    @Override
    protected void onResume() {
        Intent i = new Intent("com.koushikdutta.rommanager.api.BIND");
        try {
            Log.e(TAG, "Binding with ROM Manager");
            bindService(i, serviceConnection, Service.BIND_AUTO_CREATE
                    | Service.BIND_NOT_FOREGROUND);

            // premium = mService.isPremium();
        } catch (Exception ex) {
            // connecting to rom manager failed, is it installed and
            // 4.5.0.0+?
            ex.printStackTrace();
        }
        super.onResume();

        if (getIntent().getAction().equals(BROADCAST_DL_CANCEL)
                || getIntent().getAction().equals(BROADCAST_DL_FLASH)) {

            // mROMVersion.setEnabled(false);
            mROMVersion.setSummary("Download in progress");
            onNewIntent(getIntent());
        } else {
            new CheckVersionTask(mContext, modversion_stripped, mROMVersion).execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    public boolean isKanged(String lol) {
        modversion = "";

        StringBuilder sup = new StringBuilder();
        sup.append("J");

        Process ifc = null;
        try {
            sup.append("u");
            ifc = Runtime.getRuntime().exec("getprop ro.modversion");
            BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
            sup.append("g");

            modversion = bis.readLine();
            // Log.d("Tweaks", "Modversion: " + modversion);
        } catch (java.io.IOException e) {
            return true;
        } finally {
            sup.append("g");
        }
        ifc.destroy();

        sup.append("e");

        if (modversion.contains(lol) && modversion.contains("Juggernaut")
                && modversion.contains(sup)) {
            return false;
        }

        return true;
    }

    public void openUserActivity(Context sup) {
        String activityUri = Settings.System.getString(sup.getContentResolver(),
                "tweaks_rosie_activity_intent");

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
                Toast.makeText(sup, "Invalid activity intent", Toast.LENGTH_SHORT);
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_ACTIVITY && resultCode != Activity.RESULT_CANCELED) {
            // launch the application that we just picked
            // startActivity(data);

            PackageManager pm = getPackageManager();
            ResolveInfo ac = pm.resolveActivity(data, PackageManager.MATCH_DEFAULT_ONLY);

            String appName = ac.loadLabel(pm).toString();

            String uri = data.toUri(Intent.URI_INTENT_SCHEME);
            // uri = uri.substring(7, uri.length());

            Settings.System.putString(getContentResolver(), "tweaks_rosie_activity_name", appName);
            Settings.System.putString(getContentResolver(), "tweaks_rosie_activity_intent", uri);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mOverscrollPref) {
            int overscrollEffect = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), "overscroll_effect", overscrollEffect);
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

            Settings.System.putInt(getContentResolver(), "tweaks_crt_off", checked ? 1 : 0);
            Log.e(TAG,
                    "CRT off set to : "
                            + (Settings.System.getInt(getContentResolver(), "tweaks_crt_off", 0) == 1 ? "on"
                                    : "off"));
            return true;
        } else if (preference == mAnimateScreenOn) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();

            Settings.System.putInt(getContentResolver(), "tweaks_crt_on", checked ? 1 : 0);
            Log.e(TAG,
                    "CRT on set to : "
                            + (Settings.System.getInt(getContentResolver(), "tweaks_crt_on", 0) == 1 ? "on"
                                    : "off"));
            return true;
        } else if (preference == mTwitter) {

            Intent twitterIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://twitter.com/romanbb"));
            startActivity(twitterIntent);
            return true;
        } else if (preference == mThread) {

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://goo.gl/EGY58"));
            startActivity(i);
            return true;
        } else if (preference == mROMVersion) {
            mROMVersion.setSummary("Checking...");
            new CheckVersionTask(mContext, modversion_stripped, mROMVersion).execute();
        }

        return false;
    }

    private void showDlDialog() {
        final DownloadManager dm = (DownloadManager) mContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
        final long download_id = PreferenceManager.getDefaultSharedPreferences(mContext).getLong(
                DownloadReceiver.strPref_Download_ID, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setMessage("Do you want to stop the download?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                        File externalStorageDir = Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                        final String md5_expected = PreferenceManager.getDefaultSharedPreferences(mContext)
                                .getString("file_md5", "");
                        final String fileName = PreferenceManager.getDefaultSharedPreferences(mContext)
                                .getString("file_name", "");
                        File f = new File(externalStorageDir.getAbsolutePath() + "/" + fileName);

                        f.delete();
                        
                        dm.remove(download_id);
                        mROMVersion.setEnabled(true);
                        new CheckVersionTask(mContext, modversion_stripped, mROMVersion).execute();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String md5(File f) {
        MessageDigest digest;
        InputStream is = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            is = new FileInputStream(f);

            byte[] buffer = new byte[8192];
            int read = 0;

            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            is.close();
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return null;

    }
}
