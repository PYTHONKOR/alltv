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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class PlayerActivity extends FragmentActivity implements Player.EventListener, Target {

    static boolean active = false;

    private PlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;
    private DataSource.Factory mDataSourceFactory;

    private Animation translateTopAnim;
    private Animation translateBottomAnim;
    private LinearLayout slidingPanel;

    private boolean isPageOpen=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_videoplayer);

        slidingPanel=(LinearLayout)findViewById(R.id.slidingPanel);

        mPlayerView = findViewById(R.id.video_view);

        mPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());

        mPlayerView.setPlayer(mPlayer);
        mPlayerView.setUseController(false);

        if(BuildConfig.DEBUG) {
            mPlayerView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

//                    new Thread(new Runnable() {
//                        public void run() {
//                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
//                        }
//                    }).start();

                    if(isPageOpen) {
                        slidingPanel.startAnimation(translateBottomAnim);
                        slidingPanel.setVisibility(View.GONE);
                    } else {
                        slidingPanel.startAnimation(translateTopAnim);
                        slidingPanel.setVisibility(View.VISIBLE);
                    }

                    return false;
                }
            });
        }

        mDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getResources().getString(R.string.app_name)));

        ChannelData item = getIntent().getParcelableExtra(getResources().getString(R.string.PLAYCHANNEL_STR));

        Uri uriLive = Uri.parse(item.getVideoUrl());

        HlsMediaSource mediaSourcelive = new HlsMediaSource.Factory(mDataSourceFactory).
                setAllowChunklessPreparation(true).createMediaSource(uriLive);

        if (item.isAudioChannel()) {
            Picasso.get().load(item.getStillImageUrl()).into(this);
        }

        mPlayer.prepare(mediaSourcelive);
        mPlayer.setPlayWhenReady(true);
        mPlayer.addListener(this);

        translateTopAnim =AnimationUtils.loadAnimation(this,R.anim.translate_top);
        translateBottomAnim =AnimationUtils.loadAnimation(this,R.anim.translate_bottom);

        translateTopAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(isPageOpen){
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPageOpen=false;
                }else{
                    isPageOpen=true;
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
                if(isPageOpen){
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPageOpen=false;
                }else{
                    isPageOpen=true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        releasePlayer();
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