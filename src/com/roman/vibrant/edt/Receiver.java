package com.roman.vibrant.edt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

	public static final String CUSTOM_INTENT = "com.roman.vibrant.edt.SET_DEFAULTS";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("EDT", "OMG RECIEVED AN INTENT");
		Intent i = new Intent(context, Main.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
	
}
