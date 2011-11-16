
package com.roman.tweaks.listeners;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.roman.tweaks.DownloadReceiver;
import com.roman.tweaks.Main;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class ROMUpdateClickListener implements OnPreferenceClickListener {

    Context mContext;

    private File externalStorageDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    private static String TAG = "ROM Update Click Listener";

    final String strPref_Download_ID = "PREF_DOWNLOAD_ID";

    SharedPreferences preferenceManager;

    public ROMUpdateClickListener(Context c) {
        mContext = c;
    }

    public boolean onPreferenceClick(Preference preference) {
        // File check = new File(externalStorageDir.getAbsolutePath() + "/" +
        // zipName);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        String md5 = preferenceManager.getString("file_md5", null);
        String url = preferenceManager.getString("file_url", null);
        String fileName = preferenceManager.getString("file_name", null);

        File f = new File(externalStorageDir.getAbsolutePath() + "/" + fileName);

        Log.e(TAG, "file name: " + f.getAbsolutePath());

        if (f.exists()) {
            Log.e(TAG, "Checking MD5");

            if (Main.md5(f).equals(md5)) {
                Log.e(TAG, "MD5 matches");

                Intent launch = new Intent(mContext, Main.class);
                launch.setAction(Main.BROADCAST_DL_FLASH);
                launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                mContext.startActivity(launch);
                return true;
            } else {
                Log.e(TAG, "MD5 mismatch");
                f.delete();
                // if (f.delete()) {
                enqueue(url, fileName);
                // } else {
                // preference.setSummary("An error occured, please tap to try again");
                // }
                return true;
            }
        }
        enqueue(url, fileName);

        return true;
    }

    public void enqueue(String url, String fileName) {
        Log.e(TAG, "enqueueing download: " + url);

        Uri down = Uri.parse(url);
        DownloadManager.Request req = new DownloadManager.Request(down);
         req.setTitle(fileName);
        // req.setDescription("Juggernaut ROM Update");
        req.setShowRunningNotification(true);
        req.setVisibleInDownloadsUi(true);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        // req.setDestinationUri(Uri.fromFile(f));

        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        long id = dm.enqueue(req);

        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putLong(strPref_Download_ID, id).apply();
    }
}
