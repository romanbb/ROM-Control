package com.android.internal.policy.impl;

import com.android.internal.telephony.IccCard.State;

public interface KeyguardUpdateMonitor {

    public interface SimStateCallback {

    }

    public interface InfoCallback {

    }

    void registerInfoCallback(LockScreen lockScreen);

    void registerSimStateCallback(LockScreen lockScreen);

    boolean shouldShowBatteryInfo();

    boolean isDevicePluggedIn();

    int getBatteryLevel();

    State getSimState();

    boolean isDeviceCharged();

    boolean isDeviceProvisioned();

    CharSequence getTelephonyPlmn();

    boolean isKeyguardBypassEnabled();

    void removeCallback(LockScreen lockScreen);


}
