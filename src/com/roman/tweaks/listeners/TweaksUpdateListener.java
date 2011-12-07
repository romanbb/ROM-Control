
package com.roman.tweaks.listeners;

import java.io.File;

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

import com.roman.tweaks.DownloadReceiver;
import com.roman.tweaks.Main;

public class TweaksUpdateListener implements OnPreferenceClickListener {

    Context mContext;

    private File externalStorageDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    private static String TAG = "VersionListener";

    final String strPref_Download_ID = "PREF_DOWNLOAD_ID";

    SharedPreferences preferenceManager;

    Uri uri;

    Handler handler;

    Looper l;

    public TweaksUpdateListener(Context c) {
        mContext = c;
    }

    public boolean onPreferenceClick(Preference preference) {

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        String md5 = preferenceManager.getString("tweaks_md5", null);
        String url = preferenceManager.getString("tweaks_url", null);
        String fileName = preferenceManager.getString("tweaks_filename", null);

        if (!new File(externalStorageDir.getAbsolutePath()).exists()) {
            new File(externalStorageDir.getAbsolutePath()).mkdir();
        }

        File f = new File(externalStorageDir.getAbsolutePath() + "/" + fileName);

        // Log.e(TAG, "file name: " + f.getAbsolutePath());

        if (f.exists()) {
            Log.i(TAG, "Checking MD5");

            if (Main.md5(f).equals(md5)) {
                Log.i(TAG, "MD5 matches");

                Intent installIntent = new Intent();
                installIntent.setAction(Intent.ACTION_VIEW);
                installIntent.setDataAndType(Uri.fromFile(f),
                        "application/vnd.android.package-archive");
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(installIntent);

                return true;
            } else {
                Log.e(TAG, "MD5 mismatch");
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
        Log.i(TAG, "enqueueing download");

        Uri down = Uri.parse(url);
        DownloadManager.Request req = new DownloadManager.Request(down);
        // req.setTitle(zipName);
        // req.setDescription("Juggernaut ROM Update");
        req.setShowRunningNotification(true);
        req.setVisibleInDownloadsUi(true);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        // req.setDestinationUri(Uri.fromFile(f));

        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        long id = dm.enqueue(req);

        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putLong("tweaks_id", id)
                .apply();
    }
}
