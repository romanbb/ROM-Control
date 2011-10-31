/*****************************************************************************************************
/
/
/  SGS Style Music Widget:
/     -REQUIRES: CM Music Player and MediaProvider
/
/  This was modeled from the SGS2 Music widget
/
/  Converted/Written By: Scott Brissenden
 *******************************************************************************************************/

package com.android.internal.policy.impl;

import java.lang.Object;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.*;
import android.view.*;
import com.android.internal.policy.impl.KeyguardScreenCallback;
import com.android.internal.policy.impl.KeyguardViewMediator;
import android.content.ContentResolver;
import android.os.Handler;
import android.net.Uri;
import android.view.View;
import android.view.View.OnTouchListener;
import com.android.internal.policy.impl.KeyguardUpdateMonitor;
import android.content.Context;
import android.view.LayoutInflater;
import android.content.IntentFilter;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.content.ContentUris;
import android.content.Intent;
import android.os.SystemClock;
import android.media.AudioManager;
import android.content.BroadcastReceiver;
import android.view.animation.*;
import android.content.res.Configuration;
import java.util.Random;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.xmlpull.v1.XmlPullParser;

public class MusicWidget extends RelativeLayout {

    private static final String TAG = "MusicWidget";
    private static final String INTENT_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    private static final String INTENT_SCREEN_ON = "android.intent.action.SCREEN_ON";
    private static final String INTENT_META_CHANGED = "com.android.music.metachanged";

    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final int R_id_musicwidget_root = 0x10202a7;
    private static final int R_id_MusicInfoTextInMax = 0x10202ac;
    private static final int R_id_MusicInfoTextInMin = 0x10202b1;
    private static final int R_id_MaxMusicController = 0x10202a8;
    private static final int R_id_BoxInMaxMusicController = 0x10202aa;
    private static final int R_id_MinMusicControllerHandle = 0x10202a9;
    private static final int R_id_AlbumArtWithImage = 0x10202ab;
    private static final int R_layout_keyguard_screen_media_controller = 0x1090091;
    private static final int R_id_MusicBoxForward = 0x10202eb;
    private static final int R_id_MusicBoxPlay = 0x10202ec;
    private static final int R_id_MusicBoxPause = 0x10202ed;
    private static final int R_id_MusicBoxRewind = 0x10202ee;
    private static final int R_drawable_unlock_music_bg_bottom = 0x10803d5;
    private static final int R_drawable_unlock_music_bg_top = 0x10803d6;
    private static final int R_drawable_unlock_music_play_grid_thumb_01 = 0x10803da;
    private static final int R_drawable_unlock_music_play_grid_thumb_02 = 0x10803db;
    private static final int R_drawable_unlock_music_play_grid_thumb_03 = 0x10803dc;
    private static final int R_drawable_unlock_music_play_grid_thumb_04 = 0x10803dd;
    private static final int R_drawable_unlock_music_play_grid_thumb_05 = 0x10803de;
    private static final int R_drawable_unlock_music_play_grid_thumb_06 = 0x10803df;
    private static final int R_drawable_unlock_music_play_grid_thumb_07 = 0x10803e0;
    private static final int R_drawable_unlock_music_play_grid_thumb_08 = 0x10803e1;
    private static final int R_drawable_unlock_music_sound_bg = 1080422;
    private static final int R_drawable_unlock_music_sound_bg_f = 0x1080457;
    private static final int R_drawable_unlock_music_sound_bg_p = 0x1080458;

    private final boolean DEBUG = true;
    private final int MSG_MEDIA_UPDATE = 0x12c2;
    private final int SHRINK_ANIMATION_START = 0x12c1;
    private final int STOP_MARQUEE = 0x12c4;
    private final int STOP_MARQUEE_DELAY = 0x3e8;
    private final int STREAMING_MEDIA_UPDATE = 0x12c3;
    private Long currentAlbumID;
    private String currentArtist;
    private String currentTitle;
    private boolean isMaxLayout;
    private boolean isTopLayout;
    private ImageView mAlbumArtWithImage;
    private Bitmap mAlbumArtBitmap;
    private ImageButton mBeforeButton;
    private LinearLayout mBoxInMaxMusicController;
    private BroadcastReceiver mBroadcastReceiver;
    private KeyguardScreenCallback mCallback;
    private final ContentResolver mContentResolver;
    private boolean mFFLongPressed;
    private Handler mHandler;
    private int mHeightDifference;
    private boolean mIsMoving = false;
    private boolean mIsPlaying = false;
    private RelativeLayout mMainLayout;
    private FrameLayout mMaxMusicController;
    private Uri mMediaUri;
    private LinearLayout mMinMusicControllerHandle;
    private TextView mMusicInfoTextInMax;
    private TextView mMusicInfoTextInMin;
    private ImageButton mNextButton;
    private ImageButton mPlayButton;
    private ImageButton mPauseButton;
    private boolean mREWLongPressed;
    private boolean mScreenOn = false;
    private int mServiceNumber;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private boolean mVisibleLayout = false;
    private boolean mWasShowed = false;
    OnTouchListener mTouchListener;
    private AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    Random generator = new Random();
    Context mContext;

