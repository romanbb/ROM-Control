
package com.roman.tweaks;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

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

public class CheckVersionListener implements OnPreferenceClickListener {

    Context mContext;

    private File externalStorageDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    private String url;

    private String md5;

    private String zipName;

    DownloadReceiver mReceiver;

    private DownloadManager dm;

    private static String TAG = "VersionListener";

    final String strPref_Download_ID = "PREF_DOWNLOAD_ID";

    SharedPreferences preferenceManager;

    Uri uri;

    Handler handler;

    Looper l;

    Editor PrefEdit;

    public CheckVersionListener(Context c, String url, String md5) {
        mContext = c;
        this.url = url;
        this.md5 = md5;

        // zipName = url.substring(url.lastIndexOf("/"));

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(c);
        PrefEdit = preferenceManager.edit();
        uri = Uri.parse(url);
        zipName = uri.getLastPathSegment();
    }


    public boolean onPreferenceClick(Preference preference) {
        // File check = new File(externalStorageDir.getAbsolutePath() + "/" +
        // zipName);
        dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        File f = new File(externalStorageDir.getAbsolutePath() + "/" + zipName);

        Log.e(TAG, "file name: " + f.getAbsolutePath());

        if (f.exists()) {
            Log.e(TAG, "Checking MD5");

            if (Main.md5(f).equals(md5)) {
                Log.e(TAG, "MD5 matches");

                PrefEdit.putLong(strPref_Download_ID, 0);
                PrefEdit.putString("file_name", zipName);
                PrefEdit.putString("file_md5", md5);
                PrefEdit.commit();

                Intent launch = new Intent(mContext, Main.class);
                launch.setAction(Main.BROADCAST_DL_FLASH);
                launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Log.e(TAG, "Sending broadcast");
                mContext.startActivity(launch);
                return true;
            } else {
                Log.e(TAG, "MD5 mismatch");
//                if (f.delete()) {
                    enqueue();
                // } else {
                // preference.setSummary("An error occured, please tap to try again");
                // }
                return true;
            }
        }
        enqueue();

        return true;
    }

    public void enqueue() {
        Log.e(TAG, "enqueueing download");
        File f = new File(externalStorageDir.getAbsolutePath() + "/" + zipName);

        Uri down = Uri.parse(url);
        DownloadManager.Request req = new DownloadManager.Request(down);
        // req.setTitle(zipName);
        // req.setDescription("Juggernaut ROM Update");
        req.setShowRunningNotification(true);
        req.setVisibleInDownloadsUi(true);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, zipName);
        // req.setDestinationUri(Uri.fromFile(f));

        long id = dm.enqueue(req);

        PrefEdit.putLong(strPref_Download_ID, id);
        PrefEdit.putString("file_name", zipName);
        PrefEdit.putString("file_md5", md5);
        PrefEdit.commit();
    }
}
