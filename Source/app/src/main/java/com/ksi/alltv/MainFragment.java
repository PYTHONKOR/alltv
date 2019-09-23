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
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PageRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainFragment extends BrowseSupportFragment implements FetchChannelResultReceiver.Receiver,
        OnItemViewClickedListener, OnItemViewSelectedListener {

    private static final String TAG = MainFragment.class.getSimpleName();

    private final Handler mHandler = new Handler();
    private PicassoBackgroundManager mPicassoBackgroundManager = null;
    private Gson mGson = new Gson();

    private FetchChannelResultReceiver mChannelResultReceiver;
    private ArrayObjectAdapter mCategoryRowAdapter;

    private SettingsData mSettingsData = new SettingsData();

    private SpinnerFragment[] mSpinnerFragment = new SpinnerFragment[Utils.SiteType.values().length];

    private VersionChecker mVersionChecker;

    private Timer mTimer;
    private String mUpdateTime;


    private String getStringById(int resourceId) {
        return this.getResources().getString(resourceId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initOnCreated();

        if (mSettingsData.mPooqSettings.mEnable &&
            (mSettingsData.mPooqSettings.mId != null && mSettingsData.mPooqSettings.mId.length() > 0)) {
            startServiceIntent(Utils.SiteType.Pooq);
        }
        if (mSettingsData.mTvingSettings.mEnable &&
            (mSettingsData.mTvingSettings.mId != null && mSettingsData.mTvingSettings.mId.length() > 0)) {
            startServiceIntent(Utils.SiteType.Tving);
        }
        if (mSettingsData.mOksusuSettings.mEnable &&
                (mSettingsData.mOksusuSettings.mId != null && mSettingsData.mOksusuSettings.mId.length() > 0)) {
            startServiceIntent(Utils.SiteType.Oksusu);
        }

    }

    private void initOnCreated() {

        Hawk.init(getContext())
                //.setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
                .build();

        if (Hawk.contains(getStringById(R.string.SETTINGS_STR))) {

            String settingsStr = Hawk.get(getStringById(R.string.SETTINGS_STR));

            mSettingsData = mGson.fromJson(settingsStr, SettingsData.class);

            PooqRowSupportFragment.setEnable(mSettingsData.mPooqSettings.mEnable? Boolean.TRUE: Boolean.FALSE);
            PooqRowSupportFragment.setQualityType(mSettingsData.mPooqSettings.mQualityType);
            TvingRowSupportFragment.setEnable(mSettingsData.mTvingSettings.mEnable? Boolean.TRUE: Boolean.FALSE);
            TvingRowSupportFragment.setQualityType(mSettingsData.mTvingSettings.mQualityType);
            OksusuRowSupportFragment.setEnable(mSettingsData.mOksusuSettings.mEnable? Boolean.TRUE: Boolean.FALSE);
            OksusuRowSupportFragment.setQualityType(mSettingsData.mOksusuSettings.mQualityType);

        } else {
            mSettingsData.mPooqSettings.mEnable = false;
            mSettingsData.mPooqSettings.mId = getStringById(R.string.PooqId);
            mSettingsData.mPooqSettings.mPassword = getStringById(R.string.PooqPwd);
            mSettingsData.mPooqSettings.mQualityType = SettingsData.PooqQualityType.SD;
            PooqRowSupportFragment.setEnable(mSettingsData.mPooqSettings.mEnable? Boolean.TRUE: Boolean.FALSE);
            PooqRowSupportFragment.setQualityType(mSettingsData.mPooqSettings.mQualityType);

            mSettingsData.mTvingSettings.mEnable = false;
            mSettingsData.mTvingSettings.mCJOneId = getStringById(R.string.CjOneTvingId).equals("true") ? true: false;
            mSettingsData.mTvingSettings.mId = getStringById(R.string.TvingId);
            mSettingsData.mTvingSettings.mPassword = getStringById(R.string.TvingPwd);
            mSettingsData.mTvingSettings.mQualityType = SettingsData.TvingQualityType.HD;
            TvingRowSupportFragment.setEnable(mSettingsData.mTvingSettings.mEnable? Boolean.TRUE: Boolean.FALSE);
            TvingRowSupportFragment.setQualityType(mSettingsData.mTvingSettings.mQualityType);

            mSettingsData.mOksusuSettings.mEnable = false;
            mSettingsData.mOksusuSettings.mId = getStringById(R.string.OksusuId);
            mSettingsData.mOksusuSettings.mPassword = getStringById(R.string.OksusuPwd);
            mSettingsData.mOksusuSettings.mQualityType = SettingsData.OksusuQualityType.FullHD;
            OksusuRowSupportFragment.setEnable(mSettingsData.mOksusuSettings.mEnable? Boolean.TRUE: Boolean.FALSE);
            OksusuRowSupportFragment.setQualityType(mSettingsData.mOksusuSettings.mQualityType);
        }

        setupUIElements();

        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());

        mCategoryRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mCategoryRowAdapter);

        createDefaultRows();

        setupEventListeners();

        mPicassoBackgroundManager = new PicassoBackgroundManager(getActivity());

        mChannelResultReceiver = new FetchChannelResultReceiver(mHandler);
        mChannelResultReceiver.setReceiver(this);

        // Version Check
        mVersionChecker = new VersionChecker(getActivity());
        mVersionChecker.setcheckJsonUrl(getStringById(R.string.UPDATE_JSON_URL));
        mVersionChecker.check();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                String now =  new SimpleDateFormat("HH").format(new Date());

                if(now.compareTo(mUpdateTime) != 0) {

                    Fragment fragment = getMainFragment();

                    if (fragment instanceof AllTvBaseRowsSupportFragment) {

                        mUpdateTime = now;

                        if (mSettingsData.mPooqSettings.mEnable &&
                            (mSettingsData.mPooqSettings.mId != null && mSettingsData.mPooqSettings.mId.length() > 0)) {
                            refreshServiceIntent(Utils.SiteType.Pooq);
                        }
                        if (mSettingsData.mTvingSettings.mEnable &&
                            (mSettingsData.mTvingSettings.mId != null && mSettingsData.mTvingSettings.mId.length() > 0)) {
                            refreshServiceIntent(Utils.SiteType.Tving);
                        }
                        if (mSettingsData.mOksusuSettings.mEnable &&
                                (mSettingsData.mOksusuSettings.mId != null && mSettingsData.mOksusuSettings.mId.length() > 0)) {
                            refreshServiceIntent(Utils.SiteType.Oksusu);
                        }
                    }

                } else {
                    Fragment fragment = getMainFragment();

                    if (fragment instanceof AllTvBaseRowsSupportFragment) {
                        ((AllTvBaseRowsSupportFragment) fragment).refreshRows();
                    }
                }
            }
        };

        mUpdateTime = new SimpleDateFormat("HH").format(new Date());

        mTimer = new Timer();
        mTimer.schedule(task, 10 * 60 * 1000, 10 * 60 * 1000);

    }

    public void startServiceIntent(Utils.SiteType inSiteType) {

        mSpinnerFragment[inSiteType.ordinal()] = new SpinnerFragment();
        getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mSpinnerFragment[inSiteType.ordinal()]).commit();

        // Start Service Intent
        Intent serviceIntent = new Intent(getActivity(), FetchChannelService.class);

        String mySettingsJson = mGson.toJson(mSettingsData);

        serviceIntent.putExtra(getStringById(R.string.FETCHCHANNELRESULTRECEIVER_STR), mChannelResultReceiver);
        serviceIntent.putExtra(getStringById(R.string.SETTINGSDATA_STR), mySettingsJson);
        serviceIntent.putExtra(getStringById(R.string.SITETYPE_STR), inSiteType);
        serviceIntent.putExtra(getStringById(R.string.AUTHKEY_STR), "");
        serviceIntent.putExtra(getStringById(R.string.FETCHMODE_STR), "create");

        getActivity().startService(serviceIntent);
    }

    public void refreshServiceIntent(Utils.SiteType inSiteType) {

        if(PlayerActivity.active) {
            mSpinnerFragment[inSiteType.ordinal()] = null;
        } else {
            mSpinnerFragment[inSiteType.ordinal()] = new SpinnerFragment();
            getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mSpinnerFragment[inSiteType.ordinal()]).commit();
        }

        // Start Service Intent
        Intent serviceIntent = new Intent(getActivity(), FetchChannelService.class);

        String mySettingsJson = mGson.toJson(mSettingsData);
        String authkey = AllTvBaseRowsSupportFragment.getAuthKey(inSiteType);

        serviceIntent.putExtra(getStringById(R.string.FETCHCHANNELRESULTRECEIVER_STR), mChannelResultReceiver);
        serviceIntent.putExtra(getStringById(R.string.SETTINGSDATA_STR), mySettingsJson);
        serviceIntent.putExtra(getStringById(R.string.SITETYPE_STR), inSiteType);
        serviceIntent.putExtra(getStringById(R.string.AUTHKEY_STR), authkey);
        serviceIntent.putExtra(getStringById(R.string.FETCHMODE_STR), "refresh");
        serviceIntent.putParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR),
                AllTvBaseRowsSupportFragment.mChannels.get(inSiteType));
        serviceIntent.putParcelableArrayListExtra(getStringById(R.string.CATEGORY_STR),
                AllTvBaseRowsSupportFragment.mCategory.get(inSiteType));

        getActivity().startService(serviceIntent);
    }

    public class PageRowFragmentFactory extends BrowseSupportFragment.FragmentFactory<Fragment> {

        @Override
        public Fragment createFragment(Object rowObj) {

            Row row = (Row) rowObj;

            if(row.getHeaderItem().getId() == Utils.Header.Favorite.ordinal()) {
                return new FavoriteRowSupportFragment();
            }  else if (row.getHeaderItem().getId() == Utils.Header.Pooq.ordinal()) {
                return new PooqRowSupportFragment();
            } else if (row.getHeaderItem().getId() == Utils.Header.Tving.ordinal()) {
                return new TvingRowSupportFragment();
            } else if (row.getHeaderItem().getId() == Utils.Header.Oksusu.ordinal()) {
                return new OksusuRowSupportFragment();
            }

            throw new IllegalArgumentException(String.format("Invalid row %s", rowObj));
        }
    }

    private void setupUIElements() {
        setTitle(getStringById(R.string.browse_title));

        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        setBrandColor(ContextCompat.getColor(getContext(), R.color.fastlane_background));
        setSearchAffordanceColor(ContextCompat.getColor(getContext(), R.color.search_opaque));
    }

    public void createDefaultRows() {

        mCategoryRowAdapter.clear();

        //Favorite
        HeaderItem favoriteHeader = new HeaderItem(Utils.Header.Favorite.ordinal(),
                getStringById(R.string.favorite));
        mCategoryRowAdapter.add(new PageRow(favoriteHeader));

        //Pooq
        if(mSettingsData.mPooqSettings.mEnable) {
            HeaderItem pooqHeader = new HeaderItem(Utils.Header.Pooq.ordinal(),
                    getStringById(R.string.pooq));
            mCategoryRowAdapter.add(new PageRow(pooqHeader));
        }

        //Tving
        if(mSettingsData.mTvingSettings.mEnable) {
            HeaderItem tvingHeader = new HeaderItem(Utils.Header.Tving.ordinal(),
                    getStringById(R.string.tving));
            mCategoryRowAdapter.add(new PageRow(tvingHeader));
        }

        //Oksusu
        if(mSettingsData.mOksusuSettings.mEnable) {
            HeaderItem oksusuHeader = new HeaderItem(Utils.Header.Oksusu.ordinal(),
                    getStringById(R.string.oksusu));
            mCategoryRowAdapter.add(new PageRow(oksusuHeader));
        }

        // Etc
        HeaderItem gridHeader = new HeaderItem(Utils.Header.Etc.ordinal(),
                getStringById(R.string.etc));

        GridItemPresenter gridItemPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridItemPresenter);

        gridRowAdapter.add(getStringById(R.string.preferences));
        gridRowAdapter.add(getStringById(R.string.opensource));
        gridRowAdapter.add(getStringById(R.string.version_str) + " " + BuildConfig.VERSION_NAME);

        mCategoryRowAdapter.add(new ListRow(gridHeader, gridRowAdapter));
    }

    private void setupEventListeners() {

        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BuildConfig.DEBUG) {
                    new Thread(new Runnable() {
                        public void run() {
                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        }
                    }).start();
                } else {
                    Utils.showToast(getContext(), R.string.notready);
                }
            }
        });

    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {

        if (item instanceof String) {
            if (((String) item).contains(getString(R.string.preferences))) {

                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra(getStringById(R.string.SETTINGSDATA_STR), mGson.toJson(mSettingsData));
                getActivity().startActivityForResult(intent, Utils.Code.SettingsRequestCode.ordinal());

            } else if (((String) item).contains(getString(R.string.opensource))) {
                showLicensesDialogFragment();
            }
        }

    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                               RowPresenter.ViewHolder rowViewHolder, Row row) {

        if (item instanceof String) {
            //mPicassoBackgroundManager.updateBackgroundWithDelay("http://blabla/blabla.jpg");
        } else if (item instanceof ChannelData) {
            //mPicassoBackgroundManager.updateBackgroundWithDelay(((ChannelData) item).getStillImageUrl());
        }
    }

    // GridItemPresenter
    private class GridItemPresenter extends Presenter {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {

            TextView view = new TextView(parent.getContext());

            view.setLayoutParams(new ViewGroup.LayoutParams(getResources().getInteger(R.integer.GRID_ITEM_WIDTH), getResources().getInteger(R.integer.GRID_ITEM_HEIGHT)));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        Utils.SiteType siteType = (Utils.SiteType)resultData.get(getStringById(R.string.SITETYPE_STR));
        String fetchMode = (String)resultData.get(getStringById(R.string.FETCHMODE_STR));
        boolean refresh = fetchMode.equals("refresh");

        ArrayList<ChannelData> chList;

        if(!PlayerActivity.active && mSpinnerFragment[siteType.ordinal()] != null) {
            getFragmentManager().beginTransaction().remove(mSpinnerFragment[siteType.ordinal()]).commit();
            mSpinnerFragment[siteType.ordinal()] = null;
        }

        switch (Utils.Code.values()[resultCode]) {
            case ServiceIntent_OK:
                if (resultData != null) {
                    switch (siteType) {

                        case Pooq:
                            chList = resultData.getParcelableArrayList(getStringById(R.string.CHANNELS_STR));

                            if (Hawk.contains(getStringById(R.string.POOQ_CHANNELS_STR))) {
                                String str = Hawk.get(getStringById(R.string.POOQ_CHANNELS_STR));
                                ArrayList<String> favorites = mGson.fromJson(str,
                                        new TypeToken<ArrayList<String>>() {}.getType());
                                for(ChannelData chData : chList) {
                                    for (String val : favorites)
                                        if (val.contains(chData.getId())) chData.setFavorite(1);
                                }
                            }

                            if(!refresh) {
                                PooqRowSupportFragment.setChannelList(chList);
                                PooqRowSupportFragment.setCategoryList(resultData.getParcelableArrayList(getStringById(R.string.CATEGORY_STR)));
                                PooqRowSupportFragment.setAuthKey(resultData.getString(getStringById(R.string.AUTHKEY_STR)));
                            } else {
                                PooqRowSupportFragment.updateEPG(chList);
                            }
                            break;

                        case Tving:
                            chList = resultData.getParcelableArrayList(getStringById(R.string.CHANNELS_STR));
                            if (Hawk.contains(getStringById(R.string.TVING_CHANNELS_STR))) {
                                String str = Hawk.get(getStringById(R.string.TVING_CHANNELS_STR));
                                ArrayList<String> favorites = mGson.fromJson(str,
                                        new TypeToken<ArrayList<String>>() {}.getType());
                                for(ChannelData chData : chList) {
                                    for (String val : favorites)
                                        if (val.contains(chData.getId())) chData.setFavorite(1);
                                }
                            }

                            if(!refresh) {
                                TvingRowSupportFragment.setChannelList(chList);
                                TvingRowSupportFragment.setCategoryList(resultData.getParcelableArrayList(getStringById(R.string.CATEGORY_STR)));
                                TvingRowSupportFragment.setAuthKey(resultData.getString(getStringById(R.string.AUTHKEY_STR)));
                            } else {
                                TvingRowSupportFragment.updateEPG(chList);
                            }
                            break;

                        case Oksusu:
                            chList = resultData.getParcelableArrayList(getStringById(R.string.CHANNELS_STR));
                            if (Hawk.contains(getStringById(R.string.OKSUSU_CHANNELS_STR))) {
                                String str = Hawk.get(getStringById(R.string.OKSUSU_CHANNELS_STR));
                                ArrayList<String> favorites = mGson.fromJson(str,
                                        new TypeToken<ArrayList<String>>() {}.getType());
                                for(ChannelData chData : chList) {
                                    for (String val : favorites)
                                        if (val.contains(chData.getId())) chData.setFavorite(1);
                                }
                            }

                            if(!refresh) {
                                OksusuRowSupportFragment.setChannelList(chList);
                                OksusuRowSupportFragment.setCategoryList(resultData.getParcelableArrayList(getStringById(R.string.CATEGORY_STR)));
                                OksusuRowSupportFragment.setAuthKey(resultData.getString(getStringById(R.string.AUTHKEY_STR)));
                            } else {
                                OksusuRowSupportFragment.updateEPG(chList);
                            }
                            break;
                    }

                    Fragment fragment = getMainFragment();

                    if(siteType == Utils.SiteType.Pooq && fragment instanceof PooqRowSupportFragment) {
                        if(PlayerActivity.active)
                            ((PooqRowSupportFragment) fragment).sendChannelData();
                        else if(refresh)
                            ((PooqRowSupportFragment) fragment).refreshRows();
                        else
                            ((PooqRowSupportFragment) fragment).createRows();
                    }  else if(siteType == Utils.SiteType.Tving && fragment instanceof TvingRowSupportFragment) {
                        if(PlayerActivity.active)
                            ((TvingRowSupportFragment) fragment).sendChannelData();
                        else if(refresh)
                            ((TvingRowSupportFragment) fragment).refreshRows();
                        else
                            ((TvingRowSupportFragment) fragment).createRows();
                    } else if(siteType == Utils.SiteType.Oksusu && fragment instanceof OksusuRowSupportFragment) {
                        if(PlayerActivity.active)
                            ((OksusuRowSupportFragment) fragment).sendChannelData();
                        else if(refresh)
                            ((OksusuRowSupportFragment) fragment).refreshRows();
                        else
                            ((OksusuRowSupportFragment) fragment).createRows();
                    }

                    if(fragment instanceof FavoriteRowSupportFragment) {

                        if(siteType == Utils.SiteType.Pooq) {
                            if(mSettingsData.mTvingSettings.mEnable || mSettingsData.mOksusuSettings.mEnable) {
                                break;
                            }
                        } else if(siteType == Utils.SiteType.Tving) {
                            if(mSettingsData.mOksusuSettings.mEnable) {
                                break;
                            }
                        }

                        if(PlayerActivity.active)
                            ((FavoriteRowSupportFragment) fragment).sendChannelData();
                        else if(refresh)
                            ((FavoriteRowSupportFragment) fragment).refreshRows();
                        else
                            ((FavoriteRowSupportFragment) fragment).createRows();
                    }

                }
                break;

            case ServiceIntent_Fail:
                switch (siteType) {
                    case Pooq:
                        Utils.showToast(getContext(), R.string.pooq_login_fail);
                        break;
                    case Tving:
                        Utils.showToast(getContext(), R.string.tving_login_fail);
                        break;
                    case Oksusu:
                        Utils.showToast(getContext(), R.string.oksusu_login_fail);
                        break;
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        ArrayList<ChannelData> chList;
        ArrayList<String> favorites = new ArrayList<String>();
        Fragment fragment;

        switch (Utils.Code.values()[requestCode]) {
            case SettingsRequestCode:
                if (resultCode == 0)
                    return;

                String settingsStr = data.getStringExtra(getStringById(R.string.SETTINGSDATA_STR));
                mSettingsData = mGson.fromJson(settingsStr, SettingsData.class);

                if (resultCode == Utils.Code.PooqSave.ordinal()) {
                    PooqRowSupportFragment.setEnable(mSettingsData.mPooqSettings.mEnable ? Boolean.TRUE : Boolean.FALSE);
                    PooqRowSupportFragment.setQualityType(mSettingsData.mPooqSettings.mQualityType);
                    if (mSettingsData.mPooqSettings.mEnable) {
                        startServiceIntent(Utils.SiteType.Pooq);
                    }
                } else if (resultCode == Utils.Code.TvingSave.ordinal()) {
                    TvingRowSupportFragment.setEnable(mSettingsData.mTvingSettings.mEnable ? Boolean.TRUE : Boolean.FALSE);
                    TvingRowSupportFragment.setQualityType(mSettingsData.mTvingSettings.mQualityType);
                    if (mSettingsData.mTvingSettings.mEnable) {
                        startServiceIntent(Utils.SiteType.Tving);
                    }
                } else if (resultCode == Utils.Code.OksusuSave.ordinal()) {
                    OksusuRowSupportFragment.setEnable(mSettingsData.mOksusuSettings.mEnable ? Boolean.TRUE : Boolean.FALSE);
                    OksusuRowSupportFragment.setQualityType(mSettingsData.mOksusuSettings.mQualityType);
                    if (mSettingsData.mOksusuSettings.mEnable) {
                        startServiceIntent(Utils.SiteType.Oksusu);
                    }
                }

                if (!Hawk.put(getStringById(R.string.SETTINGS_STR), mGson.toJson(mSettingsData))) {
                    Utils.showToast(getContext(), R.string.settingssave_error);
                    return;
                }

                createDefaultRows();

                break;

            case FavoritePlay:

                boolean updated = false;

                if (resultCode == -1) {
                    chList = data.getParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR));

                    favorites.clear();
                    for (int i = 0; i < chList.size(); i++) {
                        ChannelData chData = chList.get(i);
                        if (chData.getSiteType() == Utils.SiteType.Pooq.ordinal() && chData.getFavorite() > 0)
                            favorites.add(chData.getId());
                    }

                    if (PooqRowSupportFragment.updateFavoriteList(favorites)) {
                        updated = true;
                        if (!Hawk.put(getStringById(R.string.POOQ_CHANNELS_STR), mGson.toJson(favorites))) {
                            return;
                        }
                    }

                    favorites.clear();
                    for (int i = 0; i < chList.size(); i++) {
                        ChannelData chData = chList.get(i);
                        if (chData.getSiteType() == Utils.SiteType.Tving.ordinal() && chData.getFavorite() > 0)
                            favorites.add(chData.getId());
                    }

                    if (TvingRowSupportFragment.updateFavoriteList(favorites)) {
                        updated = true;
                        if (!Hawk.put(getStringById(R.string.TVING_CHANNELS_STR), mGson.toJson(favorites))) {
                            return;
                        }
                    }

                    favorites.clear();
                    for (int i = 0; i < chList.size(); i++) {
                        ChannelData chData = chList.get(i);
                        if (chData.getSiteType() == Utils.SiteType.Oksusu.ordinal() && chData.getFavorite() > 0)
                            favorites.add(chData.getId());
                    }

                    if (OksusuRowSupportFragment.updateFavoriteList(favorites)) {
                        updated = true;
                        if (!Hawk.put(getStringById(R.string.OKSUSU_CHANNELS_STR), mGson.toJson(favorites))) {
                            return;
                        }
                    }
                }

                fragment = getMainFragment();
                if (fragment instanceof AllTvBaseRowsSupportFragment) {
                    if( updated )
                        ((AllTvBaseRowsSupportFragment) fragment).createRows();
                    else
                        ((AllTvBaseRowsSupportFragment) fragment).refreshRows();
                }

                break;

            case PooqPlay:
                if (resultCode == -1) {
                    chList = data.getParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR));

                    favorites.clear();
                    for (int i = 0; i < chList.size(); i++) {
                        ChannelData chData = chList.get(i);
                        if (chData.getFavorite() > 0) favorites.add(chData.getId());
                    }

                    if (PooqRowSupportFragment.updateFavoriteList(favorites)) {
                        if (!Hawk.put(getStringById(R.string.POOQ_CHANNELS_STR), mGson.toJson(favorites))) {
                            return;
                        }
                    }
                }

                fragment = getMainFragment();

                if (fragment instanceof AllTvBaseRowsSupportFragment)
                    ((AllTvBaseRowsSupportFragment) fragment).refreshRows();

                break;

            case TvingPlay:
                if(resultCode == -1) {
                    chList = data.getParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR));

                    favorites.clear();
                    for (int i = 0; i < chList.size(); i++) {
                        ChannelData chData = chList.get(i);
                        if (chData.getFavorite() > 0) favorites.add(chData.getId());
                    }

                    if (TvingRowSupportFragment.updateFavoriteList(favorites)) {
                        if (!Hawk.put(getStringById(R.string.TVING_CHANNELS_STR), mGson.toJson(favorites))) {
                            return;
                        }
                    }
                }

                fragment = getMainFragment();
                if(fragment instanceof AllTvBaseRowsSupportFragment)
                    ((AllTvBaseRowsSupportFragment) fragment).refreshRows();

                break;

            case OksusuPlay:
                if(resultCode == -1) {
                    chList = data.getParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR));

                    favorites.clear();
                    for (int i = 0; i < chList.size(); i++) {
                        ChannelData chData = chList.get(i);
                        if (chData.getFavorite() > 0) favorites.add(chData.getId());
                    }

                    if (OksusuRowSupportFragment.updateFavoriteList(favorites)) {
                        if (!Hawk.put(getStringById(R.string.OKSUSU_CHANNELS_STR), mGson.toJson(favorites))) {
                            return;
                        }
                    }
                }

                fragment = getMainFragment();
                if(fragment instanceof AllTvBaseRowsSupportFragment)
                    ((AllTvBaseRowsSupportFragment) fragment).refreshRows();

                break;
        }
    }

    private void showLicensesDialogFragment() {

        LicensesDialogFragment dialog = LicensesDialogFragment.newInstance();
        dialog.show(getActivity().getSupportFragmentManager(), getStringById(R.string.LICENSESDIALOG_STR));

    }
}