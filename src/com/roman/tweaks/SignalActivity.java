package com.roman.tweaks;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.roman.tweaks.AmbilWarnaDialog.OnAmbilWarnaListener;

public class SignalActivity extends PreferenceActivity implements
		OnPreferenceClickListener, OnAmbilWarnaListener {

	private static final int STYLE_SHOW = 1;
	private static final int STYLE_DISABLE = 2;
	private static final int STYLE_SMALL_DBM = 3;

	private static final int COLOR_0 = 4;
	private static final int COLOR_1 = 5;
	private static final int COLOR_2 = 6;
	private static final int COLOR_3 = 7;
	private static final int COLOR_4 = 8;

	private static final int COLOR_STATIC = 9;
	private static final int COLOR_AUTO = 10;

	private Context context;

	private int signalTextColorPickerFlag;

	AmbilWarnaDialog signalColorPickerDialog = null;

	public void onCreate(Bundle ofLove) {
		super.onCreate(ofLove);
		context = this.getApplicationContext();
		addPreferencesFromResource(R.xml.signal_prefs);
		signalColorPickerDialog = new AmbilWarnaDialog(this, 0xffffffff, this);

		findPreference("signal_color_0").setOnPreferenceClickListener(this);
		findPreference("signal_color_1").setOnPreferenceClickListener(this);
		findPreference("signal_color_2").setOnPreferenceClickListener(this);
		findPreference("signal_color_3").setOnPreferenceClickListener(this);
		findPreference("signal_color_4").setOnPreferenceClickListener(this);
		findPreference("signal_color_static")
				.setOnPreferenceClickListener(this);

		/*
		 * color signal text
		 */
		Preference signal_automatically_color_pref = (Preference) findPreference("signal_automatically_color_pref");

		try {
			if (Settings.System.getInt(getContentResolver(),
					"tweaks_signal_text_autocolor_enable") == 1) {
				((CheckBoxPreference) signal_automatically_color_pref)
						.setChecked(true);

			} else {
				((CheckBoxPreference) signal_automatically_color_pref)
						.setChecked(false);
			}
		} catch (SettingNotFoundException e) {

			((CheckBoxPreference) signal_automatically_color_pref)
					.setChecked(true);
			Settings.System.putInt(getContentResolver(),
					"automatic_battery_enabled", 1);

		}

		signal_automatically_color_pref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						boolean checked = ((CheckBoxPreference) preference)
								.isChecked();

						if (checked) {
							Settings.System.putInt(getContentResolver(),
									"tweaks_signal_text_autocolor_enable", 1);

						} else {
							Settings.System.putInt(getContentResolver(),
									"tweaks_signal_text_autocolor_enable", 0);

						}

						Intent i = new Intent();
						i.setAction(ConnectivityManager.DATA_ICON_ACTION);
						sendBroadcast(i);
						sendBroadcast(i);

						refreshOptions();
						return true;
					}

				});

		/*
		 * dbm text style
		 */

		findPreference("signal_text_style_pref").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						preference = (ListPreference) preference;
						Integer val = Integer.parseInt((String) newValue);

						Settings.System.putInt(getContentResolver(),
								"tweaks_signal_text_style", val);

						Intent i = new Intent();
						i.setAction(ConnectivityManager.SIGNAL_ICON_ACTION);
						sendBroadcast(i);
						sendBroadcast(i);
						return true;
					}
				});

		/*
		 * signal bar visibility
		 */
		Preference show_signal_bars = (Preference) findPreference("show_signal_bars");

		try {
			if (Settings.System.getInt(getContentResolver(),
					"tweaks_show_signal_bars") == 1) {
				((CheckBoxPreference) show_signal_bars).setChecked(true);
			} else {
				((CheckBoxPreference) show_signal_bars).setChecked(false);
			}
		} catch (SettingNotFoundException e) {
			((CheckBoxPreference) show_signal_bars).setChecked(true);
			Settings.System.putInt(getContentResolver(),
					"tweaks_show_signal_bars", 1);
		}

		show_signal_bars
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						boolean checked = ((CheckBoxPreference) preference)
								.isChecked();

						if (checked) {
							Settings.System.putInt(getContentResolver(),
									"tweaks_show_signal_bars", 1);
						} else {
							Settings.System.putInt(getContentResolver(),
									"tweaks_show_signal_bars", 0);
						}
						Intent i = new Intent();
						i.setAction(ConnectivityManager.DATA_ICON_ACTION);
						sendBroadcast(i);

						sendBroadcast(i);
						return true;

					}

				});

		refreshOptions();

		findPreference("");

		/*
		 * 4g/h statusbar icon
		 */

		if (Settings.System.getInt(getContentResolver(), "tweaks_show_4g_icon",
				0) == 1) {
			((CheckBoxPreference) findPreference("show_4g_icon"))
					.setChecked(true);
		} else {
			((CheckBoxPreference) findPreference("show_4g_icon"))
					.setChecked(false);
		}

		findPreference("show_4g_icon").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						if (((CheckBoxPreference) preference).isChecked()) {
							Settings.System.putInt(getContentResolver(),
									"tweaks_show_4g_icon", 1);
						} else {
							Settings.System.putInt(getContentResolver(),
									"tweaks_show_4g_icon", 0);
						}
						Intent i = new Intent();
						i.setAction(ConnectivityManager.DATA_ICON_ACTION);
						sendBroadcast(i);

						sendBroadcast(i);
						return true;

					}
				});
	}

	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();

		if (key.equals("signal_color_0")) {
			signalTextColorPickerFlag = COLOR_0;

			signalColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"tweaks_signal_text_color_0", 0xFFFFFFFF), this);

			signalColorPickerDialog.show();

		} else if (key.equals("signal_color_1")) {
			signalTextColorPickerFlag = COLOR_1;
			signalColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"tweaks_signal_text_color_1", 0xFFFFFFFF), this);
			signalColorPickerDialog.show();

		} else if (key.equals("signal_color_2")) {

			signalTextColorPickerFlag = COLOR_2;
			signalColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"tweaks_signal_text_color_2", 0xFFFFFFFF), this);
			signalColorPickerDialog.show();

		} else if (key.equals("signal_color_3")) {

			signalTextColorPickerFlag = COLOR_3;
			signalColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"tweaks_signal_text_color_3", 0xFFFFFFFF), this);
			signalColorPickerDialog.show();

		} else if (key.equals("signal_color_4")) {

			signalTextColorPickerFlag = COLOR_4;
			signalColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"tweaks_signal_text_color_4", 0xFFFFFFFF), this);
			signalColorPickerDialog.show();

		} else if (key.equals("signal_color_static")) {

			signalTextColorPickerFlag = COLOR_STATIC;
			signalColorPickerDialog = new AmbilWarnaDialog(this,
					Settings.System.getInt(getContentResolver(),
							"tweaks_signal_text_color", 0xFFFFFFFF), this);
			signalColorPickerDialog.show();
		}

		
	
		return true;
	}

	public void onOk(AmbilWarnaDialog dialog, int color) {

		switch (signalTextColorPickerFlag) {

		case COLOR_0:

			Settings.System.putInt(context.getContentResolver(),
					"tweaks_signal_text_color_0", color);
			broadcastColorChange();
			break;

		case COLOR_1:

			Settings.System.putInt(context.getContentResolver(),
					"tweaks_signal_text_color_1", color);
			broadcastColorChange();
			break;

		case COLOR_2:

			Settings.System.putInt(context.getContentResolver(),
					"tweaks_signal_text_color_2", color);
			broadcastColorChange();
			break;

		case COLOR_3:

			Settings.System.putInt(context.getContentResolver(),
					"tweaks_signal_text_color_3", color);
			broadcastColorChange();
			break;

		case COLOR_4:

			Settings.System.putInt(context.getContentResolver(),
					"tweaks_signal_text_color_4", color);
			broadcastColorChange();
			break;

		case COLOR_STATIC:

			Settings.System.putInt(context.getContentResolver(),
					"tweaks_signal_text_color", color);
			broadcastColorChange();
			break;
		}

		// sendTimeIntent();
	}

	public void onCancel(AmbilWarnaDialog dialog) {
		// cancel was selected by the user
	}
	
	public void broadcastColorChange() {
		Intent i = new Intent();
		i.setAction(ConnectivityManager.SIGNAL_ICON_ACTION);
		sendBroadcast(i);
		sendBroadcast(i);
	}

	public void refreshOptions() {
		if (((CheckBoxPreference) findPreference("signal_automatically_color_pref"))
				.isChecked()) {
			findPreference("signal_color_static").setEnabled(false);
			findPreference("signal_color_0").setEnabled(true);
			findPreference("signal_color_1").setEnabled(true);
			findPreference("signal_color_2").setEnabled(true);
			findPreference("signal_color_3").setEnabled(true);
			findPreference("signal_color_4").setEnabled(true);
		} else {
			findPreference("signal_color_static").setEnabled(true);
			findPreference("signal_color_0").setEnabled(false);
			findPreference("signal_color_1").setEnabled(false);
			findPreference("signal_color_2").setEnabled(false);
			findPreference("signal_color_3").setEnabled(false);
			findPreference("signal_color_4").setEnabled(false);
		}
	}
}
