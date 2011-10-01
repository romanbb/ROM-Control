
package com.android.systemui.statusbar.preference;

import android.content.Context;
import android.net1.ConnectivityManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.systemui.statusbar.StatusBarFlag;
import com.android.systemui.statusbar.StatusBarStyleable;

public class QuickSettingsSense35 extends LinearLayout {

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
    private static final int VOLUME_RINGER = 10;
    private static final int CARRIER_APP = 11;
    private static final int AIRPLANE_MODE = 12;
    private static final int POWER_EFFICIENCY = 13;
    private static final int AUTO_SYNC = 14;
    private static final int VOLUME_MEDIA = 15;
    // private static final int VOLUME_RINGER = 16;
    private static final int TORCH = 17;
    private static final int VIBRATION_MODE = 18;
    private static final int SILENT_MODE = 19;

    private final int ITEM_NUMBER = 20;
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

    private int TITLE_BAR_ID = 0x7f0a0016;
    private int SCROLL_ID = 0x7f0a0017;
    private int SCROLL_LIST_ID = 0x7f0a0018;
    private int CONFIG_ID = 0x7f0a0019;
    private int ROTATION_ID = 0x7f0a001b;
    private int WIFI_ID = 0x7f0a0019;
    private int BT_ID = 0x7f0a0021;
    private int MOBILE_NETWORK_ID = 0x7f0a001d;
    private int BRIGHTNESS_ID = 0x7f0a001a;
    private int HOTSPOT_ID = 0x7f0a001e;
    private int SETTINGS_ID = 0x7f0a0025;
    private int GPS_ID = 0x7f0a0022;
    private int VOLUME_RINGER_ID = 0x7f0a0072;
    private int TASK_MANAGER_ID = 0x7f0a0026;
    private int CARRIER_APP_ID = 0x7f0a0027;
    private int AIRPLANE_MODE_ID = 0x7f0a0023;
    private int POWER_EFFICIENCY_ID = 0x7f0a0024;
    private int AUTO_SYNC_ID = 0x7f0a001f;
    private int VOLUME_MEDIA_ID = 0x7f0a0028;
    private int TORCH_ID = 0x7f0a0075;
    private int VIBRATION_MODE_ID = 0x7f0a0071;
    private int SILENT_MODE_ID = 0x7f0a0076;

    public QuickSettingsSense35(Context context, AttributeSet attrs) {
        super(context, attrs);

        bSettingItems = new boolean[ITEM_NUMBER];
        mSettingItems = new StatusBarPreference[ITEM_NUMBER];

        mStyleable = null;
        mStart = false;

        mContext = context;
        if (StatusBarFlag.HTC_SKIN)
            mStyleable = new StatusBarStyleable(context);
        init();
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
            bSettingItems[i] = false;
            mSettingItems[i] = null;
        }

        mSettingItems[ROTATION] = new Rotation(mContext, ((ViewStub) findViewById(ROTATION_ID)).inflate());
        mSettingItems[WIFI] = new Wifi(mContext, ((ViewStub) findViewById(WIFI_ID)).inflate());
        mSettingItems[BT] = new BT(mContext, ((ViewStub) findViewById(BT_ID)).inflate());
        mSettingItems[MOBILE_NETWORK] = new MobileNetwork(mContext, ((ViewStub) findViewById(MOBILE_NETWORK_ID)).inflate());
        mSettingItems[BRIGHTNESS] = new Brightness(mContext, ((ViewStub) findViewById(BRIGHTNESS_ID)).inflate());
        mSettingItems[HOTSPOT] = new HotSpot(mContext, ((ViewStub) findViewById(HOTSPOT_ID)).inflate());
        mSettingItems[SETTINGS] = new Settings(mContext, ((ViewStub) findViewById(SETTINGS_ID)).inflate());
        mSettingItems[GPS] = new GPS(mContext, ((ViewStub) findViewById(GPS_ID)).inflate());
        mSettingItems[TASK_MANAGER] = new TaskManager(mContext, ((ViewStub) findViewById(TASK_MANAGER_ID)).inflate());
        mSettingItems[VOLUME_RINGER] = new VolumeRinger(mContext, ((ViewStub) findViewById(VOLUME_RINGER_ID)).inflate());
        mSettingItems[CARRIER_APP] = new CarrierApp(mContext, ((ViewStub) findViewById(CARRIER_APP_ID)).inflate());
        mSettingItems[AIRPLANE_MODE] = new AirplaneMode(mContext, ((ViewStub) findViewById(AIRPLANE_MODE_ID)).inflate());
        mSettingItems[POWER_EFFICIENCY] = new PowerEfficiency(mContext, ((ViewStub) findViewById(POWER_EFFICIENCY_ID)).inflate());
        mSettingItems[AUTO_SYNC] = new AutoSync(mContext, ((ViewStub) findViewById(AUTO_SYNC_ID)).inflate());
        mSettingItems[VOLUME_MEDIA] = new Volume(mContext, ((ViewStub) findViewById(VOLUME_MEDIA_ID)).inflate());
        mSettingItems[TORCH] = new Torch(mContext, ((ViewStub) findViewById(TORCH_ID)).inflate());
        mSettingItems[VIBRATION_MODE] = new VibrationMode(mContext, ((ViewStub) findViewById(VIBRATION_MODE_ID)).inflate());
        mSettingItems[SILENT_MODE] = new SilentMode(mContext, ((ViewStub) findViewById(SILENT_MODE_ID)).inflate());

        ConnectivityManager cm = ((ConnectivityManager) mContext.getSystemService("connectivity"));

        if (cm.getTetherableWifiRegexs().length == 0
                && !cm.isTetheringSupported())
            mSettingItems[HOTSPOT].setEnable(false);

    }
}
