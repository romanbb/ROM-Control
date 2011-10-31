
package com.android.internal.policy.impl;

import java.io.IOException;
import java.util.ArrayList;

import android.R;
import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.app.ShutdownThread;
import com.google.android.collect.Lists;

public class GlobalActions implements OnClickListener, OnDismissListener {

    public static final String TAG = "GlobalActions";

    private Object sIsConfirmingGuard;
    private boolean sIsConfirming = false;

    private ArrayList<Action> mItems;
    private AlertDialog mDialog;

    private ToggleAction mSilentModeToggle;
    private ToggleAction mAirplaneModeOn;
    private ToggleAction.State mAirplaneState = ToggleAction.State.Off;

    private final AudioManager mAudioManager;

    private StatusBarManager mStatusBar;

    private MyAdapter mAdapter;
    private Context mContext;

    private static boolean mKeyguardShowing = false;
    private boolean mDeviceProvisioned = false;

    private static boolean mIsWaitingForEcmExit = false;

    // ids
    public static final int SILENT_ENABLED_ICON = 0x1080031;
    public static final int SILENT_DISABLED_ICON = 0x1080032;
    public static final int SILENT_MESSAGE = 0x1040153;
    public static final int SILENT_ENABLED_STATUS = 0x1040154;
    public static final int SILENT_DISABLED_STATUS = 0x1040155;

    public static final int AIRPLANE_ENABLED_ICON = 0x10801bc;
    public static final int AIRPLANE_DISABLED_ICON = 0x10801bd;
    public static final int AIRPLANE_MESSAGE = 0x1040156;
    public static final int AIRPLANE_ENABLED_STATUS = 0x1040157;
    public static final int AIRPLANE_DISABLED_STATUS = 0x1040158;

    public static final int R_LAYOUT_GLOBAL_ACTIONS_ITEM = 0x1090029;
    public static final int R_ID_STATUS = 0x10201b2;

    public static final int R_DRAWABLE_IC_LOCK_POWER_OFF = 0x1080030;
    public static final int R_STRING_GLOBAL_ACTION_POWER_OFF = 0x1040152;
    
    public static final int REBOOT_IMAGE = 0x1080455;
    public static final int REBOOT_STRING = 0x1040358;
    
    public static final int SCREENSHOT_ICON = 0x1080456;
    public static final int SCREENSHOT_STRING = 0x1040527;

    public static final int R_BOOL_CONFIG_SLOW_BLUR = 0x10d0001;
    public static final int R_STRING_GLOBAL_ACTIONS = 0x1040150;

    public static final int R_DRAWABLE_IC_LOCK_SILENT_MODE_VIBRATE = 0x10801c0;

