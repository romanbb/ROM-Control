package com.roman.tweaks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.roman.tweaks.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.roman.tweaks.ColorPickerDialog.OnColorChangedListener;

public class Main extends PreferenceActivity {
	String pref;
	public static final int SELECT_PHOTO = 1;
	public static final int SELECT_ACTIVITY = 2;
	public static final int PHOTO_PICKED = 3;
	private static final String LOCKSCREEN_WALLPAPER_LOCATION = "/lockscreen_wallpaper/edt_wallpaper.jpg";
	Context context;
	Dialog d;
	AlertDialog.Builder builder;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this.getApplicationContext();
		addPreferencesFromResource(R.xml.main_prefs);
		d = new Dialog(this);
		builder = new AlertDialog.Builder(this);

		/*
		 * ampm options
		 */
		Preference clock_am_pm = (Preference) findPreference("clock_am_pm");

		try {
			Settings.System.getInt(getContentResolver(), "clock_am_pm_style");

		} catch (SettingNotFoundException e) {
			Settings.System
					.putInt(getContentResolver(), "clock_am_pm_style", 0);
		}

		clock_am_pm
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						Integer selection = Integer.parseInt(newValue
								.toString());

						Settings.System.putInt(getContentResolver(),
								"clock_am_pm_style", selection);

