
package com.roman.tweaks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.koushikdutta.rommanager.api.IROMManagerAPIService;
import com.roman.tweaks.listeners.ROMUpdateClickListener;
import com.roman.tweaks.listeners.TweaksUpdateListener;

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
    public static final String BROADCAST_DL_CANCEL = "com.roman.tweaks.Main.SHOW_DL_DIALOG";
    public static final String BROADCAST_DL_DOWNLOADING = "com.roman.tweaks.Main.DL_DOWNLOADING";
    public static final String BROADCAST_DL_FLASH = "com.roman.tweaks.Main.DL_FLASH";
    public static final String BROADCAST_DL_FAIL = "com.roman.tweaks.Main.DL_FAIL";

    public static final String BROADCAST_HIDE_ROM = "com.roman.tweaks.main.HIDE_ROM";
    public static final String BROADCAST_HIDE_TWEAKS = "com.roman.tweaks.main.HIDE_TWEAKS";
    public static final String BROADCAST_INSTALL_TWEAKS = "com.roman.tweaks.main.INSTALL_TWEAKS";

    public static final int DOWNLOAD_CANCEL_DIALOG = 666;

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

    int attempts = 0;

    private Handler mHandler = new Handler();

    DownloadReceiver dlReceiver;

    IROMManagerAPIService mService;

    Handler handler = new Handler();

    private boolean flashing = false;

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

        /*
         * temporary, remove at a later release to disable lockscreens and shit
         * for now
         */
        boolean disabled_before = PreferenceManager.getDefaultSharedPreferences(this).contains(
                "JUGGERNAUT_26_LOCKSCREEN_DISABLE");
        boolean disable_lockscreens = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("JUGGERNAUT_26_LOCKSCREEN_DISABLE", true);

        if (getVersion() > 31) {
            disable_lockscreens = false;
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putBoolean("JUGGERNAUT_26_LOCKSCREEN_DISABLE", false).apply();
        }

        if (!disabled_before && disable_lockscreens) {
            Settings.System.putInt(getContentResolver(), "tweaks_lockscreen_style", 0);
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putBoolean("JUGGERNAUT_26_LOCKSCREEN_DISABLE", true).apply();
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setMessage(
                            "Sorry! We've defaulted your lock screen to the Stock Samsung one while we fix the hot reboots!")
                    .setPositiveButton("Okay", new OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                        }
                    }).create().show();

        }
    }

    private int getVersion() {
        int version = -1;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            version = pInfo.versionCode;
        } catch (NameNotFoundException e1) {
            Log.e(this.getClass().getSimpleName(), "Name not found", e1);
        }
        return version;
    }

    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent()");
        String action = intent.getAction();

        if (action.equals(BROADCAST_DL_CANCEL)) {
            showDlDialog();
        } else if (action.equals(BROADCAST_DL_DOWNLOADING)) {

            // mROMVersion.setSummary(modversion_stripped
            // + " | Downloading new version. Click to cancel.");
            // mROMVersion.setOnPreferenceClickListener(new
            // OnPreferenceClickListener() {
            //
            // public boolean onPreferenceClick(Preference preference) {
            // showDlDialog();
            // return true;
            // }
            // });
        } else if (action.equals(BROADCAST_DL_FLASH)) {
            // mROMVersion.setSummary("Click to flash!");
            flashing = true;
            handler.postDelayed(flashTask, 1000);

        } else if (action.equals(BROADCAST_DL_FAIL)) {
            // mROMVersion.setSummary("Download failed. Please try again.");
            // mROMVersion.setOnPreferenceClickListener(updateROMListener);
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_HIDE_ROM);
        filter.addAction(BROADCAST_HIDE_TWEAKS);

        registerReceiver(updateReceiver, filter);

        super.onResume();

        if (getIntent().getAction().equals(BROADCAST_DL_CANCEL)
                || getIntent().getAction().equals(BROADCAST_DL_FLASH)) {

            // mROMVersion.setEnabled(false);
            // mROMVersion.setSummary("Download in progress");
            onNewIntent(getIntent());
        } else {
            new CheckROMVersionTask(mContext, modversion_stripped).execute();
            new CheckTweaksVersionTask(mContext).execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
        unregisterReceiver(updateReceiver);
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

                        final String md5_expected = PreferenceManager.getDefaultSharedPreferences(
                                mContext).getString("file_md5", "");
                        final String fileName = PreferenceManager.getDefaultSharedPreferences(
                                mContext).getString("file_name", "");
                        File f = new File(externalStorageDir.getAbsolutePath() + "/" + fileName);

                        f.delete();

                        dm.remove(download_id);
                        // mROMVersion.setEnabled(true);
                        new CheckROMVersionTask(mContext, modversion_stripped).execute();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String md5(File filename) {
        InputStream in;
        try {
            in = new FileInputStream(filename);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                md.update(buf, 0, len);
            }
            in.close();

            byte[] bytes = md.digest();

            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append("0123456789ABCDEF".charAt((b & 0xF0) >> 4));
                sb.append("0123456789ABCDEF".charAt((b & 0x0F)));
            }
            String hex = sb.toString();
            return hex.toLowerCase();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private OnPreferenceClickListener updateROMListener = new OnPreferenceClickListener() {

        public boolean onPreferenceClick(Preference preference) {
            // mROMVersion.setSummary("Checking...");
            new CheckROMVersionTask(mContext, modversion_stripped).execute();
            return true;
        }
    };

    private Runnable flashTask = new Runnable() {

        public void run() {
            // connect to rom manager. this should ideally be done only when
            // necessary.

            if (mService == null) {
                if (attempts++ < 3) {
                    handler.postDelayed(flashTask, (attempts + 1) * 500);
                    mROMVersion
                            .setSummary("Couldn't connect to ROM Manager. Retrying. Please don't exit the window! If you don't have ROM Manager you can't update through ROM Control!");
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
                // mROMVersion.setSummary("File was deleted somehow.");
                mHandler.postDelayed(new Runnable() {

                    public void run() {
                        new CheckROMVersionTask(mContext, modversion_stripped).execute();
                    }
                }, 5000);
                // mROMVersion.setSummary("An error occured, please tap to try again");
                return;
            }

            String md5 = md5(f);

            if (md5 != null && !md5_expected.equals(md5)) {

            } else if (!f.exists()) {
                // mROMVersion.setSummary("MD5 mismatch. Click to redownload.");
                mHandler.postDelayed(new Runnable() {

                    public void run() {
                        new CheckROMVersionTask(mContext, modversion_stripped).execute();
                    }
                }, 5000);
                return;
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this);
            AlertDialog alert;

            Log.e(TAG, "ROM Manager Premium found");
            dialog.setMessage(
                    "We STRONGLY encourage you to make a backup prior to flashing."
                            + "\n\nYou will find an option to backup your ROM up after you click Flash ROM."
                            + "\n\nIf you'd like to flash manually click reboot recovery"
                            + "\n\nHow would you like to proceed?").setCancelable(false)
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
                    }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).setNeutralButton("Reboot Recovery", new DialogInterface.OnClickListener() {

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

            // } else {
            // Toast.makeText(mContext, "MD5 mismatch.", Toast.LENGTH_LONG);
            // }

        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IROMManagerAPIService.Stub.asInterface(service);
        }
    };

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean show = intent.getBooleanExtra("show", false);

            if (flashing)
                return;

            if (action.equals(BROADCAST_HIDE_ROM)) {
                if (show) {
                    Preference mROMUpdatePreference = new Preference(context);
                    mROMUpdatePreference.setTitle("Juggernaut ROM Update");

                    Spannable summary = new SpannableString("Update available. Click to download.");
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);

                    mROMUpdatePreference.setSummary(summary);
                    mROMUpdatePreference.setOnPreferenceClickListener(new ROMUpdateClickListener(
                            context));
                    mROMUpdatePreference.setKey("rom_update");

                    if (findPreference("rom_update") == null)
                        ((PreferenceGroup) findPreference("rom_control"))
                                .addPreference(mROMUpdatePreference);
                }
            } else if (action.equals(BROADCAST_HIDE_TWEAKS)) {
                if (show) {
                    Preference mTweaksUpdatePreference = new Preference(context);
                    mTweaksUpdatePreference.setTitle("ROM Control Update Available");
                    Spannable summary = new SpannableString("Tap to download the new version");
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);

                    mTweaksUpdatePreference.setSummary(summary);
                    mTweaksUpdatePreference.setOnPreferenceClickListener(new TweaksUpdateListener(
                            context));
                    mTweaksUpdatePreference.setKey("tweaks_update");

                    if (findPreference("tweaks_update") == null)
                        ((PreferenceGroup) findPreference("rom_control"))
                                .addPreference(mTweaksUpdatePreference);
                } else {
                    // ((PreferenceGroup)
                    // findPreference("rom_control")).removePreference(mOverscrollPref);
                }
            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pref_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.check_updates:
                Toast.makeText(mContext, "Updates will appear below ROM version if there are any.",
                        Toast.LENGTH_LONG).show();
                new CheckROMVersionTask(mContext, modversion_stripped).execute();
                new CheckTweaksVersionTask(mContext).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
