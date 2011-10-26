
package com.android.server;

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
}