						Intent timeIntent = new Intent();
						timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
						sendBroadcast(timeIntent);
						return true;
					}
				});



		/*
		 * read ahead setter
		 */
		ListPreference read_ahead_pref = (ListPreference) findPreference("read_ahead_pref");

		String readAhead = "";
		if (ShellInterface.isSuAvailable()) {
			readAhead = ShellInterface
					.getProcessOutput("more /sys/devices/virtual/bdi/179:0/read_ahead_kb");
		}
		read_ahead_pref.setSummary(readAhead);

		if (read_ahead_pref.getEntry() == null) {
			read_ahead_pref.setValueIndex(4);
		}
		// read_ahead_pref.setValueIndex()

		read_ahead_pref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (ShellInterface.isSuAvailable()) {
							ShellInterface
									.runCommand("/system/xbin/echo "
											+ newValue
											+ " > /sys/devices/virtual/bdi/179:0/read_ahead_kb");
						}

						preference.setSummary(newValue.toString());
						return true;
					}
				});

	

		findPreference("battery_options_pref").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent i = new Intent(context, BatteryActivity.class);
						startActivity(i);
						return true;
					}
				});

		/*
		 * about preference
		 */
		findPreference("about_pref").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent i = new Intent(context, AboutActivity.class);
						startActivity(i);
						return true;
					}
				});
		
		/*
		 * about preference
		 */
		findPreference("signal_pref").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent i = new Intent(Main.this, SignalActivity.class);
						Main.this.startActivity(i);
						return true;
					}
				});

		

		/*
		 * color picker
		 */
		// initialColor is the initially-selected color to be shown in the
		// rectangle on the left of the arrow.
		// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware
		// of the initial 0xff which is the alpha.

		final AmbilWarnaDialog colorPickerDialog = new AmbilWarnaDialog(this,
				0xffffffff, new OnAmbilWarnaListener() {
					public void onOk(AmbilWarnaDialog dialog, int color) {
						Settings.System.putInt(context.getContentResolver(),
								"clock_color", color);

						Intent timeIntent = new Intent();
						timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
						sendBroadcast(timeIntent);
						sendTimeIntent();

						String stringColor = color + "";
						findPreference("clock_color_pref").setSummary(
								stringColor);
					}

					public void onCancel(AmbilWarnaDialog dialog) {
						// cancel was selected by the user
					}
				});

		((Preference) findPreference("clock_color_pref"))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						// Intent i = new Intent(context,
						// ColorPickerDialog.class);
						colorPickerDialog.show();
						return true;
					}
				});

		// run at the end
		refreshLockscreenPreferences();
	}

	public void refreshLockscreenPreferences() {

		// Preference lock_screen_wallpaper_pref = (Preference)
		// findPreference("lock_screen_wallpaper_pref");

		// int key = Settings.System.getInt(getContentResolver(),
		// "lockscreen_type_key", 0);

		/*
		 * if (key == 0 || key == 7) {
		 * findPreference("custom_app_pref").setEnabled(true); } else {
		 * findPreference("custom_app_pref").setEnabled(false); }
		 */


		// no lockscreen option
		// if (key == 9) {
		// findPreference("lockscreen_timeout_pref").setEnabled(false);
		// findPreference("lockscreen_delay_behavior").setEnabled(false);
		// findPreference("lockscreen_delay_timeout_pref").setEnabled(false);
		// findPreference("lockscreen_selection_pref").setSummary(
		// "Lockscreen disabled");
		//
		// } else {
		// findPreference("lockscreen_timeout_pref").setEnabled(true);
		// findPreference("lockscreen_delay_behavior").setEnabled(true);
		// findPreference("lockscreen_delay_timeout_pref").setEnabled(true);
		// }

		// refresh delay behavior summary

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	/**
	 * context menus
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.restore_default:
			File f = new File("/mnt/sdcard/" + LOCKSCREEN_WALLPAPER_LOCATION);
			f.delete();
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * handle what to do with the picked activity
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PHOTO) {

				// make the lockscreen directory
				new File("/sdcard/lockscreen_wallpaper/").mkdir();

				// make a .nomedia file
				try {
					new File("/sdcard/lockscreen_wallpaper/.nomedia")
							.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				File file = touchLockscreenFile();
				String filePath = file.getPath();

				Bitmap selectedImage = BitmapFactory.decodeFile(filePath);

				try {
					Uri uri = Uri.fromFile(file);
					OutputStream outStream = context.getContentResolver()
							.openOutputStream(uri);
					selectedImage.compress(Bitmap.CompressFormat.JPEG, 100,
							outStream);
					outStream.flush();
					outStream.close();
					Log.e("EDT", "done");

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (requestCode == SELECT_ACTIVITY) {
				// launch the application that we just picked
				// startActivity(data);

				PackageManager pm = getPackageManager();
				ResolveInfo ac = pm.resolveActivity(data,
						PackageManager.MATCH_DEFAULT_ONLY);

				String appName = ac.loadLabel(pm).toString();

				String uri = data.toUri(Intent.URI_INTENT_SCHEME);
				uri = uri.substring(7, uri.length());

				Settings.System.putString(getContentResolver(),
						"custom_edt_app_name", appName);
				Settings.System.putString(getContentResolver(),
						"custom_edt_app_intent", uri);
			}
			return;
		} else {
			return;
		}
	}

	public void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else {
			Toast.makeText(getApplicationContext(),
					"Please don't use a file manager to select an image!",
					Toast.LENGTH_SHORT).show();
			return null;
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.pref_menu, menu);
		return true;
	}

	public void onResume() {
		super.onResume();
		// Preference custom_app_preference = (Preference)
		// findPreference("custom_app_pref");
		// custom_app_preference.setSummary(Settings.System.getString(
		// getContentResolver(), "custom_edt_app_name"));
		//
		// Preference lockscreen_timeout_pref = (Preference)
		// findPreference("lockscreen_timeout_pref");
		// int i = (Settings.System.getInt(getContentResolver(),
		// "custom_lockscreen_timeout", 5000) / 1000);
		//
		// lockscreen_timeout_pref.setSummary(i + " seconds");
		// refreshLockscreenPreferences();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.restore_default:
			File f = new File("/mnt/sdcard/" + LOCKSCREEN_WALLPAPER_LOCATION);
			f.delete();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private Uri getLockscreenUri() {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				LOCKSCREEN_WALLPAPER_LOCATION));
	}

	private File touchLockscreenFile() {
		if (isSDCARDMounted()) {

			File f = new File(Environment.getExternalStorageDirectory(),
					LOCKSCREEN_WALLPAPER_LOCATION);
			try {
				f.createNewFile();
			} catch (IOException e) {

			}
			return f;
		} else {
			return null;
		}
	}

	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}

	public class ClockChangeListener implements OnColorChangedListener {

		private Context mContext;
		private int color;

		public ClockChangeListener(Context c) {
			mContext = c;
		}

		public void colorChanged(int color) {

		}

		public void setColor(int c) {
			color = c;
		}

		public int getColor() {
			return color;
		}

	}

	public void sendTimeIntent() {
		Intent timeIntent = new Intent();
		timeIntent.setAction(Intent.ACTION_TIME_CHANGED);
		sendBroadcast(timeIntent);
	}

}