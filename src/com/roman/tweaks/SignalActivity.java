
package com.roman.tweaks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.roman.tweaks.AmbilWarnaDialog.OnAmbilWarnaListener;

public class SignalActivity extends PreferenceActivity implements OnPreferenceClickListener,
        OnAmbilWarnaListener, OnPreferenceChangeListener {

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

    // color preference constants
    private static final String PREF_SIGNAL_COLOR_0 = "signal_color_0";

    private static final String PREF_SIGNAL_COLOR_1 = "signal_color_1";

    private static final String PREF_SIGNAL_COLOR_2 = "signal_color_2";

    private static final String PREF_SIGNAL_COLOR_3 = "signal_color_3";

    private static final String PREF_SIGNAL_COLOR_4 = "signal_color_4";

    private static final String PREF_SIGNAL_COLOR_STATIC = "signal_color_static";

    private static final String PREF_AUTO_COLOR = "signal_automatically_color_pref";

    private static final String PREF_SIGNAL_TEXT_STYLE = "signal_text_style_pref";

    private static final String PREF_TOGGLE_4G_ICON = "tweaks_show_4g_icon";

    private static final String PREF_SHOW_SIGNAL_BARS = "show_signal_bars";

    //
    Preference mSignalColor0;

    Preference mSignalColor1;

    Preference mSignalColor2;

    Preference mSignalColor3;

    Preference mSignalColor4;

    Preference mSignalColorStatic;

    CheckBoxPreference mSignalAutoColor;

    CheckBoxPreference mShow4GIcon;

    CheckBoxPreference mShowSignalBars;

    ListPreference mSignalTextStyle;

    public void onCreate(Bundle ofLove) {
        super.onCreate(ofLove);
        context = this.getApplicationContext();
        addPreferencesFromResource(R.xml.signal_prefs);
        signalColorPickerDialog = new AmbilWarnaDialog(this, 0xffffffff, this);

        // assign
        PreferenceScreen prefs = getPreferenceScreen();
        mSignalAutoColor = (CheckBoxPreference) prefs.findPreference(PREF_AUTO_COLOR);
        mSignalTextStyle = (ListPreference) prefs.findPreference(PREF_SIGNAL_TEXT_STYLE);
        mSignalColor0 = prefs.findPreference(PREF_SIGNAL_COLOR_0);
        mSignalColor1 = prefs.findPreference(PREF_SIGNAL_COLOR_1);
        mSignalColor2 = prefs.findPreference(PREF_SIGNAL_COLOR_2);
        mSignalColor3 = prefs.findPreference(PREF_SIGNAL_COLOR_3);
        mSignalColor4 = prefs.findPreference(PREF_SIGNAL_COLOR_4);
        mSignalColorStatic = prefs.findPreference(PREF_SIGNAL_COLOR_STATIC);
        mShow4GIcon = (CheckBoxPreference) prefs.findPreference(PREF_TOGGLE_4G_ICON);
        mShowSignalBars = (CheckBoxPreference) prefs.findPreference(PREF_SHOW_SIGNAL_BARS);

        // check enabled settings
        mShowSignalBars.setChecked((Settings.System.getInt(getContentResolver(),
                "tweaks_show_signal_bars", 1) == 1));
        mSignalAutoColor.setChecked((Settings.System.getInt(getContentResolver(),
                "tweaks_signal_text_autocolor_enable", 0) == 1));
        mShow4GIcon.setChecked((Settings.System.getInt(getContentResolver(), "tweaks_show_4g_icon",
                0) == 1));

    }

    public boolean onPreferenceClick(Preference preference) {

        if (preference == mSignalColor0) {
            signalTextColorPickerFlag = COLOR_0;

            signalColorPickerDialog = new AmbilWarnaDialog(this, Settings.System.getInt(
                    getContentResolver(), "tweaks_signal_text_color_0", 0xFFFFFFFF), this);
            signalColorPickerDialog.show();
            return true;

        } else if (preference == mSignalColor1) {
            signalTextColorPickerFlag = COLOR_1;
            signalColorPickerDialog = new AmbilWarnaDialog(this, Settings.System.getInt(
                    getContentResolver(), "tweaks_signal_text_color_1", 0xFFFFFFFF), this);
            signalColorPickerDialog.show();
            return true;

        } else if (preference == mSignalColor2) {

            signalTextColorPickerFlag = COLOR_2;
            signalColorPickerDialog = new AmbilWarnaDialog(this, Settings.System.getInt(
                    getContentResolver(), "tweaks_signal_text_color_2", 0xFFFFFFFF), this);
            signalColorPickerDialog.show();
            return true;

        } else if (preference == mSignalColor3) {

            signalTextColorPickerFlag = COLOR_3;
            signalColorPickerDialog = new AmbilWarnaDialog(this, Settings.System.getInt(
                    getContentResolver(), "tweaks_signal_text_color_3", 0xFFFFFFFF), this);
            signalColorPickerDialog.show();
            return true;

        } else if (preference == mSignalColor4) {

            signalTextColorPickerFlag = COLOR_4;
            signalColorPickerDialog = new AmbilWarnaDialog(this, Settings.System.getInt(
                    getContentResolver(), "tweaks_signal_text_color_4", 0xFFFFFFFF), this);
            signalColorPickerDialog.show();
            return true;

        } else if (preference == mSignalColorStatic) {

            signalTextColorPickerFlag = COLOR_STATIC;
            signalColorPickerDialog = new AmbilWarnaDialog(this, Settings.System.getInt(
                    getContentResolver(), "tweaks_signal_text_color", 0xFFFFFFFF), this);
            signalColorPickerDialog.show();
            return true;

        } else if (preference == mSignalAutoColor) {
            if (mSignalAutoColor.isChecked()) {
                Settings.System.putInt(getContentResolver(), "tweaks_signal_text_autocolor_enable",
                        1);

            } else {
                Settings.System.putInt(getContentResolver(), "tweaks_signal_text_autocolor_enable",
                        0);

            }

            broadcastSignalChange();
            return true;

        } else if (preference == mShow4GIcon) {
            if (((CheckBoxPreference) preference).isChecked()) {
                Settings.System.putInt(getContentResolver(), "tweaks_show_4g_icon", 1);
            } else {
                Settings.System.putInt(getContentResolver(), "tweaks_show_4g_icon", 0);
            }
            broadcastDataChange();
            return true;

        } else if (preference == mShowSignalBars) {
            if (mShowSignalBars.isChecked()) {
                Settings.System.putInt(getContentResolver(), "tweaks_show_signal_bars", 1);
            } else {
                Settings.System.putInt(getContentResolver(), "tweaks_show_signal_bars", 0);
            }
            broadcastDataChange();
            return true;
        } else if (preference == mSignalTextStyle) {

            preference = (ListPreference) preference;
            Integer val = Integer.parseInt((String) ((ListPreference) preference).getValue());

            Settings.System.putInt(getContentResolver(), "tweaks_signal_text_style", val);

            broadcastSignalChange();
            return true;
        }

        return false;
    }

    public void onOk(AmbilWarnaDialog dialog, int color) {

        switch (signalTextColorPickerFlag) {

            case COLOR_0:

                Settings.System.putInt(context.getContentResolver(), "tweaks_signal_text_color_0",
                        color);
                broadcastSignalChange();
                break;

            case COLOR_1:

                Settings.System.putInt(context.getContentResolver(), "tweaks_signal_text_color_1",
                        color);
                broadcastSignalChange();
                break;

            case COLOR_2:

                Settings.System.putInt(context.getContentResolver(), "tweaks_signal_text_color_2",
                        color);
                broadcastSignalChange();
                break;

            case COLOR_3:

                Settings.System.putInt(context.getContentResolver(), "tweaks_signal_text_color_3",
                        color);
                broadcastSignalChange();
                break;

            case COLOR_4:

                Settings.System.putInt(context.getContentResolver(), "tweaks_signal_text_color_4",
                        color);
                broadcastSignalChange();
                break;

            case COLOR_STATIC:

                Settings.System.putInt(context.getContentResolver(), "tweaks_signal_text_color",
                        color);
                broadcastSignalChange();
                break;
        }

        // sendTimeIntent();
    }

    public void onCancel(AmbilWarnaDialog dialog) {
        // cancel was selected by the user
    }

    public void broadcastSignalChange() {
        Intent i = new Intent();
        i.setAction(CustomIntents.SIGNAL_ICON_ACTION);
        sendBroadcast(i);
        sendBroadcast(i);
    }

    public void broadcastDataChange() {
        Intent i = new Intent();
        i.setAction(CustomIntents.DATA_ICON_ACTION);
        sendBroadcast(i);
        sendBroadcast(i);
    }

    public void refreshOptions() {
        if (((CheckBoxPreference) findPreference("signal_automatically_color_pref")).isChecked()) {
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

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        return false;
    }

}
