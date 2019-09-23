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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class PlayerActivity extends FragmentActivity implements Player.EventListener, Target {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    static boolean active = false;

    private Activity mContext;
    private PlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;

    private ArrayList<ChannelData> mChannels;
    private int mCurrentChannel;
    private int mChannelIndex;
    private int mEPGIndex;

    private SlidingPanel mSlidingPanel;
    private ProgressBar mProgressBar;

    private Point mWindowSize;
    private boolean isLongPressed = false;
    private boolean isSaveNeeded = false;

    // Used to load the native library on application startup.
    static {
        System.loadLibrary("alltv");
    }

    public native String[] tvingDecrypt(String code, String data);

    private String getStringById(int resourceId) {
        return this.getResources().getString(resourceId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_videoplayer);

        mContext = this;

        mPlayerView = (PlayerView)findViewById(R.id.video_view);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.VISIBLE);

        mSlidingPanel = new SlidingPanel(this);

        mWindowSize = Utils.getDisplaySize(this);

        isLongPressed = false;
        isSaveNeeded = false;

        createPlayer();

        mPlayerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                float left, right, top, bottom, mid;

                left  = (float) (mWindowSize.x * 0.1);
                right = (float) (mWindowSize.x * 0.9);
                mid   = (float) (mWindowSize.x * 0.5);
                top = left;
                bottom = (float)(mWindowSize.y - top);

                if(motionEvent.getX() < left) {
                    if (motionEvent.getY() < top) {
                        Intent intent = new Intent();
                        intent.putParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR), mChannels);
                        if( isSaveNeeded )
                            setResult(RESULT_OK, intent);
                        else
                            setResult(RESULT_CANCELED, intent);
                        finishAfterTransition();
                    } else {

                        if (mSlidingPanel.isPanelShow()) {
                            if (mEPGIndex >= 0) {
                                if (mEPGIndex > 0) mEPGIndex -= 1;
                                displayProgramInfo(3000);
                            }
                        } else {
                            if (mCurrentChannel > 0) {
                                mCurrentChannel -= 1;
                                mSlidingPanel.hideSlidingPanel();
                                mProgressBar.setVisibility(View.VISIBLE);
                                new FetchVideoUrlTask().execute(mCurrentChannel);
                            } else {
                                Toast.makeText(mContext, getStringById(R.string.first_channel),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                } else if(motionEvent.getX() > right) {

                    if (motionEvent.getY() < top) {

                        if(!mSlidingPanel.isPanelShow()) {
                            showProgramInfo(3000);
                        }

                        if (mCurrentChannel != mChannelIndex) {
                            return false;
                        }

                        ChannelData chData = mChannels.get(mCurrentChannel);
                        isSaveNeeded = true;
                        if (chData.getFavorite() > 0) {
                            chData.setFavorite(0);
                            mSlidingPanel.hideFavorite();
                            Toast.makeText(mContext, getStringById(R.string.favorite_disable),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            chData.setFavorite(1);
                            mSlidingPanel.showFavorite();
                            Toast.makeText(mContext, getStringById(R.string.favorite_enable),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (mSlidingPanel.isPanelShow()) {

                            if (mEPGIndex >= 0) {
                                int size = mChannels.get(mChannelIndex).getEPG().size() - 1;
                                if (mEPGIndex < size) mEPGIndex += 1;
                                displayProgramInfo(3000);
                            }

                        } else {

                            if (mCurrentChannel < mChannels.size() - 1) {
                                mCurrentChannel += 1;
                                mSlidingPanel.hideSlidingPanel();
                                mProgressBar.setVisibility(View.VISIBLE);
                                new FetchVideoUrlTask().execute(mCurrentChannel);
                            } else {
                                Toast.makeText(mContext, getStringById(R.string.last_channel),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    }

                } else if(motionEvent.getX() > (mid-left) && motionEvent.getX() < (mid+left) &&
                         (motionEvent.getY() < top || motionEvent.getY() > bottom)) {

                    if(mSlidingPanel.isPanelShow()) {
                        if(motionEvent.getY() < top) {

                            if(mChannelIndex < mChannels.size()-1)
                                mChannelIndex += 1;
                            else
                                mChannelIndex = 0;

                            changeChannelInfo(3000);

                        } else if( motionEvent.getY() > bottom) {

                            if(mChannelIndex > 0)
                                mChannelIndex -= 1;
                            else
                                mChannelIndex = mChannels.size()-1;

                            changeChannelInfo(3000);
                        }
                    }

                } else {
                    if(mSlidingPanel.isPanelShow()) {

                        mSlidingPanel.hideSlidingPanel();

                        if(mCurrentChannel != mChannelIndex) {
                            mCurrentChannel = mChannelIndex;
                            mProgressBar.setVisibility(View.VISIBLE);
                            new FetchVideoUrlTask().execute(mCurrentChannel);
                        }

                    }
                    else {
                        showProgramInfo(3000);
                    }
                }
                return false;
            }
        });

        mSlidingPanel.setInfoText("");

        mChannels = getIntent().getParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR));
        mCurrentChannel = getIntent().getIntExtra(getStringById(R.string.CURRENTCHANNEL_STR), 0);

        new FetchVideoUrlTask().execute(mCurrentChannel);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        ArrayList<ChannelData> channelData = intent.getParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR));

        mChannels.clear();
        mChannels = channelData;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

            if (mSlidingPanel.isPanelShow()) {
                if( isLongPressed ) {
                    isLongPressed = false;
                } else {
                    mSlidingPanel.hideSlidingPanel();

                    if(mCurrentChannel != mChannelIndex) {
                        mCurrentChannel = mChannelIndex;
                        mProgressBar.setVisibility(View.VISIBLE);
                        new FetchVideoUrlTask().execute(mCurrentChannel);
                    }
                }
            } else {
                showProgramInfo(3000);
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

            if( event.isLongPress() ) {

                isLongPressed = true;

                if(!mSlidingPanel.isPanelShow()) {
                    showProgramInfo(3000);
                }

                if(mCurrentChannel != mChannelIndex) {
                    return false;
                }

                isSaveNeeded = true;
                ChannelData chData = mChannels.get(mCurrentChannel);

                if (chData.getFavorite() > 0) {
                    chData.setFavorite(0);
                    mSlidingPanel.hideFavorite();
                    Toast.makeText(mContext, getStringById(R.string.favorite_disable),
                            Toast.LENGTH_SHORT).show();

                } else {
                    chData.setFavorite(1);
                    mSlidingPanel.showFavorite();
                    Toast.makeText(mContext, getStringById(R.string.favorite_enable),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                mSlidingPanel.closeDelayed(3000);
            }

        } else if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mSlidingPanel.isPanelShow()) {
                mSlidingPanel.hideSlidingPanel();
                return false;
            } else {

                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR), mChannels);

                if( isSaveNeeded )
                    setResult(RESULT_OK, intent);
                else
                    setResult(RESULT_CANCELED, intent);

                finishAfterTransition();
            }

        } else if(keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                  keyCode == KeyEvent.KEYCODE_CHANNEL_UP) {

            if(mSlidingPanel.isPanelShow()) {
                if(mChannelIndex < mChannels.size()-1)
                    mChannelIndex += 1;
                else
                    mChannelIndex = 0;

                changeChannelInfo(3000);

            } else {
                if(mCurrentChannel < mChannels.size()-1) {
                    mCurrentChannel += 1;
                    mSlidingPanel.hideSlidingPanel();
                    mProgressBar.setVisibility(View.VISIBLE);
                    new FetchVideoUrlTask().execute(mCurrentChannel);
                } else {
                    Toast.makeText(mContext, getStringById(R.string.last_channel),
                            Toast.LENGTH_SHORT).show();
                }
            }

        } else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                  keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN) {

            if(mSlidingPanel.isPanelShow()) {
                if(mChannelIndex > 0)
                    mChannelIndex -= 1;
                else
                    mChannelIndex = mChannels.size()-1;

                changeChannelInfo(3000);

            } else {
                if(mCurrentChannel > 0) {
                    mCurrentChannel -= 1;
                    mSlidingPanel.hideSlidingPanel();
                    mProgressBar.setVisibility(View.VISIBLE);
                    new FetchVideoUrlTask().execute(mCurrentChannel);
                } else {
                    Toast.makeText(mContext, getStringById(R.string.first_channel),
                            Toast.LENGTH_SHORT).show();
                }
            }

        } else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

            if(mSlidingPanel.isPanelShow()) {
                if(mEPGIndex >= 0) {
                    if (mEPGIndex > 0) mEPGIndex -= 1;
                }
                displayProgramInfo(3000);
            }

        } else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

            if(mSlidingPanel.isPanelShow()) {
                if(mEPGIndex >= 0) {
                    int size = mChannels.get(mChannelIndex).getEPG().size()-1;
                    if (mEPGIndex < size) mEPGIndex += 1;
                }
                displayProgramInfo(3000);
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSlidingPanel.hideSlidingPanel();
        releasePlayer();
    }

    private void showProgramInfo(int msec) {

        mChannelIndex = mCurrentChannel;

        ChannelData chData = mChannels.get(mChannelIndex);
        ArrayList<EPGData> epgData = chData.getEPG();

        mEPGIndex = -1;

        for(int i=0; i<epgData.size(); i++) {
            Date now = new Date();
            if(now.compareTo(epgData.get(i).getEndTime()) < 0) {
                mEPGIndex = i;
                break;
            }
        }

        displayProgramInfo(0);

        if(!mSlidingPanel.isPanelShow())
            mSlidingPanel.showSlidingPanel(msec);

    }

    private void changeChannelInfo(int msec) {

        ChannelData chData = mChannels.get(mChannelIndex);
        ArrayList<EPGData> epgData = chData.getEPG();

        mEPGIndex = -1;

        for(int i=0; i<epgData.size(); i++) {
            Date now = new Date();
            if(now.compareTo(epgData.get(i).getEndTime()) < 0) {
                mEPGIndex = i;
                break;
            }
        }

        displayProgramInfo(msec);
    }

    private void displayProgramInfo(int msec) {

        ChannelData chData = mChannels.get(mChannelIndex);
        ArrayList<EPGData> epgData = chData.getEPG();

        String title = chData.getTitle();

        if(mEPGIndex >= 0) {
            String name = epgData.get(mEPGIndex).getProgramName();
            Date stime = epgData.get(mEPGIndex).getStartTime();
            Date etime = epgData.get(mEPGIndex).getEndTime();

            String stimeStr = new SimpleDateFormat("HH:mm").format(stime);
            String etimeStr = new SimpleDateFormat("HH:mm").format(etime);

            mSlidingPanel.setInfoText(stimeStr + " ~ " + etimeStr + "\n" + title + " - " + name + "\n");
        } else {
            mSlidingPanel.setInfoText(title);
        }

        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        mSlidingPanel.setTimeText(currentTime);

        if(chData.getFavorite() > 0) {
            mSlidingPanel.showFavorite();
        } else {
            mSlidingPanel.hideFavorite();
        }

        if(msec > 0)
            mSlidingPanel.closeDelayed(msec);

    }

    private void createPlayer() {

        if (mPlayer != null) {
            releasePlayer();
        }

        mPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), new DefaultTrackSelector());

        mPlayerView.setPlayer(mPlayer);
        mPlayerView.setUseController(false);

        mPlayer.setPlayWhenReady(true);
        mPlayer.addListener(this);

        mPlayer.setVolume(1.0f);

        mSlidingPanel.setInfoText("");

    }

    private void openPlayer(String videoUrl) {

        ChannelData chData = mChannels.get(mCurrentChannel);

        if ( chData.isAudioChannel() ) {
            Picasso.get().load(chData.getStillImageUrl()).into(this);
        }

        if(videoUrl == null) return;

        Uri uriLive = Uri.parse(videoUrl);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                getStringById(R.string.USERAGENT));
        HlsMediaSource mediaSourceLive = new HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(uriLive);

        mPlayer.prepare(mediaSourceLive);

    }

    private void openPlayer(String videoUrl, String videoCookie) {

        ChannelData chData = mChannels.get(mCurrentChannel);

        if ( chData.isAudioChannel() ) {
            Picasso.get().load(chData.getStillImageUrl()).into(this);
        }

        if(videoUrl == null) return;

        Uri uriLive = Uri.parse(videoUrl);

        DefaultHttpDataSourceFactory httpSourceFactory = new DefaultHttpDataSourceFactory(
                getStringById(R.string.USERAGENT), null);

        if( videoCookie != null && !videoCookie.equals("") ) {
            httpSourceFactory.getDefaultRequestProperties().set("Cookie", videoCookie);
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                null, httpSourceFactory);

        HlsMediaSource mediaSourceLive = new HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(uriLive);

        mPlayer.prepare(mediaSourceLive);

    }

    private void releasePlayer() {

        if (mPlayer != null) {
            mPlayer.stop(true);
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

        if( isLoading )
            showProgramInfo(1000);

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

    private class FetchVideoUrlTask extends AsyncTask<Integer, Integer, Integer> {

        private String videoUrl = "";
        private String videoCookie = "";

        protected Integer doInBackground(Integer... channelIndex) {

            ArrayList<ChannelData> chList = mChannels;
            int arrIndex = channelIndex[0];

            if(arrIndex < 0)
                return Utils.Code.NoVideoUrl_err.ordinal();

            ChannelData chData = chList.get(arrIndex);

            if(chData.getSiteType() == Utils.SiteType.Oksusu.ordinal()) {
                return OksusuFetchVideoUrl(chData);
            } else if(chData.getSiteType() == Utils.SiteType.Pooq.ordinal()) {
                return PooqFetchVideoUrl(chData);
            } else if(chData.getSiteType() == Utils.SiteType.Tving.ordinal()) {
                return TvingFetchVideoUrl(chData);
            }

            return Utils.Code.NoVideoUrl_err.ordinal();
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        protected void onPostExecute(Integer result) {

            mProgressBar.setVisibility(View.GONE);

            switch (Utils.Code.values()[result]) {
                case NoAuthKey_err:
                    Utils.showToast(mContext, R.string.login_error_video);
                    setResult(RESULT_CANCELED);
                    finishAfterTransition();
                    break;
                case NoVideoUrl_err:
                    Utils.showToast(mContext, R.string.geturl_error);
                    setResult(RESULT_CANCELED);
                    finishAfterTransition();
                    break;
                case FetchVideoUrlTask_OK:
                    if(videoCookie == null || videoCookie.equals(""))
                        openPlayer(videoUrl);
                    else
                        openPlayer(videoUrl, videoCookie);
                    break;
            }
        }

        private int PooqFetchVideoUrl(ChannelData chData) {

            String authKey = chData.getAuthkey();

            if (authKey == null || authKey.length() < 10)
                return Utils.Code.NoAuthKey_err.ordinal();

            try {

                String url = getStringById(R.string.POOQ_CHANNEL_URL) + chData.getId() + "/" + getStringById(R.string.URL_STR);

                HttpRequest request = HttpRequest.get(url, true,
                        getStringById(R.string.DEVICETYPEID_STR), getStringById(R.string.PC_STR),
                        getStringById(R.string.MARKETTYPEID_STR), getStringById(R.string.GENERIC_STR),
                        getStringById(R.string.POOQ_CREDENTIAL_STR), authKey,
                        getStringById(R.string.QUALITY_STR), getQualityTag(chData),
                        getStringById(R.string.DEVICEMODEID_STR), getStringById(R.string.PC_STR),
                        getStringById(R.string.AUTHTYPE_STR), getStringById(R.string.URL_STR))
                        .userAgent(getStringById(R.string.USERAGENT));

                if (request == null || request.badRequest() || request.isBodyEmpty()) {
                    return Utils.Code.NoVideoUrl_err.ordinal();
                }

                String resultJson = request.body();

                JsonParser parser = new JsonParser();

                videoUrl = Utils.removeQuote(parser.parse(resultJson).getAsJsonObject().get(getStringById(R.string.RESULT_STR))
                        .getAsJsonObject()
                        .get(getStringById(R.string.SIGNEDURL_STR))
                        .getAsString());

                if (videoUrl == null || videoUrl.equals(getStringById(R.string.NULL_STR)) || videoUrl.length() == 0) {
                    return Utils.Code.NoVideoUrl_err.ordinal();
                }

            } catch (Exception ex) {
                return Utils.Code.NoVideoUrl_err.ordinal();
            } finally {
                return Utils.Code.FetchVideoUrlTask_OK.ordinal();
            }
        }

        private int TvingFetchVideoUrl(ChannelData chData) {

            String authKey = chData.getAuthkey();

            if (authKey == null || authKey.length() < 10)
                return Utils.Code.NoAuthKey_err.ordinal();

            try {

                Long ts = System.currentTimeMillis() / 1000;
                String timestamp = ts.toString();

                String channelId = chData.getId();
                String quality = getQualityTag(chData);

                HttpRequest request = HttpRequest.get("http://api.tving.com/v1/media/stream/info", true,
                        "apiKey", "1e7952d0917d6aab1f0293a063697610",
                        "info", "Y",
                        "networkCode", "CSND0900",
                        "osCode", "CSOD0900",
                        "teleCode", "CSCD0900",
                        "mediaCode", channelId,
                        "screenCode", "CSSD0100",
                        "streamCode", quality,
                        "noCache", timestamp,
                        "callingFrom", "FLASH",
                        "adReq", "none",
                        "ooc", "")
                        .userAgent(getStringById(R.string.USERAGENT))
                        .header("Cookie", authKey);

                if (request == null || request.badRequest() || request.isBodyEmpty()) {
                    return Utils.Code.NoVideoUrl_err.ordinal();
                }

                String resultJson = request.body();

                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(resultJson).getAsJsonObject().getAsJsonObject("body");

                String broad_url = object.getAsJsonObject("stream").getAsJsonObject("broadcast").get("broad_url").getAsString();

                String[] decrypt = tvingDecrypt(channelId, broad_url);

                videoUrl = decrypt[0];
                videoCookie = decrypt[1];

                if (videoUrl == null || videoUrl.equals("null") || videoUrl.length() == 0) {
                    return Utils.Code.NoVideoUrl_err.ordinal();
                }

            } catch (Exception ex) {
                return Utils.Code.NoVideoUrl_err.ordinal();
            } finally {
                return Utils.Code.FetchVideoUrlTask_OK.ordinal();
            }
        }

        private int OksusuFetchVideoUrl(ChannelData chData) {

            String authKey = chData.getAuthkey();

            if (authKey == null || authKey.length() < 10)
                return Utils.Code.NoAuthKey_err.ordinal();

            try {

                String url = getStringById(R.string.OKSUSUVIDEO_URL) + String.valueOf(chData.getId());

                HttpRequest request = HttpRequest.get(url)
                        .userAgent("Mozilla/4.0")
                        .header(getStringById(R.string.COOKIE_STR), authKey);

                if(request == null || request.badRequest() || request.isBodyEmpty()) {
                    return Utils.Code.NoVideoUrl_err.ordinal();
                }

                String resultBody = request.body();

                if (resultBody.contains(getStringById(R.string.CONTENTINFO_STR))) {

                    String jsonStr = resultBody.substring(resultBody.indexOf(getStringById(R.string.CONTENTINFO_STR)) + 14,
                            resultBody.indexOf(getStringById(R.string.OKSUSUJSONSUB_STR)));

                    JsonParser jsonParser = new JsonParser();
                    JsonElement jsonElement = jsonParser.parse(jsonStr);

                    videoUrl = Utils.removeQuote(jsonElement.getAsJsonObject().get(getStringById(R.string.STREAMURL_STR))
                            .getAsJsonObject().get(getQualityTag(chData)).toString());

                    if (videoUrl.equals("null") || videoUrl.length() == 0) {
                        return Utils.Code.NoVideoUrl_err.ordinal();
                    }

                } else {
                    return Utils.Code.NoVideoUrl_err.ordinal();
                }

            } catch (Exception ex) {
                return Utils.Code.NoVideoUrl_err.ordinal();
            } finally {
                return Utils.Code.FetchVideoUrlTask_OK.ordinal();
            }

        }

        private String getQualityTag(ChannelData chData) {

            int siteType = chData.getSiteType();
            int qualityType = chData.getQualityType();

            if(siteType == Utils.SiteType.Pooq.ordinal()) {

                switch (SettingsData.PooqQualityType.values()[qualityType]) {
                    case Mobile:
                        return getStringById(R.string.POOQ_MOBILE_QUALITY_TAG);
                    case SD:
                        return getStringById(R.string.POOQ_SD_QUALITY_TAG);
                    case HD:
                        return getStringById(R.string.POOQ_HD_QUALITY_TAG);
                    case FHD:
                        return getStringById(R.string.POOQ_FHD_QUALITY_TAG);
                }

                return getStringById(R.string.POOQ_SD_QUALITY_TAG);

            } else if(siteType == Utils.SiteType.Tving.ordinal()) {

                switch (SettingsData.TvingQualityType.values()[qualityType]) {
                    case MD:
                        return getStringById(R.string.TVING_MD_QUALITY_TAG);
                    case SD:
                        return getStringById(R.string.TVING_SD_QUALITY_TAG);
                    case HD:
                        return getStringById(R.string.TVING_HD_QUALITY_TAG);
                    case FHD:
                        return getStringById(R.string.TVING_FHD_QUALITY_TAG);
                }

                return getStringById(R.string.TVING_HD_QUALITY_TAG);

            } else if(siteType == Utils.SiteType.Oksusu.ordinal()) {

                switch (SettingsData.OksusuQualityType.values()[qualityType]) {
                    case AUTO:
                        return getStringById(R.string.URLAUTO_STR);
                    case FullHD:
                        return getStringById(R.string.URLFHD_STR);
                    case HD:
                        return getStringById(R.string.URLHD_STR);
                    case SD:
                        return getStringById(R.string.URLSD_STR);
                }

                return getStringById(R.string.URLAUTO_STR);

            }

            return "";
        }

    }

}

