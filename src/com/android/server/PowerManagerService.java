
package com.android.server;

import java.io.PrintWriter;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

public class PowerManagerService {

    // flags for setPowerState
    private static final int SCREEN_ON_BIT = 0x00000001;
    private static final int SCREEN_BRIGHT_BIT = 0x00000002;
    private static final int BUTTON_BRIGHT_BIT = 0x00000004;
    private static final int KEYBOARD_BRIGHT_BIT = 0x00000008;
    private static final int BATTERY_LOW_BIT = 0x00000010;
    private int mButtonBrightnessOverride;
    private int mButtonOffTimeoutSetting;
    private int mLightSensorButtonBrightness;
    private boolean mUseSoftwareAutoBrightness = false;
    public boolean mAnimateScreenLights;
    private Context mContext;

    private Handler mScreenOffHandler = new Handler();
    private final LockList mLocks = new LockList();
    private final BrightnessState mScreenBrightness = new BrightnessState(SCREEN_BRIGHT_BIT);
    public int mAnimationSetting;
    private int mScreenOffReason;

    private native void nativeStartSurfaceFlingerAnimation(int mode);

    private class LockList {

    }

    /**
     * Brightness value for fully off
     */
    public static final int BRIGHTNESS_OFF = 0;

    /**
     * Brightness value for dim backlight
     */
    public static final int BRIGHTNESS_DIM = 20;

    /**
     * Brightness value for fully on
     */
    public static final int BRIGHTNESS_ON = 255;

    private int applyButtonState(int state) {
        int brightness = -1;
        boolean disableWhileScreenOn = true;

        // 1
        if ((state & BATTERY_LOW_BIT) != 0) {
            // do not override brightness if the battery is low
            return state;
        }

        // 0 = always off
        // -1 = always on
        if (mButtonBrightnessOverride >= 0) {
            brightness = mButtonBrightnessOverride;
        } else if (mLightSensorButtonBrightness >= 0 && mButtonOffTimeoutSetting == -2) {
            brightness = mLightSensorButtonBrightness;
        } else if (mButtonOffTimeoutSetting != -3 && mButtonOffTimeoutSetting == -1) {
            brightness = 0xFF;
        } else if (mButtonOffTimeoutSetting == 0) {
            brightness = 0;
        }

        if (brightness > 0 && !disableWhileScreenOn) {
            return state | BUTTON_BRIGHT_BIT;
        } else if (brightness == 0) {
            return state & ~BUTTON_BRIGHT_BIT;
        } else {
            return state;
        }
    }

    class BrightnessState implements Runnable {
        final int mask;

        boolean initialized;
        int targetValue;
        float curValue;
        float delta;
        boolean animating;

        BrightnessState(int m) {
            mask = m;
        }

        public void dump(PrintWriter pw, String prefix) {

        }

        void setTargetLocked(int target, int stepsToTarget, int initialValue,
                int nominalCurrentValue) {

        }

        boolean stepLocked() {
            return false;
        }

        void jumpToTargetLocked() {

        }

        private void finishAnimationLocked(boolean more, int curIntValue) {

        }

        public void run() {
            final int ANIM_SETTING_ON = 0x1;
            final int ANIM_SETTING_OFF = 0x10;
            boolean screenOnAni = false;
            boolean screenOffAni = false;

            int mode = 0;

            if (Settings.System.getInt(mContext.getContentResolver(), "tweaks_crt_on", 1) == 1) {
                mode |= ANIM_SETTING_ON;
                screenOnAni = true;
            }

            if (Settings.System.getInt(mContext.getContentResolver(), "tweaks_crt_off", 1) == 1) {
                mode |= ANIM_SETTING_OFF;
                screenOffAni = true;
            }

            if (curValue == BRIGHTNESS_OFF) {
                Log.e("ROMAN", "screen is off, turning on");
                if (screenOnAni) {
                    Log.e("ROMAN", "screenOnAni == true and mode = " + mode);
                }
            }

            // Check for the electron beam for fully on/off transitions.
            // Otherwise, allow it to fade the brightness as normal.
            final boolean animate = animating &&
                    ((screenOffAni && targetValue == BRIGHTNESS_OFF) ||
                    (screenOnAni && (int) curValue == BRIGHTNESS_OFF));
            if (mAnimateScreenLights || !animate) {
                Log.e("ROMAN", "Not going to animate!");
                synchronized (mLocks) {
                    long now = SystemClock.uptimeMillis();
                    boolean more = mScreenBrightness.stepLocked();
                    if (more) {
                        mScreenOffHandler.postAtTime(this, now + (1000 / 60));
                    }
                }
            } else {
                if (curValue == BRIGHTNESS_OFF) {
                    Log.e("ROMAN", "should hit nativeStartAnimation() next");
                }
                synchronized (mLocks) {
                    if (animate) {
                        if (curValue == BRIGHTNESS_OFF) {
                            Log.e("ROMAN", "about to hit nativeStartAnmation(), mScreenOffReason = " + mScreenOffReason);
                        }
                        // It's pretty scary to hold mLocks for this long, and
                        // we should
                        // redesign this, but it works for now.
                        nativeStartSurfaceFlingerAnimation(mScreenOffReason == 0x4
                                ? 0 : mode);
                    }
                    mScreenBrightness.jumpToTargetLocked();
                }
            }
        }
    }
}
