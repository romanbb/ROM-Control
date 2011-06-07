package com.roman.vibrant.edt;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OldIntentPicker extends ListActivity {

	public void onCreate(Bundle ofLove) {
		super.onCreate(ofLove);

//		// get a list of installed apps.
////		PackageManager pm = getPackageManager();
////		List<ApplicationInfo> packages = pm
////				.getInstalledApplications(PackageManager.PERMISSION_GRANTED);
////
////		IntentAdapter adapter = new IntentAdapter(getApplicationContext(),
////				packages);
////		setListAdapter(adapter);
//		
//		// Pick an application
//		
//		
//		// The result is obtained in onActivityResult:
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		   if (data != null) {
		      // launch the application that we just picked
		      startActivity(data);
		   }
		}

	private class IntentAdapter extends ArrayAdapter<ApplicationInfo> {
		ArrayList<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();

		public IntentAdapter(Context context, List<ApplicationInfo> objects) {
			super(context, R.id.name, objects);
			apps = (ArrayList<ApplicationInfo>) objects;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ApplicationInfo app = apps.get(position);
			View v = convertView;

			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.intent_row, parent, false);

			TextView name = (TextView) v.findViewById(R.id.name);
			name.setText(getPackageManager().getApplicationLabel(app));

			ImageView icon = (ImageView) v.findViewById(R.id.icon);
			icon.setImageDrawable(getPackageManager().getApplicationIcon(app));

			LinearLayout layout = (LinearLayout) v.findViewById(R.id.layout);
			layout.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Intent in = getPackageManager().getLaunchIntentForPackage(
							app.packageName);
					Log.e("EDT", in.toUri(Intent.URI_INTENT_SCHEME));

					Settings.System.putString(getContentResolver(),
							"custom_edt_app_name", getPackageManager()
									.getApplicationLabel(app).toString());

					String uri = in.toUri(Intent.URI_INTENT_SCHEME);

					uri = uri.substring(7, uri.length());

					Settings.System.putString(getContentResolver(),
							"custom_edt_app_intent", uri);

					OldIntentPicker.this.finish();
				}
			});
			return v;
		}

	}
}
