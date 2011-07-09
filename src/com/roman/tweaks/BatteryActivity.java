package com.roman.tweaks;

import android.content.Context;
import android.content.CustomIntents;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.roman.tweaks.AmbilWarnaDialog.OnAmbilWarnaListener;

public class BatteryActivity extends PreferenceActivity implements
		OnAmbilWarnaListener, OnPreferenceChangeListener {
	Context context;

	private static final int COLOR_BATTERY = 1;

	private static final int COLOR_AUTO_CHARGING = 2;

	private static final int COLOR_AUTO_MEDIUM = 3;

	private static final int COLOR_AUTO_REGULAR = 4;

	private static final int COLOR_AUTO_LOW = 5;

	private int batteryColorPickerFlag;

	AmbilWarnaDialog batteryColorPickerDialog = null;

	private static final String PREF_BATTERY_TEXT_STYLE = "battery_text_style_pref";

	private static final String PREF_SHOW_BATTERY_ICON = "show_battery_icon";

	private static final String PREF_SHOW_CM_BATTERY_BAR = "show_cm_battery_icon";

	private static final String PREF_COLOR_AUTOMATICALLY = "battery_automatically_color_pref";

	private static final String PREF_COLOR_STATIC = "battery_color_pref";

	private static final String PREF_COLOR_CHARGING = "battery_color_auto_charging";

	private static final String PREF_COLOR_REGULAR = "battery_color_auto_regular";

	private static final String PREF_COLOR_MEDIUM = "battery_color_auto_medium";

	private static final String PREF_COLOR_LOW = "battery_color_auto_low";

	ListPreference mBatteryTextStyle;

	CheckBoxPreference mShowBatteryIcon;

	CheckBoxPreference mShowCMBatteryIcon;

	CheckBoxPreference mColorAutomatically;

	Preference mColorStatic;

	Preference mColorCharging;

	Preference mColorRegular;

	Preference mColorMedium;

	Preference mColorLow;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this.getApplicationContext();
		addPreferencesFromResource(R.xml.battery_prefs);

		PreferenceScreen prefs = getPreferenceScreen();

		mBatteryTextStyle = (ListPreference) prefs
				.findPreference(PREF_BATTERY_TEXT_STYLE);
		mShowBatteryIcon = (CheckBoxPreference) prefs
				.findPreference(PREF_SHOW_BATTERY_ICON);
		mShowCMBatteryIcon = (CheckBoxPreference) prefs
				.findPreference(PREF_SHOW_CM_BATTERY_BAR);
		mColorAutomatically = (CheckBoxPreference) prefs
				.findPreference(PREF_COLOR_AUTOMATICALLY);
		mColorStatic = prefs.findPreference(PREF_COLOR_STATIC);
		mColorCharging = prefs.findPreference(PREF_COLOR_CHARGING);
		mColorRegular = prefs.findPreference(PREF_COLOR_REGULAR);
		mColorMedium = prefs.findPreference(PREF_COLOR_MEDIUM);
		mColorLow = prefs.findPreference(PREF_COLOR_LOW);

		mBatteryTextStyle.setOnPreferenceChangeListener(this);

		batteryColorPickerDialog = new AmbilWarnaDialog(this, 0xffffffff, this);

		refreshOptions();
	}

	public void sendTimeIntent() {
		Intent timeIntent = new Intent();
		timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
		sendBroadcast(timeIntent);
		refreshOptions();
	}

	public void sendBatteryUpdateIntent() {
		Intent i = new Intent();
		i.setAction(CustomIntents.ACTION_BATTERY_UPDATE);
		sendBroadcast(i);
		sendBroadcast(i);
		refreshOptions();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mBatteryTextStyle) {
			// preference = (ListPreference) preference;

			int val = Integer.valueOf((String) newValue);

			Settings.System.putInt(getContentResolver(),
					Settings.System.STATUS_BAR_CM_BATTERY, val);
			sendBatteryUpdateIntent();
			return true;
		}
		return false;
	}

	public boolean onPreferenceTreeClick(PreferenceScreen screen,
			Preference preference) {
		// String key = preference.getKey();

		if (preference == mColorCharging) {
			batteryColorPickerFlag = COLOR_AUTO_CHARGING;

			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_charging", 0xFFFFFFFF), this);

			batteryColorPickerDialog.show();
			return true;

		} else if (preference == mColorRegular) {
			batteryColorPickerFlag = COLOR_AUTO_REGULAR;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_regular", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();
			return true;

		} else if (preference == mColorMedium) {
			batteryColorPickerFlag = COLOR_AUTO_MEDIUM;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_medium", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();
			return true;

		} else if (preference == mColorLow) {
			batteryColorPickerFlag = COLOR_AUTO_LOW;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_low", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();
			return true;

		} else if (preference == mColorStatic) {
			batteryColorPickerFlag = COLOR_BATTERY;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();
			return true;

		} else if (preference == mColorAutomatically) {
			boolean checked = ((CheckBoxPreference) preference).isChecked();

			if (checked) {
				Settings.System.putInt(getContentResolver(),
						"battery_text_color", 1);
				preference.setSummary(R.string.automatic_battery_enabled);
			} else {
				Settings.System.putInt(getContentResolver(),
						"battery_text_color", 0);
				preference.setSummary(R.string.automatic_battery_disabled);
			}
			sendBatteryUpdateIntent();
			return true;
		} else if (preference == mShowBatteryIcon) {
			boolean checked = ((CheckBoxPreference) preference).isChecked();

			if (checked) {
				Settings.System.putInt(getContentResolver(),
						"show_battery_icon", 1);
			} else {
				Settings.System.putInt(getContentResolver(),
						"show_battery_icon", 0);
			}
			// sendTimeIntent();
			sendBatteryUpdateIntent();
			return true;

		} else if (preference == mShowCMBatteryIcon) {
			boolean checked = ((CheckBoxPreference) preference).isChecked();

			if (checked) {
				Settings.System.putInt(getContentResolver(),
						Settings.System.STATUS_BAR_CM_BATTERY_ICON, 1);
			} else {
				Settings.System.putInt(getContentResolver(),
						Settings.System.STATUS_BAR_CM_BATTERY_ICON, 0);
			}
			// sendTimeIntent();
			sendBatteryUpdateIntent();
			return true;

		}

		return false;
	}

	public void onOk(AmbilWarnaDialog dialog, int color) {

		switch (batteryColorPickerFlag) {

		case COLOR_BATTERY:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color", color);
			sendBatteryUpdateIntent();
			break;

		case COLOR_AUTO_CHARGING:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_charging", color);
			sendBatteryUpdateIntent();
			break;

		case COLOR_AUTO_LOW:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_low", color);
			sendBatteryUpdateIntent();
			break;

		case COLOR_AUTO_MEDIUM:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_medium", color);
			sendBatteryUpdateIntent();
			break;

		case COLOR_AUTO_REGULAR:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_regular", color);
			sendBatteryUpdateIntent();
			break;
		}

	}

	public void onCancel(AmbilWarnaDialog dialog) {
		// cancel was selected by the user
	}

	public void refreshOptions() {
		if (((CheckBoxPreference) mColorAutomatically).isChecked()) {
			findPreference("battery_color_pref").setEnabled(false);
			findPreference("battery_color_auto_charging").setEnabled(true);
			findPreference("battery_color_auto_regular").setEnabled(true);
			findPreference("battery_color_auto_medium").setEnabled(true);
			findPreference("battery_color_auto_low").setEnabled(true);
		} else {
			findPreference("battery_color_pref").setEnabled(true);
			findPreference("battery_color_auto_charging").setEnabled(false);
			findPreference("battery_color_auto_regular").setEnabled(false);
			findPreference("battery_color_auto_medium").setEnabled(false);
			findPreference("battery_color_auto_low").setEnabled(false);
		}
	}

}
