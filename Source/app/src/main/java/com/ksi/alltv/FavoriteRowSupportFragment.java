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
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;


public class FavoriteRowSupportFragment extends AllTvBaseRowsSupportFragment implements OnItemViewClickedListener {

    private static final String TAG = FavoriteRowSupportFragment.class.getSimpleName();

    public FavoriteRowSupportFragment() {
        setOnItemViewClickedListener(this);
        mType = Utils.SiteType.Favorite;
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof ChannelData) {
            String authKey = ((ChannelData) item).getAuthkey();

            if (authKey == null || authKey.length() < 10) {
                Utils.showToast(getContext(), getStringById(R.string.nologin_error));
                return;
            }
            playVideo(mChannels.get(mType).indexOf(item));
        }
    }

    private void playVideo(int currentChannel) {
        if (PlayerActivity.active) return;

        int requestCode = Utils.Code.FavoritePlay.ordinal();
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
        intent.putParcelableArrayListExtra(getStringById(R.string.CHANNELS_TAG), mChannels.get(mType));
        intent.putExtra(getStringById(R.string.CURRENTCHANNEL_STR), currentChannel);

        getActivity().startActivityForResult(intent, requestCode);
    }

    @Override
    public void createRows() {

        mRowsAdapter.clear();

        if (isEmptyCategory(Utils.SiteType.Oksusu) && isEmptyCategory(Utils.SiteType.Pooq)) {
            return;
        }

        CardPresenter presenterSelector = new CardPresenter();
        ArrayObjectAdapter adapter;

        ArrayList<ChannelData> chList;
        ArrayList<ChannelData> chFavorites = new ArrayList<>();

        if (mChannels.get(Utils.SiteType.Pooq) != null) {
            chList = mChannels.get(Utils.SiteType.Pooq);
            adapter = new ArrayObjectAdapter(presenterSelector);

            for (int i = 0; i < chList.size(); i++) {
                ChannelData data = new ChannelData(chList.get(i));
                if (data.getFavorite() > 0) {
                    data.setFavorite(3);
                    chFavorites.add(data);
                    adapter.add(data);
                }
            }
            if (adapter.size() > 0)
                mRowsAdapter.add(new ListRow(new HeaderItem("POOQ"), adapter));
        }

        if (mChannels.get(Utils.SiteType.Oksusu) != null) {
            chList = mChannels.get(Utils.SiteType.Oksusu);
            adapter = new ArrayObjectAdapter(presenterSelector);

            for (int i = 0; i < chList.size(); i++) {
                ChannelData data = new ChannelData(chList.get(i));
                if (data.getFavorite() > 0) {
                    data.setFavorite(2);
                    chFavorites.add(data);
                    adapter.add(data);
                }
            }
            if (adapter.size() > 0)
                mRowsAdapter.add(new ListRow(new HeaderItem("옥수수"), adapter));
        }

        if (chFavorites.size() == 0) {
            adapter = new ArrayObjectAdapter(presenterSelector);
            mRowsAdapter.add(new ListRow(new HeaderItem("선호채널을 등록해 주세요."), adapter));
        }

        AllTvBaseRowsSupportFragment.setChannelList(Utils.SiteType.Favorite, chFavorites);

        if (getMainFragmentAdapter() != null)
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
    }

    @Override
    public void refreshRows() {
        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
    }

    @Override
    public void sendChannelData() {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
        intent.putParcelableArrayListExtra(getStringById(R.string.CHANNELS_TAG), mChannels.get(mType));

        getActivity().startActivity(intent);
    }
}
