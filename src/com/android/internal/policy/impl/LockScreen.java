/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.policy.impl;

import com.android.internal.telephony.IccCard;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.SlidingTab;
import com.android.internal.widget.RotarySelector;
import com.android.internal.widget.RotarySelector.OnDialTriggerListener;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.ColorStateList;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.*;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import com.android.internal.policy.impl.LockscreenWallpaperUpdater;
import android.content.ContentResolver;
import android.media.AudioManager;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.android.internal.policy.impl.LockscreenInfo;
import com.android.internal.policy.impl.MusicWidget;

import java.util.Date;
import java.io.File;
import java.net.URISyntaxException;

/**
 * The screen within {@link LockPatternKeyguardView} that shows general
 * information about the device depending on its state, and how to get past it,
 * as applicable.
 */
class LockScreen extends LinearLayout implements KeyguardScreen, KeyguardUpdateMonitor.InfoCallback,
        KeyguardUpdateMonitor.SimStateCallback, SlidingTab.OnTriggerListener, RotarySelector.OnDialTriggerListener {

    private static final boolean DBG = false;
    private static final String TAG = "LockScreen";
    private static final String ENABLE_MENU_KEY_FILE = "/data/local/enable_menu_key";

    private Status mStatus = Status.Normal;

    private LockPatternUtils mLockPatternUtils;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private KeyguardScreenCallback mCallback;

    private TextView mCarrier;
    private TextView mCustomMsg;
    private RotarySelector mRotarySelector;
    private DigitalClock mClock;
    private TextView mAmPm;
    private TextView mTime;
    private TextView mDate;
    private TextView mStatus1;
    private TextView mStatus2;
    private TextView mScreenLocked;
    private TextView mEmergencyCallText;
    private Button mEmergencyCallButton;
    private LockscreenWallpaperUpdater mLockscreenWallpaperUpdater;
    private RelativeLayout mMainLayout;
    private LinearLayout mBoxLayout;
    private LockscreenInfo mLockscreenInfo;
    private LinearLayout mMusicLayoutBottom;
    private LinearLayout mMusicLayoutTop;
    private MusicWidget mMusicWidget;

    private ImageButton mPlayIcon;
    private ImageButton mPauseIcon;
    private ImageButton mRewindIcon;
    private ImageButton mForwardIcon;
    private AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    private boolean mWasMusicActive = am.isMusicActive();
    private boolean mIsMusicActive = false;

    // current configuration state of keyboard and display
    private int mKeyboardHidden;
    private int mCreationOrientation;

    // are we showing battery information?
    private boolean mShowingBatteryInfo = false;

    // last known plugged in state
    private boolean mPluggedIn = false;

    // last known battery level
    private int mBatteryLevel = 100;

    private String mNextAlarm = null;
    private Drawable mAlarmIcon = null;
    private String mCharging = null;
    private Drawable mChargingIcon = null;

    private boolean mSilentMode;
    private AudioManager mAudioManager;
    private String mDateFormatString;
    private java.text.DateFormat mTimeFormat;
    private boolean mEnableMenuKeyInLockScreen;

    private Context mContext;

    private boolean mShowingInfo = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_show_info", 0) == 1);

    private boolean mLockAlwaysBattery = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_battery_info", 0) == 1);

    private boolean mLockMusicControls = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_music_controls", 0) == 1);

    private boolean mLockAlwaysMusic = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_always_music_controls", 0) == 1);

    private int mLockscreenStyle = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_type_key", 3));

    private boolean mRotaryUnlockDown = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_rotary_unlock_down", 0) == 1);

    private boolean mRotaryHideArrows = true;

    private String mCustomAppActivity = (Settings.System.getString(mContext.getContentResolver(),
            "lockscreen_custom_app_activity"));

    private int mClockAlign = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_clock_align", 0));

    private int mSgsMusicLoc = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_sgsmusic_controls_loc", 1));

    private boolean mSgsMusicControls = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_sgsmusic_controls", 1) == 1);

    private boolean mAlwaysSgsMusicControls = (Settings.System.getInt(mContext.getContentResolver(),
            "lockscreen_always_sgsmusic_controls", 0) == 1);

    private boolean mUseRotaryLockscreen = (mLockscreenStyle == 1);

    private boolean mUseRotaryRevLockscreen = (mLockscreenStyle == 5);

    private boolean mUseLenseSquareLockscreen = (mLockscreenStyle == 6);
    private boolean mLensePortrait = false;

    private String mCustomAppName;

    private static final int R_layout_keyguard_screen_rotary_unlock = 0x109008e;
    private static final int R_layout_keyguard_screen_rotary_unlock_land = 0x1090092;
    private static final int R_id_carrier = 0x10201fa;
    private static final int R_id_wallpaper_panel = 0x10202d5;
    private static final int R_id_customMsg = 0x10202d6;
    private static final int R_id_time = 0x102005d;
    private static final int R_id_timeDisplay = 0x1020209;
    private static final int R_id_am_pm = 0x102020a;
    private static final int R_id_date = 0x102005c;
    private static final int R_id_status1 = 0x102020b;
    private static final int R_id_status2 = 0x102020c;
    private static final int R_id_musicControlPlay = 0x10202d7;
    private static final int R_id_musicControlPause = 0x10202d8;
    private static final int R_id_musicControlPrevious = 0x10202da;
    private static final int R_id_musicControlNext = 0x10202d9;
    private static final int R_id_screenLocked = 0x102020d;
    private static final int R_id_rotary_selector = 0x10202db;
    private static final int R_id_lock_box = 0x10202dc;
    private static final int R_id_musicwidget_bottom = 0x10202dd;
    private static final int R_id_musicwidget_top = 0x10202de;
    private static final int R_id_emergencyCallText = 0x1020206;
    private static final int R_id_emergencyCallButton = 0x10201ee;
    private static final int R_string_lockscreen_emergency_call = 0x104032e;
    private static final int R_drawable_ic_jog_dial_unlock = 0x10801b9;
    private static final int R_drawable_ic_jog_dial_tsm = 0x108045c;
    private static final int R_drawable_ic_jog_dial_vibrate_on = 0x10801b9;
    private static final int R_drawable_ic_jog_dial_sound_off = 0x10801b7;
    private static final int R_drawable_ic_jog_dial_sound_on = 0x10801b8;
    private static final int R_string_full_wday_month_day_no_year = 0x10400a9;
    private static final int R_string_global_action_silent_mode_on_status = 0x1040154;
    private static final int R_string_global_action_silent_mode_off_status = 0x1040155;
    private static final int R_drawable_ic_lock_ringer_off = 0x10801be;
    private static final int R_drawable_ic_lock_ringer_on = 0x10801bf;
    private static final int R_color_keyguard_text_color_soundoff = 0x106002f;
    private static final int R_color_keyguard_text_color_soundon = 0x1060030;
    private static final int R_string_lockscreen_charged = 0x1040334;
    private static final int R_string_lockscreen_plugged_in = 0x1040333;
    private static final int R_string_lockscreen_low_battery = 0x1040336;
    private static final int R_drawable_ic_lock_idle_discharging = 0x108001e;
    private static final int R_string_lockscreen_network_locked_message = 0x104033b;
    private static final int R_string_lockscreen_instructions_when_pattern_disabled = 0x104032c;
    private static final int R_string_lockscreen_missing_sim_message_short = 0x1040337;
    private static final int R_string_lockscreen_missing_sim_instructions = 0x104033d;
    private static final int R_string_lockscreen_discharging = 0x1040528;
    private static final int R_string_lockscreen_sim_locked_message = 0x104033e;
    private static final int R_string_lockscreen_sim_puk_locked_message = 0x104033c;
    private static final int R_string_lockscreen_sim_puk_locked_instructions = 0x104033d;
    private static final int R_drawable_ic_lock_idle_alarm = 0x108002e;
    private static final int R_drawable_ic_lock_idle_low_battery = 0x1080020;
    private static final int R_drawableic_lock_idle_charging = 0x108001e;

    /**
     * The status of this lock screen.
     */
    enum Status {
        /**
         * Normal case (sim card present, it's not locked)
         */
        Normal(true),

        /**
         * The sim card is 'network locked'.
         */
        NetworkLocked(true),

        /**
         * The sim card is missing.
         */
        SimMissing(false),

        /**
         * The sim card is missing, and this is the device isn't provisioned, so
         * we don't let them get past the screen.
         */
        SimMissingLocked(false),

        /**
         * The sim card is PUK locked, meaning they've entered the wrong sim
         * unlock code too many times.
         */
        SimPukLocked(false),

        /**
         * The sim card is locked.
         */
        SimLocked(true);

        private final boolean mShowStatusLines;

        Status(boolean mShowStatusLines) {
            this.mShowStatusLines = mShowStatusLines;
        }

        /**
         * @return Whether the status lines (battery level and / or next alarm)
         *         are shown while in this state. Mostly dictated by whether
         *         this is room for them.
         */
        public boolean showStatusLines() {
            return mShowStatusLines;
        }
    }

    /**
     * In general, we enable unlocking the insecure key guard with the menu key.
     * However, there are some cases where we wish to disable it, notably when
     * the menu button placement or technology is prone to false positives.
     * 
     * @return true if the menu key should be enabled
     */
    private boolean shouldEnableMenuKey() {
        final Resources res = getResources();
        final boolean configDisabled = res.getBoolean(0x10d0016); // R.bool.config_disableMenuKeyInLockScreen
        final boolean isMonkey = SystemProperties.getBoolean("ro.monkey", false);
        final boolean fileOverride = (new File(ENABLE_MENU_KEY_FILE)).exists();
        return !configDisabled || isMonkey || fileOverride;
    }

    /**
     * @param context Used to setup the view.
     * @param configuration The current configuration. Used to use when
     *            selecting layout, etc.
     * @param lockPatternUtils Used to know the state of the lock pattern
     *            settings.
     * @param updateMonitor Used to register for updates on various keyguard
     *            related state, and query the initial state at setup.
     * @param callback Used to communicate back to the host keyguard view.
     */

    LockScreen(Context context, Configuration configuration, LockPatternUtils lockPatternUtils,
            KeyguardUpdateMonitor updateMonitor,
            KeyguardScreenCallback callback) {
        super(context);
        mLockPatternUtils = lockPatternUtils;
        mUpdateMonitor = updateMonitor;
        mCallback = callback;
        ContentResolver resolver = mContext.getContentResolver();

        mEnableMenuKeyInLockScreen = shouldEnableMenuKey();

        mCreationOrientation = configuration.orientation;

        mKeyboardHidden = configuration.hardKeyboardHidden;

        if (LockPatternKeyguardView.DEBUG_CONFIGURATION) {
            Log.v(TAG, "***** CREATING LOCK SCREEN", new RuntimeException());
            Log.v(TAG, "Cur orient=" + mCreationOrientation
                    + " res orient=" + context.getResources().getConfiguration().orientation);
        }

        final LayoutInflater inflater = LayoutInflater.from(context);
        if (DBG)
            Log.v(TAG, "Creation orientation = " + mCreationOrientation);
        if (mCreationOrientation != Configuration.ORIENTATION_LANDSCAPE) {
            inflater.inflate(R_layout_keyguard_screen_rotary_unlock, this, true);
        } else {
            inflater.inflate(R_layout_keyguard_screen_rotary_unlock_land, this, true);
        }

        mCarrier = (TextView) findViewById(R_id_carrier);
        // Required for Marquee to work
        mCarrier.setSelected(true);
        mCarrier.setTextColor(0xffffffff);

        mMainLayout = (RelativeLayout) findViewById(R_id_wallpaper_panel);

        mCustomMsg = (TextView) findViewById(R_id_customMsg);
        String r = (Settings.System.getString(resolver, "lockscreen_custom_msg"));
        mCustomMsg.setSelected(true);
        mCustomMsg.setText(r);
        mCustomMsg.setTextColor(0xffffffff);

        mClock = (DigitalClock) findViewById(R_id_time);
        mTime = (TextView) findViewById(R_id_timeDisplay);
        mAmPm = (TextView) findViewById(R_id_am_pm);
        mDate = (TextView) findViewById(R_id_date);
        mStatus1 = (TextView) findViewById(R_id_status1);
        mStatus2 = (TextView) findViewById(R_id_status2);

        if (mClockAlign == 0) {
            mClock.setGravity(Gravity.LEFT);
            mDate.setGravity(Gravity.LEFT);
            mStatus1.setGravity(Gravity.LEFT);
            mStatus2.setGravity(Gravity.LEFT);
        } else if (mClockAlign == 1) {
            mClock.setGravity(Gravity.CENTER_HORIZONTAL);
            mDate.setGravity(Gravity.CENTER_HORIZONTAL);
            mStatus1.setGravity(Gravity.CENTER_HORIZONTAL);
            mStatus2.setGravity(Gravity.CENTER_HORIZONTAL);
        } else if (mClockAlign == 2) {
            mClock.setGravity(Gravity.RIGHT);
            mDate.setGravity(Gravity.RIGHT);
            mStatus1.setGravity(Gravity.RIGHT);
            mStatus2.setGravity(Gravity.RIGHT);
        }

        mPlayIcon = (ImageButton) findViewById(R_id_musicControlPlay);
        mPauseIcon = (ImageButton) findViewById(R_id_musicControlPause);
        mRewindIcon = (ImageButton) findViewById(R_id_musicControlPrevious);
        mForwardIcon = (ImageButton) findViewById(R_id_musicControlNext);

        mScreenLocked = (TextView) findViewById(R_id_screenLocked);
        mRotarySelector = (RotarySelector) findViewById(R_id_rotary_selector);

        mLockscreenWallpaperUpdater = new LockscreenWallpaperUpdater(context);
        mLockscreenWallpaperUpdater.setVisibility(View.VISIBLE);
        mMainLayout.addView(mLockscreenWallpaperUpdater, 0);

        mLockscreenInfo = new LockscreenInfo(context, updateMonitor, configuration, lockPatternUtils);
        mBoxLayout = (LinearLayout) findViewById(R_id_lock_box);

        mMusicWidget = new MusicWidget(context, callback, updateMonitor);
        mMusicLayoutBottom = (LinearLayout) findViewById(R_id_musicwidget_bottom);
        mMusicLayoutTop = (LinearLayout) findViewById(R_id_musicwidget_top);

        if (mSgsMusicLoc == 1 && mSgsMusicControls) {
            mMusicWidget.setTopLayout();
            mMusicLayoutTop.addView(mMusicWidget);
        } else if (mSgsMusicLoc == 2 && mSgsMusicControls) {
            mMusicWidget.setBottomLayout();
            mMusicLayoutBottom.addView(mMusicWidget);
        }

        if (mShowingInfo) {
            mBoxLayout.addView(mLockscreenInfo);
        }

        mEmergencyCallText = (TextView) findViewById(R_id_emergencyCallText);
        mEmergencyCallButton = (Button) findViewById(R_id_emergencyCallButton);
        mEmergencyCallButton.setText(R_string_lockscreen_emergency_call);

        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
        mEmergencyCallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.takeEmergencyCallAction();
            }
        });

        mPlayIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.pokeWakelock();
                refreshMusicStatus();
                if (!am.isMusicActive()) {
                    mPauseIcon.setVisibility(View.VISIBLE);
                    mPlayIcon.setVisibility(View.GONE);
                    mRewindIcon.setVisibility(View.VISIBLE);
                    mForwardIcon.setVisibility(View.VISIBLE);
                    sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                }
            }
        });

        mPauseIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.pokeWakelock();
                refreshMusicStatus();
                if (am.isMusicActive()) {
                    mPlayIcon.setVisibility(View.VISIBLE);
                    mPauseIcon.setVisibility(View.GONE);
                    mRewindIcon.setVisibility(View.GONE);
                    mForwardIcon.setVisibility(View.GONE);
                    sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                }
            }
        });

        mRewindIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.pokeWakelock();
                sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            }
        });

        mForwardIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.pokeWakelock();
                sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
            }
        });

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        mUpdateMonitor.registerInfoCallback(this);
        mUpdateMonitor.registerSimStateCallback(this);

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mSilentMode = isSilentMode();

        mRotarySelector.setLeftHandleResource(R_drawable_ic_jog_dial_unlock);

        updateRightTabResources();

        // Rotary setup
        if (!mRotaryUnlockDown) {
            mRotarySelector.setLeftHandleResource(R_drawable_ic_jog_dial_unlock);
            mRotarySelector.setMidHandleResource(R_drawable_ic_jog_dial_tsm);
        } else {
            mRotarySelector.setLeftHandleResource(R_drawable_ic_jog_dial_tsm);
            mRotarySelector.setMidHandleResource(R_drawable_ic_jog_dial_unlock);
        }
        mRotarySelector.enableCustomAppDimple(mUseRotaryRevLockscreen);
        mRotarySelector.setRevamped(mUseRotaryRevLockscreen);
        mRotarySelector.setLenseSquare(mUseRotaryRevLockscreen);
        if (mRotaryHideArrows)
            mRotarySelector.hideArrows(true);

        if (!mUseRotaryRevLockscreen)
            mRotaryUnlockDown = false;

        // hide most items when we are in potrait lense mode
        mLensePortrait = (mUseLenseSquareLockscreen && mCreationOrientation != Configuration.ORIENTATION_LANDSCAPE);
        if (mLensePortrait) {
            setLenseWidgetsVisibility(View.INVISIBLE);
        }

        mRotarySelector.setOnDialTriggerListener(this);

        resetStatusInfo(updateMonitor);
    }

    private boolean isSilentMode() {
        return mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
    }

    private void updateRightTabResources() {
        boolean vibe = mSilentMode
                && (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);

        mRotarySelector
                .setRightHandleResource(mSilentMode ? (vibe ? R_drawable_ic_jog_dial_vibrate_on
                        : R_drawable_ic_jog_dial_sound_off) : R_drawable_ic_jog_dial_sound_on);
    }

    private void resetStatusInfo(KeyguardUpdateMonitor updateMonitor) {
        mShowingBatteryInfo = updateMonitor.shouldShowBatteryInfo();
        mPluggedIn = updateMonitor.isDevicePluggedIn();
        mBatteryLevel = updateMonitor.getBatteryLevel();

        mStatus = getCurrentStatus(updateMonitor.getSimState());
        updateLayout(mStatus);

        refreshBatteryStringAndIcon();
        refreshAlarmDisplay();
        refreshMusicStatus();

        mTimeFormat = DateFormat.getTimeFormat(getContext());
        mDateFormatString = getContext().getString(R_string_full_wday_month_day_no_year);
        refreshTimeAndDateDisplay();
        updateStatusLines();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && mEnableMenuKeyInLockScreen) {
            mCallback.goToUnlockScreen();
        }
        return false;
    }

    /** {@inheritDoc} */
    public void onTrigger(View v, int whichHandle) {
        if (whichHandle == SlidingTab.OnTriggerListener.LEFT_HANDLE) {
            mCallback.goToUnlockScreen();
        } else if (whichHandle == SlidingTab.OnTriggerListener.RIGHT_HANDLE) {
            // toggle silent mode
            mSilentMode = !mSilentMode;
            if (mSilentMode) {
                final boolean vibe = (Settings.System.getInt(
                        getContext().getContentResolver(),
                        "vibrate_in_silent", 1) == 1);

                mAudioManager.setRingerMode(vibe
                        ? AudioManager.RINGER_MODE_VIBRATE
                        : AudioManager.RINGER_MODE_SILENT);
            } else {
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }

            updateRightTabResources();

            String message = mSilentMode ?
                    getContext().getString(R_string_global_action_silent_mode_on_status) :
                    getContext().getString(R_string_global_action_silent_mode_off_status);

            final int toastIcon = mSilentMode
                    ? R_drawable_ic_lock_ringer_off
                    : R_drawable_ic_lock_ringer_on;

            final int toastColor = mSilentMode
                    ? getContext().getResources().getColor(R_color_keyguard_text_color_soundoff)
                    : getContext().getResources().getColor(R_color_keyguard_text_color_soundon);
            toastMessage(mScreenLocked, message, toastColor, toastIcon);
            mCallback.pokeWakelock();
        }
    }

    /** {@inheritDoc} */
    public void onDialTrigger(View v, int whichHandle) {
        boolean mUnlockTrigger = false;
        boolean mCustomAppTrigger = false;

        if (whichHandle == RotarySelector.OnDialTriggerListener.LEFT_HANDLE) {
            if (mRotaryUnlockDown)
                mCustomAppTrigger = true;
            else
                mUnlockTrigger = true;
        }
        if (whichHandle == RotarySelector.OnDialTriggerListener.MID_HANDLE) {
            if (mRotaryUnlockDown)
                mUnlockTrigger = true;
            else
                mCustomAppTrigger = true;
        }

        if (mUnlockTrigger) {
            mCallback.goToUnlockScreen();
        } else if (mCustomAppTrigger) {
            if (mCustomAppActivity != null) {
                try {
                    Intent i = Intent.parseUri(mCustomAppActivity, 0);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mContext.startActivity(i);
                    mCallback.goToUnlockScreen();
                } catch (URISyntaxException e) {
                } catch (ActivityNotFoundException e) {
                }
            }
        } else if (whichHandle == RotarySelector.OnDialTriggerListener.RIGHT_HANDLE) {
            // toggle silent mode
            toggleSilentMode();
            updateRightTabResources();

            String message = mSilentMode ? getContext().getString(
                    R_string_global_action_silent_mode_on_status) : getContext().getString(
                    R_string_global_action_silent_mode_off_status);

            final int toastIcon = mSilentMode ? R_drawable_ic_lock_ringer_off
                    : R_drawable_ic_lock_ringer_on;
            final int toastColor = mSilentMode ? getContext().getResources().getColor(
                    R_color_keyguard_text_color_soundoff) : getContext().getResources().getColor(
                    R_color_keyguard_text_color_soundon);
            toastMessage(mScreenLocked, message, toastColor, toastIcon);
            mCallback.pokeWakelock();
        }
    }

    /** {@inheritDoc} */
    public void onGrabbedStateChange(View v, int grabbedState) {
        if (grabbedState == SlidingTab.OnTriggerListener.RIGHT_HANDLE) {
            mSilentMode = isSilentMode();
        }
        // Don't poke the wake lock when returning to a state where the handle
        // is
        // not grabbed since that can happen when the system (instead of the
        // user)
        // cancels the grab.
        if (grabbedState != SlidingTab.OnTriggerListener.NO_HANDLE) {
            mCallback.pokeWakelock();
        } else {
            if (mSgsMusicControls) {
                if (am.isMusicActive() || mAlwaysSgsMusicControls)
                    mMusicWidget.setVisibility(View.VISIBLE);
                mMusicWidget.setControllerVisibility(true, mMusicWidget.isControllerShowing());
            } else if (!am.isMusicActive()) {
                mMusicWidget.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Displays a message in a text view and then restores the previous text.
     * 
     * @param textView The text view.
     * @param text The text.
     * @param color The color to apply to the text, or 0 if the existing color
     *            should be used.
     * @param iconResourceId The left hand icon.
     */
    private void toastMessage(final TextView textView, final String text, final int color, final int iconResourceId) {
        if (mPendingR1 != null) {
            textView.removeCallbacks(mPendingR1);
            mPendingR1 = null;
        }
        if (mPendingR2 != null) {
            mPendingR2.run(); // fire immediately, restoring non-toasted
                              // appearance
            textView.removeCallbacks(mPendingR2);
            mPendingR2 = null;
        }

        final String oldText = textView.getText().toString();
        final ColorStateList oldColors = textView.getTextColors();

        mPendingR1 = new Runnable() {
            public void run() {
                textView.setText(text);
                if (color != 0) {
                    textView.setTextColor(color);
                }
                textView.setCompoundDrawablesWithIntrinsicBounds(iconResourceId, 0, 0, 0);
            }
        };

        textView.postDelayed(mPendingR1, 0);
        mPendingR2 = new Runnable() {
            public void run() {
                textView.setText(oldText);
                textView.setTextColor(oldColors);
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        };
        textView.postDelayed(mPendingR2, 3500);
    }

    private Runnable mPendingR1;
    private Runnable mPendingR2;

    private void refreshAlarmDisplay() {
        mNextAlarm = mLockPatternUtils.getNextAlarm();
        if (mNextAlarm != null && !mShowingInfo) {
            mAlarmIcon = getContext().getResources().getDrawable(R_drawable_ic_lock_idle_alarm);
        }
        updateStatusLines();
    }

    /** {@inheritDoc} */
    public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn,
            int batteryLevel) {
        if (DBG)
            Log.d(TAG, "onRefreshBatteryInfo(" + showBatteryInfo + ", " + pluggedIn + ")");
        mShowingBatteryInfo = showBatteryInfo;
        mPluggedIn = pluggedIn;
        mBatteryLevel = batteryLevel;

        refreshBatteryStringAndIcon();
        updateStatusLines();
        mLockscreenInfo.onResume();
    }

    private void refreshBatteryStringAndIcon() {
        if (!mShowingBatteryInfo && !mLockAlwaysBattery || mShowingInfo) {
            mCharging = null;
            return;
        }

        if (mPluggedIn) {
            mChargingIcon =
                    getContext().getResources().getDrawable(R_drawableic_lock_idle_charging);
            if (mUpdateMonitor.isDeviceCharged()) {
                mCharging = getContext().getString(R_string_lockscreen_charged);
            } else {
                mCharging = getContext().getString(R_string_lockscreen_plugged_in, mBatteryLevel);
            }
        } else {
            if (mBatteryLevel <= 20) {
                mChargingIcon =
                        getContext().getResources().getDrawable(R_drawable_ic_lock_idle_low_battery);
                mCharging = getContext().getString(R_string_lockscreen_low_battery, mBatteryLevel);
            } else {
                mChargingIcon =
                        getContext().getResources().getDrawable(R_drawable_ic_lock_idle_discharging);
                mCharging = getContext().getString(R_string_lockscreen_discharging, mBatteryLevel);
            }
        }
    }

    private void refreshMusicStatus() {
        if ((mWasMusicActive || mIsMusicActive || mLockAlwaysMusic
                || (mAudioManager.isWiredHeadsetOn())
                || (mAudioManager.isBluetoothA2dpOn())) && (mLockMusicControls)) {
            if (am.isMusicActive()) {
                mPauseIcon.setVisibility(View.VISIBLE);
                mPlayIcon.setVisibility(View.GONE);
                mRewindIcon.setVisibility(View.VISIBLE);
                mForwardIcon.setVisibility(View.VISIBLE);
            } else {
                mPlayIcon.setVisibility(View.VISIBLE);
                mPauseIcon.setVisibility(View.GONE);
                mRewindIcon.setVisibility(View.GONE);
                mForwardIcon.setVisibility(View.GONE);
            }
        } else {
            mPlayIcon.setVisibility(View.GONE);
            mPauseIcon.setVisibility(View.GONE);
            mRewindIcon.setVisibility(View.GONE);
            mForwardIcon.setVisibility(View.GONE);
        }
    }

    private void sendMediaButtonEvent(int code) {
        long eventtime = SystemClock.uptimeMillis();

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        getContext().sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, code, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        getContext().sendOrderedBroadcast(upIntent, null);
    }

    /** {@inheritDoc} */
    public void onTimeChanged() {
        refreshTimeAndDateDisplay();
    }

    private void refreshTimeAndDateDisplay() {
        mDate.setText(DateFormat.format(mDateFormatString, new Date()));
    }

    private void updateStatusLines() {
        if (!mStatus.showStatusLines()
                || (mCharging == null && mNextAlarm == null)) {
            mStatus1.setVisibility(View.INVISIBLE);
            mStatus2.setVisibility(View.INVISIBLE);
        } else if (mCharging != null && mNextAlarm == null) {
            // charging only
            mStatus1.setVisibility(View.VISIBLE);
            mStatus2.setVisibility(View.INVISIBLE);

            mStatus1.setText(mCharging);
            mStatus1.setCompoundDrawablesWithIntrinsicBounds(mChargingIcon, null, null, null);

        } else if (mNextAlarm != null && mCharging == null && !mShowingInfo) {
            // next alarm only
            mStatus1.setVisibility(View.VISIBLE);
            mStatus2.setVisibility(View.INVISIBLE);

            mStatus1.setText(mNextAlarm);
            mStatus1.setCompoundDrawablesWithIntrinsicBounds(mAlarmIcon, null, null, null);
        } else if (mCharging != null && mNextAlarm != null && !mShowingInfo) {
            // both charging and next alarm
            mStatus1.setVisibility(View.VISIBLE);
            mStatus2.setVisibility(View.VISIBLE);

            mStatus1.setText(mCharging);
            mStatus1.setCompoundDrawablesWithIntrinsicBounds(mChargingIcon, null, null, null);
            mStatus2.setText(mNextAlarm);
            mStatus2.setCompoundDrawablesWithIntrinsicBounds(mAlarmIcon, null, null, null);
        }
    }

    /** {@inheritDoc} */
    public void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn) {
        if (DBG)
            Log.d(TAG, "onRefreshCarrierInfo(" + plmn + ", " + spn + ")");
        updateLayout(mStatus);
    }

    /**
     * Determine the current status of the lock screen given the sim state and
     * other stuff.
     */
    private Status getCurrentStatus(IccCard.State simState) {
        boolean missingAndNotProvisioned = (!mUpdateMonitor.isDeviceProvisioned()
                && simState == IccCard.State.ABSENT);
        if (missingAndNotProvisioned) {
            return Status.SimMissingLocked;
        }

        switch (simState) {
            case ABSENT:
                return Status.SimMissing;
            case NETWORK_LOCKED:
                return Status.SimMissingLocked;
            case NOT_READY:
                return Status.SimMissing;
            case PIN_REQUIRED:
                return Status.SimLocked;
            case PUK_REQUIRED:
                return Status.SimPukLocked;
            case READY:
                return Status.Normal;
            case UNKNOWN:
                return Status.SimMissing;
        }
        return Status.SimMissing;
    }

    /**
     * Update the layout to match the current status.
     */
    private void updateLayout(Status status) {
        // The emergency call button no longer appears on this screen.
        if (DBG)
            Log.d(TAG, "updateLayout: status=" + status);
        mLockscreenStyle = (Settings.System.getInt(mContext.getContentResolver(),
                "lockscreen_type_key", 3));

        if (mLockscreenStyle == 5) {
            mRotarySelector.enableCustomAppDimple(true);
        }

        mEmergencyCallButton.setVisibility(View.GONE); // in almost all cases

        switch (status) {
            case Normal:
                // text
                mCarrier.setText(
                        getCarrierString(
                                mUpdateMonitor.getTelephonyPlmn(),
                                mUpdateMonitor.getTelephonyPlmn()));

                // Empty now, but used for sliding tab feedback
                mScreenLocked.setText("");

                // layout
                mScreenLocked.setVisibility(View.VISIBLE);
                mRotarySelector.setVisibility(View.VISIBLE);
                mRotarySelector.setRevamped(mUseRotaryRevLockscreen);
                mRotarySelector.setLenseSquare(mUseLenseSquareLockscreen);
                mEmergencyCallText.setVisibility(View.GONE);
                break;
            case NetworkLocked:
                // The carrier string shows both sim card status (i.e. No Sim
                // Card) and
                // carrier's name and/or "Emergency Calls Only" status
                mCarrier.setText(
                        getCarrierString(
                                mUpdateMonitor.getTelephonyPlmn(),
                                getContext().getText(R_string_lockscreen_network_locked_message)));
                mScreenLocked.setText(R_string_lockscreen_instructions_when_pattern_disabled);

                // layout
                mScreenLocked.setVisibility(View.VISIBLE);
                mRotarySelector.setVisibility(View.VISIBLE);
                mRotarySelector.setRevamped(mUseRotaryRevLockscreen);
                mRotarySelector.setLenseSquare(mUseLenseSquareLockscreen);
                mEmergencyCallText.setVisibility(View.GONE);
                break;
            case SimMissing:
                // text
                mCarrier.setText(R_string_lockscreen_missing_sim_message_short);
                mScreenLocked.setText(R_string_lockscreen_missing_sim_instructions);

                // layout
                mScreenLocked.setVisibility(View.VISIBLE);
                mRotarySelector.setVisibility(View.VISIBLE);
                mRotarySelector.setRevamped(mUseRotaryRevLockscreen);
                mRotarySelector.setLenseSquare(mUseLenseSquareLockscreen);
                mEmergencyCallText.setVisibility(View.VISIBLE);
                // do not need to show the e-call button; user may unlock
                break;
            case SimMissingLocked:
                // text
                mCarrier.setText(
                        getCarrierString(
                                mUpdateMonitor.getTelephonyPlmn(),
                                getContext().getText(R_string_lockscreen_missing_sim_message_short)));
                mScreenLocked.setText(R_string_lockscreen_missing_sim_instructions);

                // layout
                mScreenLocked.setVisibility(View.VISIBLE);
                mRotarySelector.setVisibility(View.GONE);
                mEmergencyCallText.setVisibility(View.VISIBLE);
                mEmergencyCallButton.setVisibility(View.VISIBLE);
                break;
            case SimLocked:
                // text
                mCarrier.setText(
                        getCarrierString(
                                mUpdateMonitor.getTelephonyPlmn(),
                                getContext().getText(R_string_lockscreen_sim_locked_message)));

                // layout
                mScreenLocked.setVisibility(View.INVISIBLE);
                mRotarySelector.setVisibility(View.VISIBLE);
                mRotarySelector.setRevamped(mUseRotaryRevLockscreen);
                mRotarySelector.setLenseSquare(mUseLenseSquareLockscreen);
                mEmergencyCallText.setVisibility(View.GONE);
                break;
            case SimPukLocked:
                // text
                mCarrier.setText(
                        getCarrierString(
                                mUpdateMonitor.getTelephonyPlmn(),
                                getContext().getText(R_string_lockscreen_sim_puk_locked_message)));
                mScreenLocked.setText(R_string_lockscreen_sim_puk_locked_instructions);

                // layout
                mScreenLocked.setVisibility(View.VISIBLE);
                mRotarySelector.setVisibility(View.GONE);
                mEmergencyCallText.setVisibility(View.VISIBLE);
                mEmergencyCallButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    static CharSequence getCarrierString(CharSequence telephonyPlmn, CharSequence telephonySpn) {
        if (telephonyPlmn != null && telephonySpn == null) {
            return telephonyPlmn;
        } else if (telephonyPlmn != null && telephonySpn != null) {
            return telephonyPlmn + "|" + telephonySpn;
        } else if (telephonyPlmn == null && telephonySpn != null) {
            return telephonySpn;
        } else {
            return "";
        }
    }

    public void onSimStateChanged(IccCard.State simState) {
        if (DBG)
            Log.d(TAG, "onSimStateChanged(" + simState + ")");
        mStatus = getCurrentStatus(simState);
        updateLayout(mStatus);
        updateStatusLines();
    }

    void updateConfiguration() {
        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation != mCreationOrientation) {
            mCallback.recreateMe(newConfig);
        } else if (newConfig.hardKeyboardHidden != mKeyboardHidden) {
            mKeyboardHidden = newConfig.hardKeyboardHidden;
            final boolean isKeyboardOpen = mKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            if (mUpdateMonitor.isKeyguardBypassEnabled() && isKeyboardOpen) {
                mCallback.goToUnlockScreen();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LockPatternKeyguardView.DEBUG_CONFIGURATION) {
            Log.v(TAG, "***** LOCK ATTACHED TO WINDOW");
            Log.v(TAG, "Cur orient=" + mCreationOrientation
                    + ", new config=" + getResources().getConfiguration());
        }
        updateConfiguration();
    }

    /** {@inheritDoc} */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (LockPatternKeyguardView.DEBUG_CONFIGURATION) {
            Log.w(TAG, "***** LOCK CONFIG CHANGING", new RuntimeException());
            Log.v(TAG, "Cur orient=" + mCreationOrientation
                    + ", new config=" + newConfig);
        }
        updateConfiguration();
    }

    /** {@inheritDoc} */
    public boolean needsInput() {
        return false;
    }

    /** {@inheritDoc} */
    public void onPause() {
        if (mSgsMusicControls) {
            mMusicWidget.onPause();
        }
    }

    /** {@inheritDoc} */
    public void onResume() {
        resetStatusInfo(mUpdateMonitor);
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
        mLockscreenWallpaperUpdater.onResume();
        mLockscreenInfo.onResume();
        if (mSgsMusicControls) {
            mMusicWidget.onResume();
            if (am.isMusicActive() || mAlwaysSgsMusicControls) {
                mMusicWidget.setVisibility(View.VISIBLE);
                mMusicWidget.setControllerVisibility(true, mMusicWidget.isControllerShowing());
            } else if (!am.isMusicActive()) {
                mMusicWidget.setVisibility(View.GONE);
            }
        }
    }

    /** {@inheritDoc} */
    public void cleanUp() {
        mUpdateMonitor.removeCallback(this); // this must be first
        mLockPatternUtils = null;
        mUpdateMonitor = null;
        mCallback = null;
        mLockscreenWallpaperUpdater.cleanUp();
        mLockscreenInfo.cleanUp();
        mMusicWidget.cleanUp();
    }

    /** {@inheritDoc} */
    public void onRingerModeChanged(int state) {
        boolean silent = AudioManager.RINGER_MODE_NORMAL != state;
        if (silent != mSilentMode) {
            mSilentMode = silent;
            updateRightTabResources();
        }
    }

    public void onPhoneStateChanged(String newState) {
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
    }

    public void toggleSilentMode() {
        mSilentMode = !mSilentMode;
        if (mSilentMode) {
            final boolean vibe = (Settings.System.getInt(getContext().getContentResolver(),
                    "vibrate_in_silent", 1) == 1);

            mAudioManager.setRingerMode(vibe ? AudioManager.RINGER_MODE_VIBRATE
                    : AudioManager.RINGER_MODE_SILENT);
        } else {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

    /*
     * enables or disables visibility of most lockscreen widgets depending on
     * lense status
     */
    private void setLenseWidgetsVisibility(int visibility) {
        mClock.setVisibility(visibility);
        mDate.setVisibility(visibility);
        mTime.setVisibility(visibility);
        mAmPm.setVisibility(visibility);
        mCarrier.setVisibility(visibility);

        if (DateFormat.is24HourFormat(mContext))
            mAmPm.setVisibility(View.INVISIBLE);
    }

}
