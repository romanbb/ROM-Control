package com.roman.vibrant.edt;

import android.app.Activity;
import android.provider.Settings;

public class Test extends Activity {

	private int getAction(int why) {
		int setting = 0;

		setting = Settings.System.getInt(getContentResolver(),
				"lockscreen_delay_behavior", 1);

		if (why == 3) { // system thinks it's a screen timeout
			if (setting == 1) { // only delay on timeout
				return 3;
			} else if (setting == 2) { // only delay on power button
				return 1;
			} else if (setting == 3) { // delay on either
				return 3;
			} else if (setting == 4) { // never delay
				return 1;
			}
		} else if (why == 2) { // power button pressed
			if (setting == 1) { // only delay on timeout
				return 1;
			} else if (setting == 2) { // only delay on power button
				return 3;
			} else if (setting == 3) { // delay on either
				return 3;
			} else if (setting == 4) {
				return 1;
			}
		} 
		return why;
	}

}