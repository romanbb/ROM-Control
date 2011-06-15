package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SignalText extends TextView {

	int dBm = 0;
	int ASU = 0;

	private boolean mAttached;
	private String prependText = "-";
	private String appendText = "";

	private static final int STYLE_SHOW = 1;
	private static final int STYLE_DISABLE = 2;
	private static final int STYLE_SMALL_DBM = 3;

	private int style;

	private SignalStrength signal;

	public SignalText(Context context) {
		super(context);

	}

	public SignalText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SignalText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// updateSettings();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (!mAttached) {
			mAttached = true;
			IntentFilter filter = new IntentFilter();

			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

			getContext().registerReceiver(mIntentReceiver, filter, null,
					getHandler());
		}
		prependText = "-";
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAttached) {
			getContext().unregisterReceiver(mIntentReceiver);
			mAttached = false;
		}
	}

	/**
	 * Handles changes ins battery level and charger connection
	 */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			style = Settings.System.getInt(getContext().getContentResolver(),
					"signal_text_style", STYLE_DISABLE);

			updateSignalColor();
			updateSignalText();
		}
	};

	final void updateSignalColor() {

		boolean autoColor = Settings.System.getInt(getContext()
				.getContentResolver(), "signal_text_color_enable", 1) == 1 ? true
				: false;

		int color_regular = Settings.System.getInt(getContext()
				.getContentResolver(), "signal_text_color_reg", 0xFFFFFFFF);

		int color_0 = Settings.System.getInt(getContext().getContentResolver(),
				"signal_text_color_0", 0xFF93D500);

		int color_1 = Settings.System.getInt(getContext().getContentResolver(),
				"signal_text_color_1", 0xFFFFFFFF);

		int color_2 = Settings.System.getInt(getContext().getContentResolver(),
				"signal_text_color_2", 0xFFD5A300);

		int color_3 = Settings.System.getInt(getContext().getContentResolver(),
				"signal_text_color_3", 0xFFD54B00);

		int color_4 = Settings.System.getInt(getContext().getContentResolver(),
				"signal_text_color_4", 0xFFFFFFFF);

		if (autoColor) {

		} else {
			setTextColor(color_regular);
		}
	}

	/**
	 * Sets the output text. Kind of onDraw of canvas based classes
	 * 
	 * @param intent
	 */
	final void updateSignalText() {
		if (style == STYLE_SHOW) {
			this.setVisibility(View.VISIBLE);

			String result = prependText + Integer.toString(dBm)
					+ appendText;

			setText(result);
		} else if (style == STYLE_SMALL_DBM) {
			this.setVisibility(View.VISIBLE);

			String result = prependText + Integer.toString(dBm)
					+ "dBm ";

			SpannableStringBuilder formatted = new SpannableStringBuilder(
					result);
			int start = result.indexOf("d");

			CharacterStyle style = new RelativeSizeSpan(0.7f);
			formatted.setSpan(style, start, start + 3,
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

			setText(formatted);
		} else {
			this.setVisibility(View.GONE);
		}
	}

	/* —————————– */
	/* Start the PhoneState listener */
	/* —————————– */
	private class MyPhoneStateListener extends PhoneStateListener {
		/*
		 * Get the Signal strength from the provider, each tiome there is an
		 * update
		 */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			signal = signalStrength;

			if (signal != null) {
				ASU = signal.getGsmSignalStrength();
			}
			dBm = -113 + (2 * ASU);
		}

	};/* End of private Class */

}
