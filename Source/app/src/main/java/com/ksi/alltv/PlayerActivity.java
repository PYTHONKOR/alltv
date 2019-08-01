/*
 * MIT License
 *
 * Copyright (c) 2019 PYTHONKOR

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ksi.alltv;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PlayerActivity extends FragmentActivity implements Player.EventListener, Target {

    static boolean active = false;

    private PlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;
    private DataSource.Factory mDataSourceFactory;

    private Animation translateTopAnim;
    private Animation translateBottomAnim;
    private LinearLayout slidingPanel;
    private TextView infoView, timeView;

    private Handler mHandler;
    private Runnable mRunnable;

    private JsonArray mInfoArray;
    private int mInfoIndex;

    private boolean isPageOpen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_videoplayer);

        slidingPanel = (LinearLayout) findViewById(R.id.slidingPanel);
        infoView = (TextView) findViewById(R.id.infoView);
        timeView = (TextView) findViewById(R.id.timeView);

        mPlayerView = findViewById(R.id.video_view);

        mPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());

        mPlayerView.setPlayer(mPlayer);
        mPlayerView.setUseController(false);

        if (BuildConfig.DEBUG) {
            mPlayerView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if (motionEvent.getX() < 100. && motionEvent.getY() < 100.) {

                        new Thread(new Runnable() {
                            public void run() {
                                new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                            }
                        }).start();

                    } else {
                        if (isPageOpen) {
                            hideProgramInfo();
                        } else {
                            showProgramInfo();
                        }
                    }

                    return false;
                }
            });
        }

        mDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getResources().getString(R.string.app_name)));

        ChannelData item = getIntent().getParcelableExtra(getResources().getString(R.string.PLAYCHANNEL_STR));

        String info = getIntent().getStringExtra(getResources().getString(R.string.PLAYINFO_STR));

        infoView.setText("");
        mInfoArray = null;

        if (info != null && info.length() > 0) {
            JsonParser jsonParser = new JsonParser();
            mInfoArray = jsonParser.parse(info).getAsJsonArray();
        } else {
            infoView.setText(item.getProgram());
        }

        Uri uriLive = Uri.parse(item.getVideoUrl());

        HlsMediaSource mediaSourcelive = new HlsMediaSource.Factory(mDataSourceFactory).
                setAllowChunklessPreparation(true).createMediaSource(uriLive);

        if (item.isAudioChannel()) {
            Picasso.get().load(item.getStillImageUrl()).into(this);
        }

        mPlayer.prepare(mediaSourcelive);
        mPlayer.setPlayWhenReady(true);
        mPlayer.addListener(this);

        translateTopAnim = AnimationUtils.loadAnimation(this, R.anim.translate_top);
        translateBottomAnim = AnimationUtils.loadAnimation(this, R.anim.translate_bottom);

        translateTopAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPageOpen) {
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPageOpen = false;
                } else {
                    isPageOpen = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        translateBottomAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPageOpen) {
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPageOpen = false;
                } else {
                    isPageOpen = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                hideProgramInfo();
            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (isPageOpen) {
                hideProgramInfo();
            } else {
                showProgramInfo();
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isPageOpen) {
                hideProgramInfo();
                return false;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

            if (isPageOpen && mInfoArray != null) {

                if (mInfoIndex > 0) mInfoIndex -= 1;
                else mInfoIndex = 0;

                displayProgramInfo();

                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 5000);
            }

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

            if (isPageOpen && mInfoArray != null) {

                if (mInfoIndex < mInfoArray.size() - 1) mInfoIndex += 1;
                else mInfoIndex = mInfoArray.size() - 1;

                displayProgramInfo();

                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 5000);
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        hideProgramInfo();

        releasePlayer();
    }

    private void showProgramInfo() {

        try {
            if (isPageOpen) return;

            if (mInfoArray != null) {
                mInfoIndex = 0;

                SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.TIME_FORMAT_STR), Locale.KOREA);
                String currentTime = sdf.format(new Date());

                for (int i = 0; i < mInfoArray.size() - 1; i++) {

                    if (mInfoArray.get(i).isJsonNull())
                        continue;

                    if (mInfoArray.get(i).getAsJsonObject().get(getResources().getString(R.string.ETIME_STR)).isJsonNull())
                        continue;

                    String etime = mInfoArray.get(i).getAsJsonObject().get(getResources().getString(R.string.ETIME_STR)).getAsString();

                    if (currentTime.compareTo(etime) <= 0) {
                        mInfoIndex = i;
                        break;
                    }
                }

                sdf = new SimpleDateFormat(getResources().getString(R.string.CURRENT_TIME_FORMAT_STR), Locale.KOREA);
                currentTime = sdf.format(new Date());

                timeView.setText(currentTime);

                displayProgramInfo();
            }

            slidingPanel.startAnimation(translateTopAnim);
            slidingPanel.setVisibility(View.VISIBLE);

            mHandler.postDelayed(mRunnable, 5000);
        } catch (Exception ex) {

        } finally {

        }
    }

    private void hideProgramInfo() {

        if (!isPageOpen) return;

        slidingPanel.startAnimation(translateBottomAnim);
        slidingPanel.setVisibility(View.GONE);

        mHandler.removeCallbacks(mRunnable);
    }

    private void displayProgramInfo() {

        try {
            String stime = "", etime = "", name = "", finalText = getResources().getString(R.string.noinfo_str);

            if (!mInfoArray.get(mInfoIndex).getAsJsonObject().get(getResources().getString(R.string.STIME_STR)).isJsonNull())
                stime = mInfoArray.get(mInfoIndex).getAsJsonObject().get(getResources().getString(R.string.STIME_STR)).getAsString();

            if (!mInfoArray.get(mInfoIndex).getAsJsonObject().get(getResources().getString(R.string.ETIME_STR)).isJsonNull())
                etime = mInfoArray.get(mInfoIndex).getAsJsonObject().get(getResources().getString(R.string.ETIME_STR)).getAsString();

            if (!mInfoArray.get(mInfoIndex).getAsJsonObject().get(getResources().getString(R.string.NAME_STR)).isJsonNull())
                name = mInfoArray.get(mInfoIndex).getAsJsonObject().get(getResources().getString(R.string.NAME_STR)).getAsString();


            if (stime != null && stime.length() > 0 && etime != null && etime.length() > 0 && name != null && name.length() > 0) {
                stime = stime.substring(8, 10) + ":" + stime.substring(10, 12);
                etime = etime.substring(8, 10) + ":" + etime.substring(10, 12);
                finalText = stime + " ~ " + etime + "\n" + name + "\n";
            }

            infoView.setText(finalText);
        } catch (Exception ex) {
            infoView.setText(getResources().getString(R.string.noinfo_str));
        } finally {

        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {

            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            releasePlayer();
        }

        active = false;

        finish();
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Drawable d = new BitmapDrawable(getResources(), bitmap);
        mPlayerView.setDefaultArtwork(d);
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}