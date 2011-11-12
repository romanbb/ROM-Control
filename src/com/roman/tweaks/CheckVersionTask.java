
package com.roman.tweaks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.Preference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

public class CheckVersionTask extends AsyncTask<Void, Boolean, Void> {

    String modVer = "";

    Preference pref;

    Spannable summary = null;

    String sSummary;

    String md5;

    String url;

    Context mContext;

    public CheckVersionTask(Context c, String mod, Preference p) {
        mContext = c;
        modVer = mod;
        pref = p;
    }

    public void onProgressUpdate(Boolean... p) {
        if (summary == null)
            pref.setSummary(sSummary);
        else
            pref.setSummary(summary);

        pref.setEnabled(true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        String downloaded = null;
        try {
            downloaded = downloadTextFromUrl(new URL(
                    //"http://www.goo-inside.me/roms/edt/hercules/juggernaut_version_info"));
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
            sSummary = "Couldn't check for updates. Tap to recheck";
            publishProgress(false);
        } else {
            Log.e("TASK", downloaded);
            Scanner info = new Scanner(downloaded);

            if (!info.hasNext()) {
                sSummary = "Couldn't check for updates. Tap to recheck.";
                publishProgress(false);
                return null;
            }

            String webversion = info.next();
            url = info.next();
            md5 = info.next();

            // check if its an update
            if (Double.parseDouble(modVer) < Double.parseDouble(webversion)) {
                pref.setOnPreferenceClickListener(new CheckVersionListener(mContext, url, md5));

                String text = modVer + " | " + "New version available! Tap to update.";

                summary = new SpannableString(text);
                summary.setSpan(new ForegroundColorSpan(Color.RED), text.indexOf("|") + 1,
                        summary.length(), 0);

                publishProgress(true);
            } else {
                String text = modVer + " | " + "You have the newest version. You're awesome.";

                summary = new SpannableString(text);
                summary.setSpan(new ForegroundColorSpan(Color.GREEN), text.indexOf("|") + 1,
                        summary.length(), 0);
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
