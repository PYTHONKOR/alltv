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

import android.os.Bundle;
import android.support.v17.leanback.app.RowsSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;


public abstract class AllTvBaseRowsSupportFragment extends RowsSupportFragment implements OnItemViewSelectedListener {

    private static final String TAG = AllTvBaseRowsSupportFragment.class.getSimpleName();

    protected ArrayObjectAdapter mRowsAdapter;
    protected Utils.SiteType mType = Utils.SiteType.None;

    protected static HashMap<Utils.SiteType, ArrayList<ChannelData>> mChannels = new HashMap<>();
    protected static HashMap<Utils.SiteType, ArrayList<CategoryData>> mCategory = new HashMap<>();
    protected static HashMap<Utils.SiteType, String> mAuthKey = new HashMap<>();
    protected static HashMap<Utils.SiteType, Boolean> mEnable = new HashMap<>();

    protected static int mRowIndex = 0;
    protected static int mItemIndex = 0;

    public AllTvBaseRowsSupportFragment() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);
        setOnItemViewSelectedListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createRows();
    }

    @Override
    public void setExpand(boolean expand) {
        super.setExpand(true);
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                               RowPresenter.ViewHolder rowViewHolder, Row row) {

        if(rowViewHolder.getSelectedItem() != null) {
            ChannelData channelData = (ChannelData)rowViewHolder.getSelectedItem();

            mRowIndex = getSelectedPosition();
            mItemIndex = channelData.getItemIndex();

            // Log.e(TAG, "onItemSelected: row: " + Integer.toString(mRowIndex) + ", Cnt: " + Integer.toString(mItemIndex));
        } else {
            mRowIndex = 0;
            mItemIndex = 0;
        }

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

    public static void setEnable(Utils.SiteType type, Boolean enable) {
        mEnable.put(type, enable);
    }

    public static Boolean getEnable(Utils.SiteType type) {
        return mEnable.get(type);
    }

    public static boolean updateFavoriteList(Utils.SiteType type, ArrayList<String> favorites) {

        ArrayList<ChannelData> chList = mChannels.get(type);

        if(chList == null)
            return false;

        boolean updated = false;

        for(int i=0; i<chList.size(); i++) {

            int favorite = chList.get(i).getFavorite();
            chList.get(i).setFavorite(0);

            for(int j=0; j<favorites.size(); j++) {
                if(chList.get(i).getId().compareTo(favorites.get(j)) == 0) {
                    chList.get(i).setFavorite(1);
                }
            }

            if(chList.get(i).getFavorite() != favorite) updated = true;
        }

        return updated;
    }

    public static void updateEPG(Utils.SiteType type, ArrayList<ChannelData> chList) {

        ArrayList<ChannelData> channels = mChannels.get(type);

        for(int i=0; i<channels.size(); i++) {
            channels.get(i).setEPG(chList.get(i).getEPG());
        }
    }

    public abstract void createRows();
    public abstract void refreshRows();
    public abstract void sendChannelData();

    protected void createDefaultRows() {
        CardPresenter presenterSelector = new CardPresenter();
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(presenterSelector);

        HeaderItem headerItem = new HeaderItem(getStringById(R.string.dologin_please));

        mRowsAdapter.add(new ListRow(headerItem, adapter));
    }

    protected boolean isEmptyCategory(Utils.SiteType checkType) {
        return !mCategory.containsKey(checkType) || mCategory.get(checkType) == null || mCategory.get(checkType).size() == 0;
    }

}