    private boolean mSgsMusicControls = (Settings.System.getInt(getContext().getContentResolver(),
            "lockscreen_sgsmusic_controls", 1) == 1);

    private boolean mAlwaysSgsMusicControls = (Settings.System.getInt(getContext().getContentResolver(),
            "lockscreen_always_sgsmusic_controls", 0) == 1);

    public MusicWidget(Context context, KeyguardScreenCallback callback, KeyguardUpdateMonitor updateMonitor) {
        super(context);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case STOP_MARQUEE:
                        handleStopMarquee();
                    case MSG_MEDIA_UPDATE:
                        handleMediaUpdate();
                        resetMinTimer();
                    case STREAMING_MEDIA_UPDATE:
                        handleMediaUpdate();
                    case SHRINK_ANIMATION_START:
                        setMinLayout();
                        break;
                }
            }
        };

        mCallback = callback;
        mUpdateMonitor = updateMonitor;
        mContentResolver = context.getContentResolver();
        mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R_layout_keyguard_screen_media_controller, this, true);
        mMainLayout = (RelativeLayout) findViewById(R_id_musicwidget_root);
        IntentFilter filter = new IntentFilter();

        filter.addAction(INTENT_META_CHANGED);
        filter.addAction(INTENT_SCREEN_OFF);
        filter.addAction(INTENT_SCREEN_ON);

        mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.e("LockScreenMusicWidget", new StringBuilder().append("onReceive() : intent=").append(intent).toString());
                String action = intent.getAction();
                if (INTENT_SCREEN_ON.equals(action)) {
                    mScreenOn = true;
                } else if (INTENT_SCREEN_OFF.equals(action)) {
                    mScreenOn = false;
                } else if (INTENT_META_CHANGED.equals(action)) {
                    Message msg = mHandler.obtainMessage(MSG_MEDIA_UPDATE);
                    mHandler.sendMessage(msg);
                }
            }
        };

        context.registerReceiver(mBroadcastReceiver, filter);

        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    private void addMinTimer() {
        if (isMaxLayout) {
            Message msg = mHandler.obtainMessage(SHRINK_ANIMATION_START);
            mHandler.sendMessageDelayed(msg, 0xdac);
        }
    }

    private void init() {
        mMusicInfoTextInMax = (TextView) findViewById(R_id_MusicInfoTextInMax);
        mMusicInfoTextInMax.setMarqueeRepeatLimit(-0x1);
        mMusicInfoTextInMax.setSelected(true);

        mMusicInfoTextInMin = (TextView) findViewById(R_id_MusicInfoTextInMin);
        mMusicInfoTextInMin.setMarqueeRepeatLimit(-0x1);
        mMusicInfoTextInMin.setSelected(true);
        mMaxMusicController = (FrameLayout) findViewById(R_id_MaxMusicController);
        mBoxInMaxMusicController = (LinearLayout) findViewById(R_id_BoxInMaxMusicController);
        mMinMusicControllerHandle = (LinearLayout) findViewById(R_id_MinMusicControllerHandle);
        mAlbumArtWithImage = (ImageView) findViewById(R_id_AlbumArtWithImage);

        mMainLayout.setSoundEffectsEnabled(true);

        mMainLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                mCallback.pokeWakelock();
                resetMinTimer();
            }
        });

        mTouchListener = new OnTouchListener() {
            int mMusicMovingBoxOrinalY;
            int mMusicMovingBoxCurrentMarginTop = -mHeightDifference;
            int mMusicMovingBoxCurrentMarginBottom = mHeightDifference;
            int mMusicMovingBoxOffset = 0;

            public boolean onTouch(View v, MotionEvent event) {
                int rawY = (int) event.getY();
                mCallback.pokeWakelock();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mMusicMovingBoxOrinalY = rawY;
                    case MotionEvent.ACTION_DOWN:
                        mMusicMovingBoxOffset = mMusicMovingBoxOrinalY - rawY;
                        if (isTopLayout) {
                            if (mMusicMovingBoxOffset > 0 && mMusicMovingBoxOffset < mHeightDifference) {
                                startTranslateAnimation(mMusicMovingBoxCurrentMarginTop, -mHeightDifference + mMusicMovingBoxOffset, 0x32);
                                mMusicMovingBoxCurrentMarginTop = -mHeightDifference + mMusicMovingBoxOffset;
                            } else if (mMusicMovingBoxOffset > mHeightDifference) {
                                startTranslateAnimation(mMusicMovingBoxCurrentMarginTop, 0x0, 0x32);
                                mMusicMovingBoxCurrentMarginTop = 0x0;
                            }
                        } else if (mMusicMovingBoxOffset < 0 && -mHeightDifference > mMusicMovingBoxOffset) {
                            startTranslateAnimation(mMusicMovingBoxCurrentMarginBottom, mHeightDifference + mMusicMovingBoxOffset, 0x32);
                            mMusicMovingBoxCurrentMarginBottom = mHeightDifference + mMusicMovingBoxOffset;
                        } else if (mMusicMovingBoxOffset < mHeightDifference) {
                            startTranslateAnimation(mMusicMovingBoxCurrentMarginBottom, 0x0, 0x32);
                        }
                    case MotionEvent.ACTION_MOVE:
                        if (isTopLayout) {
                            if (mMusicMovingBoxCurrentMarginTop < 0 - mHeightDifference / 0x2) {
                                startTranslateAnimation(mMusicMovingBoxCurrentMarginTop, 0 - mHeightDifference, 0xc8);
                                setMaxLayout(mMusicMovingBoxCurrentMarginTop);
                            } else {
                                setMaxLayout(mMusicMovingBoxCurrentMarginTop);
                                mMusicMovingBoxCurrentMarginTop = 0 - mHeightDifference;
                            }
                        } else if (mMusicMovingBoxCurrentMarginBottom > mHeightDifference / 0x2) {
                            startTranslateAnimation(mMusicMovingBoxCurrentMarginBottom, mHeightDifference, 0xc8);
                            mMusicMovingBoxCurrentMarginBottom = mHeightDifference;
                            setMaxLayout(mMusicMovingBoxCurrentMarginBottom);
                        } else {
                            setMaxLayout(mMusicMovingBoxCurrentMarginBottom);
                            mMusicMovingBoxCurrentMarginBottom = mHeightDifference;
                        }
                }
                return false;
            }
        };

        mNextButton = (ImageButton) findViewById(R_id_MusicBoxForward);
        mPlayButton = (ImageButton) findViewById(R_id_MusicBoxPlay);
        mPauseButton = (ImageButton) findViewById(R_id_MusicBoxPause);
        mBeforeButton = (ImageButton) findViewById(R_id_MusicBoxRewind);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.pokeWakelock();
                sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
                resetMinTimer();
                handleMediaUpdate();
            }
        });

        mBeforeButton.setOnClickListener(new View.OnClickListener() { // $8
                    public void onClick(View v) {
                        mCallback.pokeWakelock();
                        sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        resetMinTimer();
                        handleMediaUpdate();
                    }
                });

        mPauseButton.setOnClickListener(new View.OnClickListener() { // $9
                    public void onClick(View v) {
                        mCallback.pokeWakelock();
                        if (am.isMusicActive()) {
                            mPauseButton.setVisibility(View.GONE);
                            mPlayButton.setVisibility(View.VISIBLE);
                            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                            resetMinTimer();
                        }
                    }
                });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCallback.pokeWakelock();
                if (!am.isMusicActive()) {
                    mPauseButton.setVisibility(View.VISIBLE);
                    mPlayButton.setVisibility(View.GONE);
                    sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                    resetMinTimer();
                    Message msg = mHandler.obtainMessage(MSG_MEDIA_UPDATE);
                    mHandler.sendMessage(msg);
                }
            }
        });

        resetMinTimer();
        if (!am.isMusicActive()) {
            mPauseButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        }
        if (mAlwaysSgsMusicControls) {
            mMaxMusicController.setVisibility(View.VISIBLE);
            mVisibleLayout = true;
            handleMediaUpdate();
        } else if (mSgsMusicControls && am.isMusicActive()) {
            mMaxMusicController.setVisibility(View.VISIBLE);
            mVisibleLayout = true;
            handleMediaUpdate();
        } else if (mSgsMusicControls) {
            mVisibleLayout = true;
        } else {
            mVisibleLayout = false;
        }
    }

    private void removeMinTimer() {
        if (mHandler.hasMessages(SHRINK_ANIMATION_START)) {
            mHandler.removeMessages(SHRINK_ANIMATION_START);
        }
    }

    private void resetMinTimer() {
        removeMinTimer();
        addMinTimer();
    }

    private void setMaxLayout(int value) {
        if (!mIsMoving) {
            mMinMusicControllerHandle.setOnTouchListener(null);
            mBoxInMaxMusicController.setEnabled(true);
            mBoxInMaxMusicController.setVisibility(View.VISIBLE);
        }
        if (isTopLayout) {
            startTranslateAnimation(value, 0x0, 0x190);
        } else {
            startTranslateAnimation(value, 0x0, 0x190);
        }
        mMusicInfoTextInMin.setVisibility(View.INVISIBLE);
        mMusicInfoTextInMax.setVisibility(View.VISIBLE);
        mMusicInfoTextInMax.setSelected(true);
        mMusicInfoTextInMax.invalidate();
        isMaxLayout = true;
        resetMinTimer();
        refreshMusicStatus();
    }

    private void setMinLayout() {
        if (!mIsMoving) {
            isMaxLayout = false;
            mMinMusicControllerHandle.setOnTouchListener(mTouchListener);
            mBoxInMaxMusicController.setVisibility(View.GONE);
            mBoxInMaxMusicController.setEnabled(false);
            mBoxInMaxMusicController.setPressed(false);
        }
        if (mMaxMusicController != null && mMinMusicControllerHandle != null) {
            mHeightDifference = mMaxMusicController.getHeight() - mMinMusicControllerHandle.getHeight();
        }
        if (isTopLayout) {
            startTranslateAnimation(0x0, -mHeightDifference, 0xc8);
        } else {
            startTranslateAnimation(0x0, mHeightDifference, 0xc8);
        }
        mMusicInfoTextInMax.setVisibility(View.INVISIBLE);
        mMusicInfoTextInMax.setSelected(true);
        mMusicInfoTextInMin.setVisibility(View.VISIBLE);
        mMusicInfoTextInMin.setSelected(true);
        mMusicInfoTextInMin.invalidate();
    }

    private void startTranslateAnimation(int x, int y, int duration) {
        if (!mIsMoving) {
            Animation animation = new TranslateAnimation(0x0, 0x0, (float) x, (float) y);
            animation.setDuration((long) duration);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setFillAfter(true);
            mMaxMusicController.startAnimation(animation);
        }
    }

    public void cleanUp() {
        mCallback = null;
        getContext().unregisterReceiver(mBroadcastReceiver);
        mMusicInfoTextInMax.setSelected(false);
        mMusicInfoTextInMin.setSelected(false);
        stopMarquee();
    }

    protected void handleMediaUpdate() {
        if (am.isMusicActive()) {
            mIsPlaying = true;
        } else {
            mIsPlaying = false;
        }

        if (mIsPlaying) {
            mMaxMusicController.setVisibility(View.VISIBLE);
            mVisibleLayout = true;
        } else if (!isMaxLayout) {
            mMaxMusicController.setVisibility(View.GONE);
            mVisibleLayout = false;
        }

        updateMediaPlayer();
    }

    public void handleStopMarquee() {

        if (mMusicInfoTextInMax != null) {
            mMusicInfoTextInMax.setSelected(false);
        } else if (mMusicInfoTextInMin == null) {
            mMusicInfoTextInMin.setSelected(false);
        }
    }

    public boolean isControllerShowing() {
        if (mAlwaysSgsMusicControls || am.isMusicActive()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);

        Log.d("LockScreenMusicWidget", "onKeyDown");

        if (keyCode == 0x3) {
            if (event.getRepeatCount() == 0) {
                startControllerAnimation();
                Log.d("LockScreenMusicWidget", "startControllerAnimation()");
            }
        } else if (keyCode == 0x18) {
            Log.d("LockScreenMusicWidget", "KEYCODE_VOLUME_UP()");
        } else if (keyCode == 0x19) {
            Log.d("LockScreenMusicWidget", "KEYCODE_VOLUME_DOWN()");
        } else {
            return false;
        }
        return true;
    }

    public void onPause() {
        mBoxInMaxMusicController.setEnabled(false);
        mMusicInfoTextInMax.setEnabled(false);
        mMusicInfoTextInMin.setEnabled(false);
        stopMarquee();
    }

    public void onResume() {
        Log.d("LockScreenMusicWidget",
                new StringBuilder().append("onResume() isTopLayout=").append(isTopLayout).append(", isMaxLayout=").append(isMaxLayout).toString());
        mHeightDifference = mMaxMusicController.getHeight() - mMinMusicControllerHandle.getHeight();
        Log.d("LockScreenMusicWidget", new StringBuilder().append("mHeightDifference = ").append(mHeightDifference).toString());
        mMusicInfoTextInMax.setSelected(false);
        mMusicInfoTextInMin.setSelected(false);

        String currentTitle = KeyguardViewMediator.TrackId();
        String currentArtist = KeyguardViewMediator.Artist();

        CharSequence newTitleText = new StringBuilder().append(currentTitle).append(" / ").append(currentArtist).toString();

        if (currentTitle == null && currentArtist == null) {
            mMusicInfoTextInMax.setText("");
            mMusicInfoTextInMin.setText("");
        } else {
            mMusicInfoTextInMax.setText(newTitleText);
            mMusicInfoTextInMin.setText(newTitleText);
            mMusicInfoTextInMax.setEnabled(true);
            mMusicInfoTextInMin.setEnabled(true);
        }

        if (isMaxLayout) {
            mMusicInfoTextInMax.setSelected(true);
        } else {
            mMusicInfoTextInMin.setSelected(true);
        }

        if (isTopLayout) {
            setMaxLayout(-(mHeightDifference));
        } else {
            setMaxLayout(mHeightDifference);
        }
        refreshMusicStatus();
        updateMediaPlayer();
    }

    private void refreshMusicStatus() {
        if (am.isMusicActive()) {
            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.GONE);
        } else {
            mPlayButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.GONE);
        }
    }

    public void setBottomLayout() {
        isTopLayout = false;
        mMaxMusicController.setBackgroundResource(R_drawable_unlock_music_bg_bottom);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mMinMusicControllerHandle.getLayoutParams();
        lp.gravity = Gravity.BOTTOM;
        mMinMusicControllerHandle.setLayoutParams(lp);
        lp = (FrameLayout.LayoutParams) mMusicInfoTextInMin.getLayoutParams();
        lp.gravity = Gravity.TOP;
        mMusicInfoTextInMin.setLayoutParams(lp);
    }

    public void setControllerVisibility(boolean bIsVisible, boolean isMusicWidgetShowed) {
        Log.d("LockScreenMusicWidget",
                new StringBuilder().append("setControllerVisibility() : bIsVisible=").append(bIsVisible).append(", mVisibleLayout=")
                        .append(mVisibleLayout).toString());

        if (!bIsVisible) {
            mIsMoving = true;
        } else {
            mIsMoving = false;
        }
        if (bIsVisible) {
            if (mAlwaysSgsMusicControls) {
                if (!mVisibleLayout) {
                    mMaxMusicController.setVisibility(View.VISIBLE);
                    mVisibleLayout = true;
                } else {
                    addMinTimer();
                }
            }
        } else {
            mWasShowed = isMusicWidgetShowed;
            if (mVisibleLayout) {
                mMaxMusicController.setVisibility(View.GONE);
                mVisibleLayout = false;
            } else {
                removeMinTimer();
            }
        }
    }

    public void setScreenOff() {
        mScreenOn = false;
    }

    public void setScreenOn() {
        mScreenOn = true;
    }

    public void setTopLayout() {
        isTopLayout = true;
        mMaxMusicController.setBackgroundResource(R_drawable_unlock_music_bg_top);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mMinMusicControllerHandle.getLayoutParams();
        lp.gravity = Gravity.TOP;
        ;
        mMinMusicControllerHandle.setLayoutParams(lp);
        lp = (FrameLayout.LayoutParams) mMusicInfoTextInMin.getLayoutParams();
        lp.gravity = Gravity.BOTTOM;
        mMusicInfoTextInMin.setLayoutParams(lp);
    }

    public void startControllerAnimation() {
        Log.d("LockScreenMusicWidget", "startControllerAnimation()");

        removeMinTimer();
        if (isMaxLayout) {
            setMinLayout();
        } else if (isTopLayout) {
            setMaxLayout(-(mHeightDifference));
        } else {
            setMaxLayout(mHeightDifference);
        }
    }

    public void stopMarquee() {

        if (mHandler.hasMessages(STOP_MARQUEE)) {
            mHandler.removeMessages(STOP_MARQUEE);
        } else {
            Message msg = mHandler.obtainMessage(STOP_MARQUEE);
            mHandler.sendMessageDelayed(msg, 0x3e8);
        }
    }

    private void setRandomAlbumCover() {
        int[] randomCovers = {
                R_drawable_unlock_music_play_grid_thumb_01,
                R_drawable_unlock_music_play_grid_thumb_02,
                R_drawable_unlock_music_play_grid_thumb_03,
                R_drawable_unlock_music_play_grid_thumb_04,
                R_drawable_unlock_music_play_grid_thumb_05,
                R_drawable_unlock_music_play_grid_thumb_06,
                R_drawable_unlock_music_play_grid_thumb_07,
                R_drawable_unlock_music_play_grid_thumb_08,
                R_drawable_unlock_music_sound_bg,
                R_drawable_unlock_music_sound_bg_f,
                R_drawable_unlock_music_sound_bg_p
        };
        int i = generator.nextInt(randomCovers.length);
        mAlbumArtWithImage.setImageResource(randomCovers[i]);
    }

    private void updateMediaPlayer() {
        String currentTitle = KeyguardViewMediator.TrackId();
        String currentArtist = KeyguardViewMediator.Artist();
        CharSequence nowPlaying = new StringBuilder().append(currentArtist).append(" / ").append(currentTitle).toString();
        if (currentTitle == null && currentArtist == null) {
            mMusicInfoTextInMax.setText("");
            mMusicInfoTextInMin.setText("");
            mMusicInfoTextInMax.setSelected(false);
            mMusicInfoTextInMin.setSelected(false);
        } else {
            mMusicInfoTextInMax.setText(nowPlaying);
            mMusicInfoTextInMin.setText(nowPlaying);
            if (isMaxLayout) {
                mMusicInfoTextInMax.setSelected(true);
            } else {
                mMusicInfoTextInMin.setSelected(true);
            }
            Uri uri = getArtworkUri(mContext, KeyguardViewMediator.SongId(), KeyguardViewMediator.AlbumId());
            if (uri != null) {
                mAlbumArtWithImage.setImageURI(uri);
            } else {
                setRandomAlbumCover();
            }
        }
    }

    public void onMusicChanged() {
        updateMediaPlayer();
    }

    private void sendMediaButtonEvent(int code) {
        long eventtime = SystemClock.uptimeMillis();

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        getContext().sendOrderedBroadcast(downIntent, null);

        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, code, 0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        getContext().sendOrderedBroadcast(upIntent, null);
    }

    public static Uri getArtworkUri(Context context, long song_id, long album_id) {
        Log.d("MusicWidget", new StringBuilder().append("getArtworkFromFile: AlbumID: ").append(album_id).toString());
        if (album_id < 0) {
            // This is something that is not in the database, so get the album
            // art directly
            // from the file.
            if (song_id >= 0) {
                return getArtworkUriFromFile(context, song_id, -1);
            }
            return null;
        }

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return uri;
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the
                // user deleted it, or
                // maybe it never existed to begin with.
                return getArtworkUriFromFile(context, song_id, album_id);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }

    private static Uri getArtworkUriFromFile(Context context, long songid, long albumid) {
        Log.d("MusicWidget", new StringBuilder().append("getArtworkUriFromFile: ").append(songid).toString());
        if (albumid < 0 && songid < 0) {
            return null;
        }

        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    return uri;
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    return uri;
                }
            }
        } catch (FileNotFoundException ex) {
            //
        }
        return null;
    }

    protected void onDestroy() {
        onDestroy();

        unbindDrawables(findViewById(R_id_musicwidget_root));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
