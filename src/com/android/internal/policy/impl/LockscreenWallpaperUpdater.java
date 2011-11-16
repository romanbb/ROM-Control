/*****************************************************************************************************
/
/
/  Lockscreen Wallpaper Updater:
/    -Displays a user selected wallpaper (via TSMParts) on Lockscreen
/
/  This was modeled from the SGS2 Wallpaper Updater
/
/  Converted/Written By: Scott Brissenden
 *******************************************************************************************************/

package com.android.internal.policy.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

class LockscreenWallpaperUpdater extends FrameLayout {

    private final String TAG = "LockscreenWallpaperUpdater";

    private final String WALLPAPER_IMAGE_PATH = "/data/data/com.cooliris.media/files/zzzzzz_lockscreen_wallpaper.jpg";

    private ImageView mLockScreenWallpaperImage;

    // BitmapDrawable wallpaperDrawable;

    Bitmap bitmap = null;

    public LockscreenWallpaperUpdater(Context context) {
        super(context);

        mLockScreenWallpaperImage = new ImageView(getContext());
        mLockScreenWallpaperImage.setScaleType(ScaleType.FIT_XY);

        addView(mLockScreenWallpaperImage, -1, -1);

        setLockScreenWallpaper();
    }

    public void setLockScreenWallpaper() {
        File file = new File(WALLPAPER_IMAGE_PATH);

        if (file.exists()) {
            Bitmap lockb = BitmapFactory.decodeFile(WALLPAPER_IMAGE_PATH);
            mLockScreenWallpaperImage.setImageDrawable(new BitmapDrawable(lockb));
        } else {
            mLockScreenWallpaperImage.setImageResource(0x1080439);
        }
    }

    public void setLockScreenBlack() {
        mLockScreenWallpaperImage.setImageResource(0x106000);
    }

    public void onResume() {

    }

    public void onPause() {

        System.gc();
    }

    public void cleanUp() {
        // removeAllViews();
        // mLockScreenWallpaperImage = null;

        System.gc();
    }

}
