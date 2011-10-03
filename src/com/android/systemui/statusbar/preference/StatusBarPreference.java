
package com.android.systemui.statusbar.preference;

import com.android.systemui.statusbar.StatusBarService;
import com.android.systemui.statusbar.StatusBarStyleable;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class StatusBarPreference {

    StatusBarService mService;
    protected CheckBox mCheckBox;
    protected LinearLayout mContentView;
    protected Context mContext;
    protected ImageView mDivider;
    private boolean mEnable;
    protected ImageView mIcon;
    protected LinearLayout mItemView;
    protected SeekBar mSeekBar;
    private StatusBarStyleable mStyleable;
    protected TextView mSummary;
    protected TextView mTitle;

    public StatusBarPreference(Context c, View v) {
        // TODO Auto-generated constructor stub
    }

    public void setEnable(boolean enable) {

    }

    public boolean getEnable() {
        // TODO Auto-generated method stub
        return false;
    }

    public void onStart() {
        // TODO Auto-generated method stub

    }

    public void onStop() {
        // TODO Auto-generated method stub

    }

    public void updateResources() {
        // TODO Auto-generated method stub

    }

    public void setService(StatusBarService service) {
        // TODO Auto-generated method stub
        
    }
}
