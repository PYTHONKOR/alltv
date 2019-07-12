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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.RowsSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;

import java.util.ArrayList;
import java.util.HashMap;


public abstract class AllTvBaseRowsSupportFragment extends RowsSupportFragment {

    protected ArrayObjectAdapter mRowsAdapter;
    protected Utils.SiteType mType = Utils.SiteType.None;

    protected static HashMap<Utils.SiteType, ArrayList<ChannelData>> mChannels = new HashMap<>();
    protected static HashMap<Utils.SiteType, ArrayList<CategoryData>> mCategory = new HashMap<>();
    protected static HashMap<Utils.SiteType, String> mAuthKey = new HashMap<>();

    public AllTvBaseRowsSupportFragment() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        createRows();
    }

    @Override
    public void setExpand(boolean expand) {
        super.setExpand(true);
    }

    protected String getStringById(int resourceId) {
        return getResources().getString(resourceId);
    }

    public static void setChannelList(Utils.SiteType type, ArrayList<ChannelData> chList) {

        if (mChannels.containsKey(type)) {
            mChannels.get(type).clear();
        }

        mChannels.put(type, chList);
    }

    public static void setCategoryList(Utils.SiteType type, ArrayList<CategoryData> ctList) {
        if (mCategory.containsKey(type)) {
            mCategory.get(type).clear();
        }

        mCategory.put(type, ctList);
    }

    public static void setAuthKey(Utils.SiteType type, String authKey) {
        mAuthKey.put(type, authKey);
    }

    public static String getAuthKey(Utils.SiteType type) {
        return mAuthKey.get(type);
    }

    protected void setVideoUrlByIndex(Utils.SiteType type, int index, String videoUrl) {
        mChannels.get(type).get(index).setVideoUrl(videoUrl);
    }

    public static void clearAllVideoUrl(Utils.SiteType type) {
        ArrayList<ChannelData> chList = mChannels.get(type);

        if (chList == null)
            return;

        for (ChannelData chData : chList) {
            chData.setVideoUrl(null);
        }
    }

    public abstract void createRows();

    protected void createDefaultRows() {
        CardPresenter presenterSelector = new CardPresenter();
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(presenterSelector);

        HeaderItem headerItem = new HeaderItem(getStringById(R.string.dologin_please));

        mRowsAdapter.add(new ListRow(headerItem, adapter));
    }

    protected boolean isEmptyCategory(Utils.SiteType checkType) {
        return !mCategory.containsKey(checkType) || mCategory.get(checkType) == null || mCategory.get(checkType).size() == 0;
    }

    protected void playVideo(ChannelData playChannel) {

        if (PlayerActivity.active) {
            return;
        }

        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(getStringById(R.string.PLAYCHANNEL_STR), playChannel);

        getActivity().startActivity(intent);
    }

    protected void playVideo(ChannelData playChannel, String playInfo) {

        if (PlayerActivity.active) {
            return;
        }

        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(getStringById(R.string.PLAYCHANNEL_STR), playChannel);
        intent.putExtra(getStringById(R.string.PLAYINFO_STR), playInfo);

        getActivity().startActivity(intent);
    }

    protected class FetchVideoUrlTask extends AsyncTask<Integer, Integer, Integer> {

        SpinnerFragment mSpinnerFragment = new SpinnerFragment();

        protected Integer doInBackground(Integer... channelIndex) {
            return Utils.Code.FetchVideoUrlTask_OK.ordinal();
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        protected void onPostExecute(Integer result) {

            getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();

            switch (Utils.Code.values()[result]) {
                case NoAuthKey_err:
                    Utils.showToast(getActivity(), R.string.login_error_video);
                    break;
                case NoVideoUrl_err:
                    Utils.showToast(getActivity(), R.string.geturl_error);
                    break;
            }
        }
    }




}

