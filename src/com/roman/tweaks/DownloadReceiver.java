
package com.roman.tweaks;

import java.io.File;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadReceiver extends BroadcastReceiver {

    private static String TAG = "DL Receiver";

    final public static String strPref_Download_ID = "PREF_DOWNLOAD_ID";

    SharedPreferences preferenceManager;

    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG, "Receiver");
        final DownloadManager dm = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);

        long download_id = -1;

        if (intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID))
            download_id = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);

        long rom_id = PreferenceManager.getDefaultSharedPreferences(context).getLong(
                "PREF_DOWNLOAD_ID", 0);
        long tweaks_id = PreferenceManager.getDefaultSharedPreferences(context).getLong(
                "tweaks_id", 0);

        boolean rom_dl = rom_id == download_id;
        boolean tweaks_dl = tweaks_id == download_id;

        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            Log.e(TAG, "Reciving " + DownloadManager.ACTION_DOWNLOAD_COMPLETE);

            if (!(rom_dl || tweaks_dl)) {
                Log.e(TAG, "dl id mismatch");
                Log.e(TAG, "rom id: " + rom_id + " | tweaks id: " + tweaks_id);
                Log.e(TAG, download_id + "");
                return;
            }

            File externalStorageDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String md5_expected = null;
            String fileName = null;

            if (rom_dl) {
                md5_expected = PreferenceManager.getDefaultSharedPreferences(context).getString(
                        "file_md5", "");
                fileName = PreferenceManager.getDefaultSharedPreferences(context).getString(
                        "file_name", "");
            } else if (tweaks_dl) {
                md5_expected = PreferenceManager.getDefaultSharedPreferences(context).getString(
                        "tweaks_md5", "");
                fileName = PreferenceManager.getDefaultSharedPreferences(context).getString(
                        "tweaks_filename", "");
            }

            File f = new File(externalStorageDir.getAbsolutePath() + "/" + fileName);

            if (!f.exists()) {
                Log.e(TAG, "DL Receiver file doesn't exist!, no notification");
                return;
            }

            String md5 = Main.md5(f);
            if (!md5_expected.equals(md5)) {
                Log.e(TAG, "DL Receiver md5 mismatch, no notification");
                Log.e(TAG, "Expected MD5: " + md5_expected + ", actual md5: " + md5);
                // Log.e(TAG, "so we're deleting the file and the result is: " +
                // f.delete());
                return;
            }

            Log.e(TAG, "displaying notification");
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(ns);

            CharSequence tickerText = null;
            CharSequence contentTitle = null;
            CharSequence contentText = null;
            int icon = 0;
            Intent intentToPlace = null;
            long when = System.currentTimeMillis();

            if (rom_dl) {
                icon = R.drawable.ic_settings_icon_juggernaut;
                tickerText = "Juggernaut ROM download completed"; // ticker-text
                contentTitle = "Juggernaut ROM Update";
                contentText = "Tap when you're ready to update!";

                intentToPlace = new Intent(context, Main.class);
                intentToPlace.setAction(Main.BROADCAST_DL_FLASH);
                intentToPlace.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            } else if (tweaks_dl) {
                icon = R.drawable.ic_rom_control_update;
                tickerText = "ROM Control update ready to install."; // ticker-text
                contentTitle = "ROM Control Update";
                contentText = "Tap when you're ready to update!";

                intentToPlace = new Intent(Intent.ACTION_VIEW);
                intentToPlace.setDataAndType(Uri.fromFile(f),
                        "application/vnd.android.package-archive");
                intentToPlace.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentToPlace);
                return;
            }

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentToPlace, 0);

            // the next two lines initialize the Notification, using
            // the configurations above
            Notification notification = new Notification(icon, tickerText, when);
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

            mNotificationManager.notify(1, notification);

        } else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            Log.e(TAG, "Reciving " + DownloadManager.ACTION_NOTIFICATION_CLICKED);

            Log.e(TAG, "click dl id: " + download_id);
            Log.e(TAG, "rom dl id: " + rom_id);
            Log.e(TAG, "tweaks dl: " + tweaks_id);
            if (tweaks_dl)
                return;

            if (download_id == 0 || download_id == -1)
                download_id = rom_id;

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(download_id);
            Cursor cursor = dm.query(query);

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);

                switch (status) {
                    case DownloadManager.STATUS_FAILED:
                        String failedReason = "";
                        dm.remove(download_id);
                        switch (reason) {
                            case DownloadManager.ERROR_CANNOT_RESUME:
                                failedReason = "ERROR_CANNOT_RESUME";
                                break;
                            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                                failedReason = "ERROR_DEVICE_NOT_FOUND";
                                break;
                            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                                failedReason = "ERROR_FILE_ALREADY_EXISTS";
                                break;
                            case DownloadManager.ERROR_FILE_ERROR:
                                failedReason = "ERROR_FILE_ERROR";
                                break;
                            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                                failedReason = "ERROR_HTTP_DATA_ERROR";
                                break;
                            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                                failedReason = "ERROR_INSUFFICIENT_SPACE";
                                break;
                            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                                failedReason = "ERROR_TOO_MANY_REDIRECTS";
                                break;
                            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                                failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                                break;
                            case DownloadManager.ERROR_UNKNOWN:
                                failedReason = "ERROR_UNKNOWN";
                                break;
                        }
                        Log.e(TAG, "Failed: " + failedReason);
                        // Toast.makeText(AndroidDownloadManagerActivity.this,
                        // "FAILED: " + failedReason,
                        // Toast.LENGTH_LONG).show();
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        String pausedReason = "";
                        dm.remove(download_id);
                        switch (reason) {
                            case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                                pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                                break;
                            case DownloadManager.PAUSED_UNKNOWN:
                                pausedReason = "PAUSED_UNKNOWN";
                                break;
                            case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                                pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                                break;
                            case DownloadManager.PAUSED_WAITING_TO_RETRY:
                                pausedReason = "PAUSED_WAITING_TO_RETRY";
                                break;
                        }
                        Log.e(TAG, "Paused: " + pausedReason);
                        // Toast.makeText(AndroidDownloadManagerActivity.this,
                        // "PAUSED: " + pausedReason,
                        // Toast.LENGTH_LONG).show();

                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        // Toast.makeText(AndroidDownloadManagerActivity.this,
                        // "RUNNING",
                        // Toast.LENGTH_LONG).show();
                        // startActivity(new Intent(context, Main.class));
                        Log.e(TAG, "Sent broadcast");
                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putBoolean("consume_old", true).apply();
                        Intent i = new Intent(context, Main.class);
                        i.setAction(Main.BROADCAST_DL_CANCEL);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        context.startActivity(i);

                        // context.sendBroadcast(i);
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:

                        // Toast.makeText(AndroidDownloadManagerActivity.this,
                        // "SUCCESSFUL",
                        // Toast.LENGTH_LONG).show();
                        // GetFile();
                        dm.remove(download_id);

                }
            } else {
                Log.e(TAG, "cursor failed");
            }

        }
    }
}
