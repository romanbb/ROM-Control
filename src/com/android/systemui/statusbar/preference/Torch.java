
package com.android.systemui.statusbar.preference;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.view.View;

public class Torch extends StatusBarPreference {

    public static String LED_ON = "Torch is ON";
    public static String LED_OFF = "Torch is OFF";

    Handler handler = new Handler();
    Camera mCamera;

    public Torch(Context c, View v) {
        super(c, v);
        init();
    }

    private void init() {

        mCheckBox.setChecked(false);
        mSummary.setText(LED_OFF);
        mIcon.setImageResource(0x7f020170);
        mTitle.setText("LED Torch");

        mContentView.setOnClickListener(mClickListener);

    }

    public void onStart() {

    }

    public void onStop() {

    }

    public void updateResources() {

    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            if (mCheckBox.isChecked()) {

                mCheckBox.setChecked(false);
                mSummary.setText(LED_OFF);
                mIcon.setImageResource(0x7f020170);
                mTorchStopTask.run();

            } else {

                mCheckBox.setChecked(true);
                mSummary.setText(LED_ON);
                mIcon.setImageResource(0x7f02016f);
                mTorchStartTask.run();

            }
        }
    };

    private Runnable mTorchStartTask = new Runnable() {
        public void run() {
            try {
                mCamera = Camera.open();
            } catch (RuntimeException e) {
                mSummary.setText("ERROR: Couldn't connect to camera");
                mCheckBox.setChecked(false);
                mSummary.setText(LED_OFF);
                mIcon.setImageResource(0x7f020170);
                return;
            }
            if (mCamera != null) {
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(params);
                // mCamera.release();
            }
        }
    };

    private Runnable mTorchStopTask = new Runnable() {

        public void run() {
            // try {
            // mCamera = Camera.open();
            // } catch (RuntimeException e) {
            // mSummary.setText("ERROR: Couldn't connect to camera");
            // mCheckBox.setChecked(true);
            // mIcon.setImageResource(0x7f02016f);
            // return;
            // }

            if (mCamera != null) {
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                mCamera.release();
            }

        }
    };
}
