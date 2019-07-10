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

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;

import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;


public class PooqRowSupportFragment extends AllTvBaseRowsSupportFragment implements OnItemViewClickedListener {

    private static SettingsData.PooqQualityType mQualityType = SettingsData.PooqQualityType.Mobile;

    public PooqRowSupportFragment() {
        setOnItemViewClickedListener(this);

        mType = Utils.SiteType.Pooq;
    }

    public static void setChannelList(ArrayList<ChannelData> chList) {
        AllTvBaseRowsSupportFragment.setChannelList(Utils.SiteType.Pooq, chList);
    }

    public static void setCategoryList(ArrayList<CategoryData> ctList) {
        AllTvBaseRowsSupportFragment.setCategoryList(Utils.SiteType.Pooq, ctList);
    }

    public static void setAuthKey(String authKey) {
        AllTvBaseRowsSupportFragment.setAuthKey(Utils.SiteType.Pooq, authKey);
    }

    public static void setQualityType(SettingsData.PooqQualityType inType) {
        if (mQualityType != inType)
            clearAllVideoUrl();

        mQualityType = inType;
    }

    public static void clearAllVideoUrl() {
        AllTvBaseRowsSupportFragment.clearAllVideoUrl(Utils.SiteType.Pooq);
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {

        if (item instanceof ChannelData) {

            String authKey = mAuthKey.get(Utils.SiteType.Pooq);

            if (authKey == null || authKey.length() < 10) {
                Utils.showToast(getContext(), getStringById(R.string.nologin_error));
                return;
            }

            PooqFetchVideoUrlTask runTask = new PooqFetchVideoUrlTask();
            runTask.execute(mChannels.get(mType).indexOf(item));
        }
    }

    @Override
    public void createRows() {

        mRowsAdapter.clear();

        if(isEmptyCategory(mType)) {
            createDefaultRows();
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
            return;
        }

        if(mAuthKey != null) {
            String authKey = mAuthKey.get(mType);
        }

        CardPresenter presenterSelector = new CardPresenter();
        ArrayList<ChannelData> chList = mChannels.containsKey(mType) ? new ArrayList<>(mChannels.get(mType)) : new ArrayList<>();
        Collections.reverse(chList);

        ArrayList<CategoryData> ctList = mCategory.containsKey(mType) ? mCategory.get(mType) : new ArrayList<>();

        for (CategoryData ctData : ctList) {

            ArrayObjectAdapter adapter = new ArrayObjectAdapter(presenterSelector);

            for (int i = (chList.size() - 1); i >= 0; i--) {
                if (ctData.getId() == chList.get(i).getCategoryId()) {
                    adapter.add(chList.get(i));
                    chList.remove(i);
                }
            }

            HeaderItem headerItem = new HeaderItem(ctData.getTitle());

            mRowsAdapter.add(new ListRow(headerItem, adapter));
        }

        getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
    }

    private class PooqFetchVideoUrlTask extends FetchVideoUrlTask {

        protected Integer doInBackground(Integer... channelIndex) {
            getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mSpinnerFragment).commit();

            String authKey = mAuthKey.get(mType);
            ArrayList<ChannelData> chList = mChannels.get(mType);

            if (authKey == null || authKey.length() < 10)
                return Utils.Code.NoAuthKey_err.ordinal();

            int arrIndex = channelIndex[0];

            String url = getStringById(R.string.POOQ_CHANNEL_URL) + chList.get(arrIndex).getId() + "/" + getStringById(R.string.URL_STR);

            String resultJson = HttpRequest.get(url, true,
                    getStringById(R.string.DEVICETYPEID_STR), getStringById(R.string.PC_STR),
                    getStringById(R.string.MARKETTYPEID_STR), getStringById(R.string.GENERIC_STR),
                    getStringById(R.string.POOQ_CREDENTIAL_STR), authKey,
                    getStringById(R.string.QUALITY_STR), getQualityTag(),
                    getStringById(R.string.DEVICEMODEID_STR), getStringById(R.string.PC_STR),
                    getStringById(R.string.AUTHTYPE_STR), getStringById(R.string.URL_STR)).
                    userAgent(getStringById(R.string.USERAGENT)).body();

            //Log.e("PooqFetchVideoUrlTask", resultJson);

            JsonParser parser = new JsonParser();

            String videoUrl = Utils.removeQuote(parser.parse(resultJson).getAsJsonObject().get(getStringById(R.string.RESULT_STR)).getAsJsonObject()
                    .get(getStringById(R.string.SIGNEDURL_STR)).getAsString());

            if (videoUrl == null || videoUrl.equals(getStringById(R.string.NULL_STR)) || videoUrl.length() == 0) {
                return Utils.Code.NoVideoUrl_err.ordinal();
            }

            setVideoUrlByIndex(Utils.SiteType.Pooq, arrIndex, videoUrl);

            playVideo(chList.get(arrIndex));

            return Utils.Code.FetchVideoUrlTask_OK.ordinal();
        }

        private String getQualityTag() {
            switch (mQualityType) {
                case Mobile:
                    return getStringById(R.string.POOQ_MOBILE_QUALITY_TAG);
                case SD:
                    return getStringById(R.string.POOQ_SD_QUALITY_TAG);
                case HD:
                    return getStringById(R.string.POOQ_HD_QUALITY_TAG);
                case FHD:
                    return getStringById(R.string.POOQ_FHD_QUALITY_TAG);
            }

            return getStringById(R.string.URLAUTO_STR);
        }


    }
}