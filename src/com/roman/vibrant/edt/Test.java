package com.roman.vibrant.edt;

import android.app.Activity;
import android.provider.Settings;

public class Test extends Activity {
	public static int AWAKE_INTERVAL_DEFAULT_MS = 5;

	public void test() {
		Settings.System.getInt(this.getContentResolver(),
				"custom_lockscreen_timeout", 5000);
	}
	
}