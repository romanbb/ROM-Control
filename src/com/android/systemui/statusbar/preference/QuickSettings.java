
package com.android.systemui.statusbar.preference;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.systemui.statusbar.StatusBarFlag;
import com.android.systemui.statusbar.StatusBarService;
import com.android.systemui.statusbar.StatusBarStyleable;

public class QuickSettings extends LinearLayout {

    private final int ITEM_NUMBER = 20;

    int[] ids;

    private static final int ROTATION = 0;

    private static final int WIFI = 1;
    private static final int BT = 2;
    private static final int MOBILE_NETWORK = 3;
    private static final int BRIGHTNESS = 5;
    private static final int HOTSPOT = 5;
    private static final int SETTINGS = 6;
    private static final int GPS = 7;
    // private static final int WIMAX = 8;
    private static final int TASK_MANAGER = 9;
    private static final int VOLUME_MEDIA = 10;
    private static final int CARRIER_APP = 11;
    private static final int AIRPLANE_MODE = 12;
    private static final int POWER_EFFICIENCY = 13;
    private static final int AUTO_SYNC = 14;
    private static final int VOLUME_RINGER = 16;
    private static final int TORCH = 17;
    private static final int VIBRATION_MODE = 18;
    private static final int SILENT_MODE = 19;

    private static final String TAG = "QuickSettings";

    private boolean[] bSettingItems;
    private Context mContext;
    private ScrollView mScrollView;
    private StatusBarPreference[] mSettingItems;
    private QuickSettingsConfig mSettingsConfig;
    private boolean mStart;
    private StatusBarStyleable mStyleable;
    private TextView mTitleBar;
    private LinearLayout mScrollList;

    private static final String SETTINGS_PREF = "tweaks_widgets_hidden";
    private ArrayList<Integer> whichToHide;

    private Handler mHandler = new Handler();

    private int TITLE_BAR_ID = 0x7f0a0016;
    private int SCROLL_ID = 0x7f0a0017;
    private int SCROLL_LIST_ID = 0x7f0a0018;
    private int CONFIG_ID = 0x7f0a0019;

    private int STATUS_BAR_TITLEBAR_2ROUND_REST_ID = 0x0f0201af;
    private int MTITLEBAR_TEXT_ID = 0x7f080014;

    public QuickSettings(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        bSettingItems = new boolean[ITEM_NUMBER];
        mSettingItems = new StatusBarPreference[ITEM_NUMBER];

        mStyleable = null;
        mStart = false;

        if (StatusBarFlag.HTC_SKIN)
            mStyleable = new StatusBarStyleable(context);
        init();

        ids = new int[ITEM_NUMBER];

        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
    }

