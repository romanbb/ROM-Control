package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class BatteryText extends TextView {
	private int batteryLevel;
	private int batteryStatus;
	private boolean mAttached;
	private String prependText = "";
	private String appendText = "%";

	private static final int STYLE_SHOW = 1;
	private static final int STYLE_DISABLE = 2;
	private static final int STYLE_SMALL_PERCENT = 3;

	private int style;

	public BatteryText(Context context) {
		super(context);

	}

	public BatteryText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BatteryText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// updateSettings();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (!mAttached) {
			mAttached = true;
			IntentFilter filter = new IntentFilter();

			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

			getContext().registerReceiver(mIntentReceiver, filter, null,
					getHandler());
		}
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
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

				batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
						BatteryManager.BATTERY_STATUS_UNKNOWN);

				batteryLevel = intent.getIntExtra("level", 50);

			}

			appendText = Settings.System.getString(getContext()
					.getContentResolver(), "battery_text_append");

			prependText = Settings.System.getString(getContext()
					.getContentResolver(), "battery_text_prepend");

			style = Settings.System.getInt(getContext().getContentResolver(),
					"battery_text_style", STYLE_DISABLE);

			updateBatteryColor();
			updateBatteryText(intent);
		}
	};

	final void updateBatteryColor() {

		boolean autoColorBatteryText = Settings.System.getInt(getContext()
				.getContentResolver(), "battery_text_color", 1) == 1 ? true
				: false;

		int color_auto_charging = Settings.System.getInt(getContext()
				.getContentResolver(), "battery_color_auto_charging",
				0xFF93D500);

		int color_auto_regular = Settings.System
				.getInt(getContext().getContentResolver(),
						"battery_color_auto_regular", 0xFFFFFFFF);

		int color_auto_medium = Settings.System.getInt(getContext()
				.getContentResolver(), "battery_color_auto_medium", 0xFFD5A300);

		int color_auto_low = Settings.System.getInt(getContext()
				.getContentResolver(), "battery_color_auto_low", 0xFFD54B00);

		int color_regular = Settings.System.getInt(getContext()
				.getContentResolver(), "battery_color", 0xFFFFFFFF);

		if (autoColorBatteryText) {
			if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING
					|| batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
				setTextColor(color_auto_charging);

			} else {
				if (batteryLevel < 15) {
					setTextColor(color_auto_low);
				} else if (batteryLevel < 40) {
					setTextColor(color_auto_medium);
				} else {
					setTextColor(color_auto_regular);
				}

			}
		} else {
			setTextColor(color_regular);
		}
	}

	/**
	 * Sets the output text. Kind of onDraw of canvas based classes
	 * 
	 * @param intent
	 */
	final void updateBatteryText(Intent intent) {

		if (style == STYLE_SHOW) {
			this.setVisibility(View.VISIBLE);

			String result = prependText + Integer.toString(batteryLevel)
					+ appendText;

			setText(result);
		} else if (style == STYLE_SMALL_PERCENT) {
			this.setVisibility(View.VISIBLE);

			String result = prependText + Integer.toString(batteryLevel) + "% ";

			SpannableStringBuilder formatted = new SpannableStringBuilder(
					result);
			int start = result.indexOf("%");

			CharacterStyle style = new RelativeSizeSpan(0.7f);
			formatted.setSpan(style, start, start + 1,
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

			setText(formatted);
		} else {
			this.setVisibility(View.GONE);
		}
	}

}
