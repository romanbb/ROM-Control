package com.roman.vibrant.edt;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class BatteryActivity extends PreferenceActivity implements
		OnPreferenceClickListener, OnAmbilWarnaListener {
	Context context;

	private static final int COLOR_BATTERY = 1;

	private static final int COLOR_AUTO_CHARGING = 2;
	private static final int COLOR_AUTO_MEDIUM = 3;
	private static final int COLOR_AUTO_REGULAR = 4;
	private static final int COLOR_AUTO_LOW = 5;

	private int batteryColorPickerFlag;

	AmbilWarnaDialog batteryColorPickerDialog = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this.getApplicationContext();
		addPreferencesFromResource(R.xml.battery_prefs);

		/*
		 * battery style
		 */

		findPreference("battery_text_style_pref")
				.setOnPreferenceChangeListener(
						new OnPreferenceChangeListener() {

							public boolean onPreferenceChange(
									Preference preference, Object newValue) {
								preference = (ListPreference) preference;
								Integer val = Integer
										.parseInt((String) newValue);

								Settings.System.putInt(getContentResolver(),
										"battery_text_style", val);
								sendTimeIntent();
								return true;
							}
						});

		/*
		 * color battery text
		 */
		Preference battery_automatically_color_pref = (Preference) findPreference("battery_automatically_color_pref");

		try {
			if (Settings.System.getInt(getContentResolver(),
					"battery_text_color") == 1) {
				((CheckBoxPreference) battery_automatically_color_pref)
						.setChecked(true);
				battery_automatically_color_pref
						.setSummary(R.string.automatic_battery_enabled);
			} else {
				((CheckBoxPreference) battery_automatically_color_pref)
						.setChecked(false);
				battery_automatically_color_pref
						.setSummary(R.string.automatic_battery_disabled);
			}
		} catch (SettingNotFoundException e) {
			((CheckBoxPreference) battery_automatically_color_pref)
					.setChecked(true);
			Settings.System.putInt(getContentResolver(), "battery_text_color",
					1);
			battery_automatically_color_pref
					.setSummary(R.string.automatic_battery_enabled);
		}

		battery_automatically_color_pref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						boolean checked = ((CheckBoxPreference) preference)
								.isChecked();

						if (checked) {
							Settings.System.putInt(getContentResolver(),
									"battery_text_color", 1);
							preference
									.setSummary(R.string.automatic_battery_enabled);
						} else {
							Settings.System.putInt(getContentResolver(),
									"battery_text_color", 0);
							preference
									.setSummary(R.string.automatic_battery_disabled);
						}
						sendTimeIntent();
						return true;
					}

				});

		findPreference("battery_color_pref").setOnPreferenceClickListener(this);

		findPreference("battery_color_auto_charging")
				.setOnPreferenceClickListener(this);

		findPreference("battery_color_auto_regular")
				.setOnPreferenceClickListener(this);

		findPreference("battery_color_auto_medium")
				.setOnPreferenceClickListener(this);

		findPreference("battery_color_auto_low").setOnPreferenceClickListener(
				this);

		batteryColorPickerDialog = new AmbilWarnaDialog(this, 0xffffffff, this);

		/*
		 * prepend battery text
		 */

		EditTextPreference battery_text_prepend = (EditTextPreference) findPreference("battery_text_prepend");

		if (Settings.System.getString(getContentResolver(),
				"battery_text_prepend") == null) {
			Settings.System.putString(getContentResolver(),
					"battery_text_prepend", "");
		}

		battery_text_prepend.setSummary("\""
				+ Settings.System.getString(getContentResolver(),
						"battery_text_prepend") + "\"");

		battery_text_prepend.setText(Settings.System.getString(
				getContentResolver(), "battery_text_prepend"));

		battery_text_prepend
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						Settings.System.putString(getContentResolver(),
								"battery_text_prepend", (String) newValue);
						preference.setSummary("\"" + newValue + "\"");
						sendTimeIntent();
						return true;
					}
				});

		/*
		 * append battery text test
		 */
		EditTextPreference battery_text_append = (EditTextPreference) findPreference("battery_text_append");

		if (Settings.System.getString(getContentResolver(),
				"battery_text_append") == null) {
			Settings.System.putString(getContentResolver(),
					"battery_text_append", "% ");
		}

		battery_text_append.setSummary("\""
				+ Settings.System.getString(getContentResolver(),
						"battery_text_append") + "\"");

		battery_text_append.setText(Settings.System.getString(
				getContentResolver(), "battery_text_append"));

		battery_text_append
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						Settings.System.putString(getContentResolver(),
								"battery_text_append", (String) newValue);
						preference.setSummary("\"" + newValue + "\"");
						sendTimeIntent();
						return true;
					}
				});

		/*
		 * battery icon
		 */
		Preference show_battery_icon = (Preference) findPreference("show_battery_icon");

		try {
			if (Settings.System.getInt(getContentResolver(),
					"show_battery_icon") == 1) {
				((CheckBoxPreference) show_battery_icon).setChecked(true);
			} else {
				((CheckBoxPreference) show_battery_icon).setChecked(false);
			}
		} catch (SettingNotFoundException e) {
			((CheckBoxPreference) show_battery_icon).setChecked(true);
			Settings.System
					.putInt(getContentResolver(), "show_battery_icon", 1);
		}

		show_battery_icon
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						boolean checked = ((CheckBoxPreference) preference)
								.isChecked();

						if (checked) {
							Settings.System.putInt(getContentResolver(),
									"show_battery_icon", 1);
						} else {
							Settings.System.putInt(getContentResolver(),
									"show_battery_icon", 0);
						}
						sendTimeIntent();
						return true;
					}

				});
		refreshOptions();
	}

	public void sendTimeIntent() {
		Intent timeIntent = new Intent();
		timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
		sendBroadcast(timeIntent);
		refreshOptions();
	}

	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();

		if (key.equals("battery_color_auto_charging")) {
			batteryColorPickerFlag = COLOR_AUTO_CHARGING;

			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_charging", 0xFFFFFFFF), this);

			batteryColorPickerDialog.show();

		} else if (key.equals("battery_color_auto_regular")) {
			batteryColorPickerFlag = COLOR_AUTO_REGULAR;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_regular", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_color_auto_medium")) {
			batteryColorPickerFlag = COLOR_AUTO_MEDIUM;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_medium", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_color_auto_low")) {
			batteryColorPickerFlag = COLOR_AUTO_LOW;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color_auto_low", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_color_pref")) {
			batteryColorPickerFlag = COLOR_BATTERY;
			batteryColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"battery_color", 0xFFFFFFFF), this);
			batteryColorPickerDialog.show();
		}

		return true;
	}

	public void onOk(AmbilWarnaDialog dialog, int color) {

		switch (batteryColorPickerFlag) {

		case COLOR_BATTERY:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color", color);

			break;

		case COLOR_AUTO_CHARGING:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_charging", color);
			break;

		case COLOR_AUTO_LOW:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_low", color);
			break;

		case COLOR_AUTO_MEDIUM:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_medium", color);
			break;

		case COLOR_AUTO_REGULAR:

			Settings.System.putInt(context.getContentResolver(),
					"battery_color_auto_regular", color);
			break;
		}

		sendTimeIntent();
	}

	public void onCancel(AmbilWarnaDialog dialog) {
		// cancel was selected by the user
	}

	public void refreshOptions() {
		if (((CheckBoxPreference) findPreference("battery_automatically_color_pref"))
				.isChecked()) {
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