    private void init() {
        return;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleBar = (TextView) findViewById(TITLE_BAR_ID);
        if (StatusBarFlag.HTC_CONFIG_FEATURE)
            mTitleBar.setVisibility(View.GONE);

        mScrollView = (ScrollView) findViewById(SCROLL_ID);
        mScrollList = (LinearLayout) findViewById(SCROLL_LIST_ID);

        if (StatusBarFlag.HTC_CONFIG_FEATURE) {
            mSettingsConfig = (QuickSettingsConfig) ((ViewStub) findViewById(CONFIG_ID))
                    .inflate();
        } else {
            mScrollList.removeView(findViewById(CONFIG_ID));
        }

        for (int i = 0; i < ITEM_NUMBER; i++) {
            bSettingItems[i] = true;
            mSettingItems[i] = null;
            ids[i] = 0;
        }

        ids[ROTATION] = 0x7f0a001b;
        ids[WIFI] = 0x7f0a001c;
        ids[BT] = 0x7f0a0021;
        ids[MOBILE_NETWORK] = 0x7f0a001d;
        ids[BRIGHTNESS] = 0x7f0a001a;
        ids[HOTSPOT] = 0x7f0a001e;
        ids[SETTINGS] = 0x7f0a0025;
        ids[GPS] = 0x7f0a0022;
        ids[TASK_MANAGER] = 0x7f0a0026;
        ids[VOLUME_MEDIA] = 0x7f0a0028;
        ids[CARRIER_APP] = 0x7f0a0027;
        ids[AIRPLANE_MODE] = 0x7f0a0023;
        ids[POWER_EFFICIENCY] = 0x7f0a0024;
        ids[AUTO_SYNC] = 0x7f0a001f;
        ids[VOLUME_RINGER] = 0x7f0a0072;
        ids[TORCH] = 0x7f0a0075;
        ids[VIBRATION_MODE] = 0x7f0a0071;
        ids[SILENT_MODE] = 0x7f0a0076;

        mSettingItems[ROTATION] = new Rotation(mContext, ((ViewStub) findViewById(ids[ROTATION])).inflate());
        mSettingItems[WIFI] = new Wifi(mContext, ((ViewStub) findViewById(ids[WIFI])).inflate());
        mSettingItems[BT] = new BT(mContext, ((ViewStub) findViewById(ids[BT])).inflate());
        mSettingItems[MOBILE_NETWORK] = new MobileNetwork(mContext, ((ViewStub) findViewById(ids[MOBILE_NETWORK])).inflate());
        mSettingItems[BRIGHTNESS] = new Brightness(mContext, ((ViewStub) findViewById(ids[BRIGHTNESS])).inflate());
        // mSettingItems[HOTSPOT] = new HotSpot(mContext, ((ViewStub)
        // findViewById(ids[HOTSPOT])).inflate());
        mSettingItems[SETTINGS] = new Settings(mContext, ((ViewStub) findViewById(ids[SETTINGS])).inflate());
        mSettingItems[GPS] = new GPS(mContext, ((ViewStub) findViewById(ids[GPS])).inflate());
        mSettingItems[TASK_MANAGER] = new TaskManager(mContext, ((ViewStub) findViewById(ids[TASK_MANAGER])).inflate());
        mSettingItems[VOLUME_MEDIA] = new Volume(mContext, ((ViewStub) findViewById(ids[VOLUME_MEDIA])).inflate());
        // mSettingItems[CARRIER_APP] = new CarrierApp(mContext, ((ViewStub)
        // findViewById(CARRIER_APP_ID)).inflate());
        mSettingItems[AIRPLANE_MODE] = new AirplaneMode(mContext, ((ViewStub) findViewById(ids[AIRPLANE_MODE])).inflate());

        mSettingItems[POWER_EFFICIENCY] = new PowerEfficiency(mContext, ((ViewStub) findViewById(ids[POWER_EFFICIENCY])).inflate());
        mSettingItems[AUTO_SYNC] = new AutoSync(mContext, ((ViewStub) findViewById(ids[AUTO_SYNC])).inflate());
        mSettingItems[VOLUME_RINGER] = new VolumeRinger(mContext, ((ViewStub) findViewById(ids[VOLUME_RINGER])).inflate());
        mSettingItems[TORCH] = new Torch(mContext, ((ViewStub) findViewById(ids[TORCH])).inflate());
        mSettingItems[VIBRATION_MODE] = new VibrationMode(mContext, ((ViewStub) findViewById(ids[VIBRATION_MODE])).inflate());
        mSettingItems[SILENT_MODE] = new SilentMode(mContext, ((ViewStub) findViewById(ids[SILENT_MODE])).inflate());

        // ConnectivityManager cm = ((ConnectivityManager)
        // mContext.getSystemService("connectivity"));

        // if (cm.getTetherableWifiRegexs().length == 0 &&
        // !cm.isTetheringSupported())
        // mScrollList.removeView(findViewById(HOTSPOT_ID));
        // mSettingItems[HOTSPOT].setEnable(false);

        updateResources();
        updateVisibility();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void onStart() {
        if (mSettingsConfig != null)
            mSettingsConfig.onStart();
        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i] != null)
                mSettingItems[i].onStart();

        mStart = true;
    }

    public void onStop() {
        if (mSettingsConfig != null)
            mSettingsConfig.onStop();
        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i] != null)
                mSettingItems[i].onStop();

        mStart = false;
    }

    public void scrollToZero() {
        if (mScrollView != null) {
            mScrollView.scrollTo(0, 0);
        }
    }

    public void setService(StatusBarService service) {
        if (mSettingsConfig != null)
            mSettingsConfig.setService(service);

        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i] != null)
                mSettingItems[i].setService(service);

    }

    public void updateResources() {

        if (mTitleBar.getVisibility() == View.VISIBLE) {

            if (mStyleable != null) {
                mStyleable.updateStyleName();

                int tempPaddingLeft = mTitleBar.getPaddingLeft();
                int tempPaddingTop = mTitleBar.getPaddingTop();
                int tempPaddingRight = mTitleBar.getPaddingRight();
                int tempPaddingBottom = mTitleBar.getPaddingBottom();

                mTitleBar.setBackgroundDrawable(mStyleable.getStyleableDrawable(
                        "status_bar_titlebar_2round_rest", STATUS_BAR_TITLEBAR_2ROUND_REST_ID));

                mTitleBar.setTextSize(0, getResources().getDimension(0x2050010));
                mTitleBar.setPadding(tempPaddingLeft, tempPaddingTop, tempPaddingRight,
                        tempPaddingBottom);
            }

            mTitleBar.setText(MTITLEBAR_TEXT_ID);
        }

        if (mSettingsConfig != null)
            mSettingsConfig.updateResources();

        for (int i = 0; i < ITEM_NUMBER; i++)
            if (mSettingItems[i] != null)
                mSettingItems[i].updateResources();
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

        // for (int i = 0; i < ITEM_NUMBER; i++)
        // if (whichToHide.contains(i))
        // mScrollList.removeView(findViewById(ids[i]));

        for (int i = 0; i < ITEM_NUMBER; i++) {

            if (mSettingItems[i] != null)
                if (whichToHide.contains(i))
                    mSettingItems[i].setEnable(false);
                else
                    mSettingItems[i].setEnable(true);

        }
        // mScrollList.removeView(view)
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
