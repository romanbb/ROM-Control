
package com.android.systemui.statusbar.preference;

import com.android.systemui.statusbar.StatusBarFlag;
import com.android.systemui.statusbar.StatusBarService;
import com.android.systemui.statusbar.StatusBarStyleable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net1.ConnectivityManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class QuickSettings extends LinearLayout {

    private static final int ROTATION = 0;
    private static final int WIFI = 1;
    private static final int BT = 2;
    private static final int MOBILE_NETWORK = 3;
    private static final int BRIGHTNESS = 4;
    private static final int HOTSPOT = 5;
    private static final int SETTINGS = 6;
    private static final int GPS = 7;
    private static final int VOLUME_RINGTONE = 8;
    private static final int TASK_MANAGER = 9;
    private static final int VOLUME = 10;
    private static final int VIBRATION = 11;

    private static final int ITEM_NUMBER = 12;

    static final boolean HTC_DEBUG = false;
    static final String TAG = "QuickSettings";

    private Context mContext;
    private StatusBarPreference[] mSettingItems;
    private boolean mStart;
    private StatusBarStyleable mStyleable;
    private TextView mTitleBar;

    private static final String SETTINGS_PREF = "tweaks_widgets_hidden";
    private ArrayList<Integer> whichToHide;

    private Handler mHandler = new Handler();

    public QuickSettings(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSettingItems = new StatusBarPreference[ITEM_NUMBER];
        mStyleable = null;
        mStart = false;
        mContext = context;

        if (StatusBarFlag.HTC_SKIN) {
            mStyleable = new StatusBarStyleable(context);
        }

        init();

        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
    }

    private void init() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();

        mSettingItems[ROTATION] = new Rotation(mContext, findViewById(0x7f0a0014));
        mSettingItems[WIFI] = new Wifi(mContext, findViewById(0x7f0a0015));
        mSettingItems[BT] = new BT(mContext, findViewById(0x7f0a0019));
        mSettingItems[MOBILE_NETWORK] = new MobileNetwork(mContext, findViewById(0x7f0a0017));
        mSettingItems[BRIGHTNESS] = new Brightness(mContext, findViewById(0x7f0a0013));
        mSettingItems[HOTSPOT] = new HotSpot(mContext, findViewById(0x7f0a0016));
        mSettingItems[SETTINGS] = new com.android.systemui.statusbar.preference.Settings(mContext,
                findViewById(0x7f0a001b));
        mSettingItems[GPS] = new GPS(mContext, findViewById(0x7f0a001a));
        mSettingItems[VOLUME_RINGTONE] = new VolumeRinger(mContext, findViewById(0x7f0a004f));
        mSettingItems[TASK_MANAGER] = new TaskManager(mContext, findViewById(0x7f0a001c));
        mSettingItems[VOLUME] = new Volume(mContext, findViewById(0x7f0a0012));
        mSettingItems[VIBRATION] = new VibrationMode(mContext, findViewById(0x7f0a004e));

        mTitleBar = (TextView) findViewById(0x7f0a0010);
        mTitleBar.setHeight(0x4c);

        if (!StatusBarFlag.HTC_QS_TASK_MANAGER)
            mSettingItems[TASK_MANAGER].setEnable(false);

        ConnectivityManager cm = ((ConnectivityManager) mContext.getSystemService("connectivity"));
        if (cm.getTetherableWifiRegexs().length == 0 && !cm.isTetheringSupported())
            mSettingItems[HOTSPOT].setEnable(false);

        updateResources();
        updateVisibility();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void onStart() {
        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i].getEnable())
                mSettingItems[i].onStart();

        mStart = true;
    }

    public void onStop() {
        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i].getEnable())
                mSettingItems[i].onStop();

        mStart = false;
    }

    public void setService(StatusBarService service) {
        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i].getEnable())
                mSettingItems[i].mService = service;

    }

    public void updateResources() {
        if (mStyleable != null) {
            mStyleable.updateStyleName();

            int tempPaddingLeft = mTitleBar.getPaddingLeft();
            int tempPaddingTop = mTitleBar.getPaddingTop();
            int tempPaddingRight = mTitleBar.getPaddingRight();
            int tempPaddingBottom = mTitleBar.getPaddingBottom();

            mTitleBar.setBackgroundDrawable(mStyleable.getStyleableDrawable(
                    "status_bar_titlebar_2round_rest", 0x7f020147));

            mTitleBar.setTextSize(0, getResources().getDimension(0x2050010));
            mTitleBar.setPadding(tempPaddingLeft, tempPaddingTop, tempPaddingRight,
                    tempPaddingBottom);
        }

        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i].getEnable())
                mSettingItems[i].updateResources();

        mTitleBar.setText(0x7f08000f);

    }

    public void updateVisibility() {
        whichToHide = new ArrayList<Integer>();

        String vals = android.provider.Settings.System.getString(mContext.getContentResolver(),
                SETTINGS_PREF);
        if (vals == null)
            return;

        StringTokenizer st = new StringTokenizer(vals);

        while (st.hasMoreTokens())
            whichToHide.add(Integer.parseInt(st.nextToken()));

        for (int i = 0; i < ITEM_NUMBER; i++)
            if (whichToHide.contains(i))
                mSettingItems[i].setEnable(false);
            else
                mSettingItems[i].setEnable(true);
    }

    public void updateSettings() {
        updateVisibility();

    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            // android.provider.Settings.System
            resolver.registerContentObserver(
                    android.provider.Settings.System.getUriFor(SETTINGS_PREF),
                    false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }
}
