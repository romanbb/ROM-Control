
package com.roman.tweaks;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class CheckTweaksVersionTask extends AsyncTask<Void, Boolean, Void> {

    private static final String TAG = "CheckTweaksVersion";

    String md5;

    String url;

    Context mContext;

    public CheckTweaksVersionTask(Context c) {
        mContext = c;
    }

    public void onProgressUpdate(Boolean... p) {
        Intent i = new Intent();
        i.setAction(Main.BROADCAST_HIDE_TWEAKS);
        i.putExtra("show", p[0]);
        mContext.sendBroadcast(i);
    }

    @Override
    protected Void doInBackground(Void... params) {

        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo("com.roman.tweaks", 0);
        } catch (NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int localVersion = pi.versionCode;
        String downloaded = null;
        try {
            downloaded = downloadTextFromUrl(new URL(
                    // "http://www.goo-inside.me/roms/edt/hercules/juggernaut_version_info"));
                    "http://www.rbirg.com/juggernaut_tweaks_version_info"));
            // Log.e(TAG, downloaded);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            downloaded = null;
        } catch (IOException e) {
            e.printStackTrace();
            downloaded = null;
        }

        if (downloaded == null) {
            publishProgress(false);
        } else {
            Log.i(TAG, downloaded);
            Scanner info = new Scanner(downloaded);

            if (!info.hasNext()) {
                publishProgress(false);
                return null;
            }

            String webversion = info.next();
            url = info.next();
            md5 = info.next();

            // check if its an update
            if (localVersion < Integer.parseInt(webversion)) {

                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                        .putString("tweaks_url", url).putString("tweaks_md5", md5)
                        .putString("tweaks_filename", Uri.parse(url).getLastPathSegment()).apply();

                publishProgress(true);
            } else {
                File externalStorageDir = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File f = new File(externalStorageDir.getAbsolutePath() + "/"
                        + Uri.parse(url).getLastPathSegment());

                if (f.exists())
                    f.delete();

                publishProgress(false);
            }
        }
        return null;
    }

    private String downloadTextFromUrl(URL url) throws IOException {

        Log.d(TAG, "downloading from: " + url.toString());
        HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
        ucon.setUseCaches(false);

        BufferedInputStream bis = new BufferedInputStream(ucon.getInputStream());
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        while (true) {
            int byteRead = bis.read();
            if (byteRead == -1)
                break;

            b.write(byteRead);
        }

        bis.close();
        b.close();

        ucon.disconnect();
        return b.toString();

        // Log.d("Download", "downloaded file name:" + filePath);

    }

}
