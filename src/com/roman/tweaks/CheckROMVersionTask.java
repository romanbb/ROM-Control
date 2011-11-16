
package com.roman.tweaks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class CheckROMVersionTask extends AsyncTask<Void, Boolean, Void> {

    String modVer = "";

    Context mContext;
    
    static String TAG = "CHeckROMVersion";

    public CheckROMVersionTask(Context c, String mod) {
        mContext = c;
        modVer = mod;
    }

    public void onProgressUpdate(Boolean... p) {
        Intent i = new Intent();
        i.setAction(Main.BROADCAST_HIDE_ROM);
        i.putExtra("show", p[0]);
        mContext.sendBroadcast(i);
    }

    @Override
    protected Void doInBackground(Void... params) {
        String downloaded = null;
        try {
            downloaded = downloadTextFromUrl(new URL(
            // "http://www.goo-inside.me/roms/edt/hercules/juggernaut_version_info"));
                    "http://www.rbirg.com/juggernaut_version_info"));
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
            Log.e("ROM TASK", downloaded);
            Scanner info = new Scanner(downloaded);

            if (!info.hasNext()) {

                publishProgress(false);
                return null;
            }

            String webversion = info.next();
            String url = info.next();
            String md5 = info.next();

            // check if its an update
            if (Double.parseDouble(modVer) < Double.parseDouble(webversion)) {
                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                        .putString("file_url", url).putString("file_md5", md5)
                        .putString("file_name", Uri.parse(url).getLastPathSegment()).apply();

                File externalStorageDir = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File f = new File(externalStorageDir.getAbsolutePath() + "/"
                        + Uri.parse(url).getLastPathSegment());

                if (f.exists() && !md5.equals(Main.md5(f))) {
                    f.delete();
                }

                publishProgress(true);
            } else {
                publishProgress(false);
            }
        }
        return null;
    }

    private String downloadTextFromUrl(URL url) throws IOException {

        // URL url = new URL(fileURL); // you can write here any link
        URLConnection ucon = url.openConnection();
        InputStream is = ucon.getInputStream();

        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        while (true) {
            int byteRead = bis.read();
            if (byteRead == -1)
                break;

            b.write(byteRead);
        }

        bis.close();
        is.close();
        b.close();

        return b.toString();

        // Log.d("Download", "downloaded file name:" + filePath);

    }

}
