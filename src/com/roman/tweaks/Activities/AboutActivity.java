package com.roman.tweaks.Activities;

import com.roman.edt.tweaks.R;
import com.roman.edt.tweaks.R.xml;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class AboutActivity extends PreferenceActivity {

	public void onCreate(Bundle ofLove) {
		super.onCreate(ofLove);
		addPreferencesFromResource(R.xml.about_prefs);

		/*
		 * findPreference("about_edtsite").setOnPreferenceClickListener( new
		 * OnPreferenceClickListener() { public boolean
		 * onPreferenceClick(Preference preference) { Intent browserIntent = new
		 * Intent( "android.intent.action.VIEW",
		 * Uri.parse("http://edtdev.com/forum/forum/40-project-v/"));
		 * startActivity(browserIntent); return true; } });
		 * 
		 * findPreference("about_xda").setOnPreferenceClickListener( new
		 * OnPreferenceClickListener() { public boolean
		 * onPreferenceClick(Preference preference) { Intent browserIntent = new
		 * Intent( "android.intent.action.VIEW",
		 * Uri.parse("http://forum.xda-developers.com/showthread.php?t=1105037"
		 * )); startActivity(browserIntent); return true; } });
		 */

		findPreference("about_jelly_twitter").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent browserIntent = new Intent(
								"android.intent.action.VIEW", Uri
										.parse("http://twitter.com/sxeweb"));
						startActivity(browserIntent);
						return true;
					}
				});

		findPreference("about_comrade_twitter").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent browserIntent = new Intent(
								"android.intent.action.VIEW", Uri
										.parse("http://twitter.com/romanbb"));
						startActivity(browserIntent);
						return true;
					}
				});

		findPreference("about_jonny_twitter").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent browserIntent = new Intent(
								"android.intent.action.VIEW", Uri
										.parse("http://twitter.com/whitehawkx"));
						startActivity(browserIntent);
						return true;
					}
				});

		findPreference("about_dan_twitter").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent browserIntent = new Intent(
								"android.intent.action.VIEW",
								Uri.parse("http://twitter.com/dan_brutal_edt"));
						startActivity(browserIntent);
						return true;
					}
				});

		findPreference("about_beast_twitter").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent browserIntent = new Intent(
								"android.intent.action.VIEW", Uri
										.parse("http://twitter.com/cwmenard"));
						startActivity(browserIntent);
						return true;
					}
				});
	}
}