    public GlobalActions(Context context) {
        mContext = context;

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(mBroadcastReceiver, filter);

        // get notified of phone state changes
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);

    }

    private AlertDialog createDialog() {
        mSilentModeToggle = new ToggleAction(SILENT_ENABLED_ICON,
                SILENT_DISABLED_ICON,
                SILENT_MESSAGE,
                SILENT_ENABLED_STATUS,
                SILENT_DISABLED_STATUS) {

            void willCreate() {
                // XXX: FIXME: switch to ic_lock_vibrate_mode when available
                mEnabledIconResId = (Settings.System.getInt(mContext.getContentResolver(),
                        "vibrate_in_silent", 1) == 1) ? R_DRAWABLE_IC_LOCK_SILENT_MODE_VIBRATE : R.drawable.ic_lock_silent_mode;
            }

            void onToggle(boolean on) {
                if (on) {
                    mAudioManager.setRingerMode((Settings.System.getInt(mContext.getContentResolver(),
                            "vibrate_in_silent", 1) == 1)
                            ? AudioManager.RINGER_MODE_VIBRATE
                            : AudioManager.RINGER_MODE_SILENT);
                } else {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }

        };

        mAirplaneModeOn = new ToggleAction(AIRPLANE_ENABLED_ICON,
                AIRPLANE_DISABLED_ICON,
                AIRPLANE_MESSAGE,
                AIRPLANE_ENABLED_STATUS,
                AIRPLANE_DISABLED_STATUS) {

            @Override
            protected void changeStateFromPress(boolean buttonOn) {
                // In ECM mode airplane state cannot be changed
                if (!(Boolean.parseBoolean(
                        SystemProperties.get("ril.cdma.inecmmode")))) {
                    mState = buttonOn ? State.TurningOn : State.TurningOff;
                    mAirplaneState = mState;
                }
            }

            @Override
            void onToggle(boolean on) {
                if (Boolean.parseBoolean(
                        SystemProperties.get("ril.cdma.inecmmode"))) {
                    mIsWaitingForEcmExit = true;
                    // Launch ECM exit dialog
                    Intent ecmDialogIntent =
                            new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                    ecmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(ecmDialogIntent);
                } else {
                    changeAirplaneModeSystemSetting(on);
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }

        };

        SinglePressAction rebootOptions = new SinglePressAction(
                REBOOT_IMAGE,
                REBOOT_STRING) {

            CharSequence rebootOpts[][] = {
                    {
                            "Reboot", "Hot Reboot", "Recovery", "Download"
                    },
                    {
                            "", "hot_reboot", "recovery", "download"
                    }
            };

            String rebootChoice = "";

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }

            @Override
            public void onPress() {
                AlertDialog dialog;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setSingleChoiceItems(rebootOpts[0], 0, new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        rebootChoice = (String) rebootOpts[1][which];

                    }
                });
                builder.setTitle("Reboot options");
                builder.setPositiveButton("Reboot", new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (rebootChoice.equals("hot_reboot")) {
                            // hot reboot
                            try {
                                Runtime.getRuntime().exec(new String[] {
                                        "pkill", "-TERM", "-f", "system_server"
                                });
                            } catch (IOException e) {
                            }
                        } else {
                            ShutdownThread.reboot(mContext, rebootChoice, true);
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog = builder.create();

                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
                if (!mContext.getResources().getBoolean(
                        R_BOOL_CONFIG_SLOW_BLUR)) {
                    dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                }
                dialog.show();
            }
        };

        mItems = Lists.newArrayList(
                // silent mode
                mSilentModeToggle,
                // next: airplane mode
                mAirplaneModeOn,
                //screenshot
                new SinglePressAction(SCREENSHOT_ICON, SCREENSHOT_STRING) {
                    public void onPress() {
                        Intent intent = new Intent("android.intent.action.SCREENSHOT");
                        mContext.sendBroadcast(intent);
                    }

                    public boolean showDuringKeyguard() {
                        return true;
                    }

                    public boolean showBeforeProvisioning() {
                        return true;
                    }
                },
                //reboot opts
                rebootOptions,
                // last: power off
                new SinglePressAction(
                        R_DRAWABLE_IC_LOCK_POWER_OFF,
                        R_STRING_GLOBAL_ACTION_POWER_OFF) {

                    public boolean showDuringKeyguard() {
                        return true;
                    }

                    public boolean showBeforeProvisioning() {
                        return false;
                    }

                    @Override
                    public void onPress() {
                        ShutdownThread.shutdown(mContext, true);
                    }

                });

        mAdapter = new MyAdapter();

        final AlertDialog.Builder ab = new AlertDialog.Builder(mContext);

        ab.setAdapter(mAdapter, this)
                .setInverseBackgroundForced(true);

        final AlertDialog dialog = ab.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        if (!mContext.getResources().getBoolean(R_BOOL_CONFIG_SLOW_BLUR)) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }

        dialog.setOnDismissListener(this);

        return dialog;

    }

    private void prepareDialog() {

        final boolean silentModeOn = mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
        mSilentModeToggle.updateState(silentModeOn ? ToggleAction.State.On : ToggleAction.State.Off);

        mAirplaneModeOn.updateState(mAirplaneState);

        mAdapter.notifyDataSetChanged();
        if (mKeyguardShowing) {
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        } else {
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        }
        mDialog.setTitle(R_STRING_GLOBAL_ACTIONS);
    }

    private boolean isGlobalActionsConfirming() {
        return sIsConfirming;
    }

    /** {@inheritDoc} */

    public void onDismiss(DialogInterface dialog) {
        mStatusBar.disable(StatusBarManager.DISABLE_NONE);
    }

    /** {@inheritDoc} */

    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        mAdapter.getItem(which).onPress();
    }

    public void releaseDialog() {
        if (mDialog != null)
            mDialog.dismiss();
    }

    /**
     * Change the airplane mode system setting
     */
    private void changeAirplaneModeSystemSetting(boolean on) {
        Settings.System.putInt(
                mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON,
                on ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("state", on);
        mContext.sendBroadcast(intent);
    }

    /**
     * Show the global actions dialog (creating if necessary)
     * 
     * @param keyguardShowing True if keyguard is showing
     */
    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        mKeyguardShowing = keyguardShowing;
        mDeviceProvisioned = isDeviceProvisioned;
        if (mDialog == null) {
            mStatusBar = (StatusBarManager) mContext.getSystemService("statusbar");
            mDialog = createDialog();
        }
        prepareDialog();

        mStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
        mDialog.show();
    }

    /**
     * What each item in the global actions dialog must be able to support.
     */
    private interface Action {
        View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater);

        void onPress();

        /**
         * @return whether this action should appear in the dialog when the
         *         keygaurd is showing.
         */
        boolean showDuringKeyguard();

        /**
         * @return whether this action should appear in the dialog before the
         *         device is provisioned.
         */
        boolean showBeforeProvisioning();

        boolean isEnabled();
    }

    /**
     * A single press action maintains no state, just responds to a press and
     * takes an action.
     */
    private static abstract class SinglePressAction implements Action {
        private final int mIconResId;
        private final int mMessageResId;

        protected SinglePressAction(int iconResId, int messageResId) {
            mIconResId = iconResId;
            mMessageResId = messageResId;
        }

        public boolean isEnabled() {
            return true;
        }

        abstract public void onPress();

        public View create(
                Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = (convertView != null) ?
                    convertView :
                    inflater.inflate(R_LAYOUT_GLOBAL_ACTIONS_ITEM, parent, false);

            ImageView icon = (ImageView) v.findViewById(R.id.icon);
            TextView messageView = (TextView) v.findViewById(R.id.message);

            v.findViewById(R_ID_STATUS).setVisibility(View.GONE);

            icon.setImageDrawable(context.getResources().getDrawable(mIconResId));
            messageView.setText(mMessageResId);

            return v;
        }
    }

    private static abstract class ToggleAction implements Action {

        protected int mEnabledIconResId;
        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledStatusMessageResId;
        protected int mMessageResId;
        protected State mState;

        public ToggleAction(int enabledIconResId,
                int disabledIconResId,
                int essage,
                int enabledStatusMessageResId,
                int disabledStatusMessageId) {
            mEnabledIconResId = enabledIconResId;
            mDisabledIconResid = disabledIconResId;
            mMessageResId = essage;
            mEnabledStatusMessageResId = enabledStatusMessageResId;
            mDisabledStatusMessageResId = disabledStatusMessageId;
        }

        enum State {
            Off(false),
            TurningOn(true),
            TurningOff(true),
            On(false);

            private final boolean inTransition;

            State(boolean intermediate) {
                inTransition = intermediate;
            }

            public boolean inTransition() {
                return inTransition;
            }

        }

        void willCreate() {

        }

        public View create(Context context, View convertView, ViewGroup parent,
                LayoutInflater inflater) {
            willCreate();

            View v = (convertView != null) ?
                    convertView :
                    inflater.inflate(R_LAYOUT_GLOBAL_ACTIONS_ITEM, parent, false);

            ImageView icon = (ImageView) v.findViewById(R.id.icon);
            TextView messageView = (TextView) v.findViewById(R.id.message);
            TextView statusView = (TextView) v.findViewById(R_ID_STATUS);

            messageView.setText(mMessageResId);

            boolean on = ((mState == State.On) || (mState == State.TurningOn));
            icon.setImageDrawable(context.getResources().getDrawable(
                    (on ? mEnabledIconResId : mDisabledIconResid)));
            statusView.setText(on ? mEnabledStatusMessageResId : mDisabledStatusMessageResId);
            statusView.setVisibility(View.VISIBLE);

            final boolean enabled = isEnabled();
            messageView.setEnabled(enabled);
            statusView.setEnabled(enabled);
            icon.setEnabled(enabled);
            v.setEnabled(enabled);

            return v;
        }

        public final void onPress() {
            if (mState.inTransition()) {
                Log.w(TAG, "shouldn't be able to toggle when in transition");
                return;
            }

            final boolean nowOn = !(mState == State.On);
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }

        public final void onPressAction() {
            if (mState.inTransition()) {
                Log.w(TAG, "shouldn't be able to toggle when in transition");
                return;
            }

            final boolean nowOn = !(mState == State.On);
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }

        public boolean isEnabled() {
            return !mState.inTransition();
        }

        /**
         * Implementations may override this if their state can be in on of the
         * intermediate states until some notification is received (e.g airplane
         * mode is 'turning off' until we know the wireless connections are back
         * online
         * 
         * @param buttonOn Whether the button was turned on or off
         */
        protected void changeStateFromPress(boolean buttonOn) {
            mState = buttonOn ? State.On : State.Off;
        }

        abstract void onToggle(boolean on);

        public void updateState(State state) {
            mState = state;
        }

    }

    /**
     * The adapter used for the list within the global actions dialog, taking
     * into account whether the keyguard is showing via
     * {@link GlobalActions#mKeyguardShowing} and whether the device is
     * provisioned via {@link GlobalActions#mDeviceProvisioned}.
     */
    private class MyAdapter extends BaseAdapter {

        public int getCount() {
            int count = 0;

            for (int i = 0; i < mItems.size(); i++) {
                final Action action = mItems.get(i);

                if (mKeyguardShowing && !action.showDuringKeyguard()) {
                    continue;
                }
                if (!mDeviceProvisioned && !action.showBeforeProvisioning()) {
                    continue;
                }
                count++;
            }
            return count;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        public Action getItem(int position) {

            int filteredPos = 0;
            for (int i = 0; i < mItems.size(); i++) {
                final Action action = mItems.get(i);
                if (mKeyguardShowing && !action.showDuringKeyguard()) {
                    continue;
                }
                if (!mDeviceProvisioned && !action.showBeforeProvisioning()) {
                    continue;
                }
                if (filteredPos == position) {
                    return action;
                }
                filteredPos++;
            }

            throw new IllegalArgumentException("position " + position
                    + " out of range of showable actions"
                    + ", filtered count=" + getCount()
                    + ", keyguardshowing=" + mKeyguardShowing
                    + ", provisioned=" + mDeviceProvisioned);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Action action = getItem(position);
            return action.create(mContext, convertView, parent, LayoutInflater.from(mContext));
        }

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                    || Intent.ACTION_SCREEN_OFF.equals(action)) {
                String reason = intent.getStringExtra("reason");
                if (!"globalactions".equals(reason)) {
                    mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action)) {
                // Airplane mode can be changed after ECM exits if airplane
                // toggle button
                // is pressed during ECM mode
                if (!(intent.getBooleanExtra("PHONE_IN_ECM_STATE", false)) &&
                        mIsWaitingForEcmExit) {
                    mIsWaitingForEcmExit = false;
                    changeAirplaneModeSystemSetting(true);
                }
            }
        }
    };

    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            final boolean inAirplaneMode = serviceState.getState() == ServiceState.STATE_POWER_OFF;
            mAirplaneState = inAirplaneMode ? ToggleAction.State.On : ToggleAction.State.Off;
            if (mAirplaneModeOn != null && mAdapter != null) {
                mAirplaneModeOn.updateState(mAirplaneState);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private static final int MESSAGE_DISMISS = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_DISMISS) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        }
    };
}
