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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;


public class OksusuRowSupportFragment extends AllTvBaseRowsSupportFragment implements OnItemViewClickedListener {

    private static SettingsData.OksusuQualityType mQualityType = SettingsData.OksusuQualityType.AUTO;

    public OksusuRowSupportFragment() {
        setOnItemViewClickedListener(this);

        mType = Utils.SiteType.Oksusu;
    }

    public static void setChannelList(ArrayList<ChannelData> chList) {
        AllTvBaseRowsSupportFragment.setChannelList(Utils.SiteType.Oksusu, chList);
    }

    public static void setCategoryList(ArrayList<CategoryData> ctList) {
        AllTvBaseRowsSupportFragment.setCategoryList(Utils.SiteType.Oksusu, ctList);
    }

    public static void setAuthKey(String authKey) {
        AllTvBaseRowsSupportFragment.setAuthKey(Utils.SiteType.Oksusu, authKey);
    }

    public static void setQualityType(SettingsData.OksusuQualityType inType) {
        if (mQualityType != inType)
            clearAllVideoUrl();

        mQualityType = inType;
    }

    public static void clearAllVideoUrl() {
        AllTvBaseRowsSupportFragment.clearAllVideoUrl(Utils.SiteType.Oksusu);
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {

        if (item instanceof ChannelData) {

            if (PlayerActivity.active) {
                return;
            }

            String authKey = mAuthKey.get(mType);

            if (authKey == null || authKey.length() < 10) {
                Utils.showToast(getContext(), getStringById(R.string.nologin_error));
                return;
            }

            OksusuFetchVideoUrlTask runTask = new OksusuFetchVideoUrlTask();
            runTask.execute(mChannels.get(mType).indexOf(item));
        }
    }

    @Override
    public void createRows() {

        mRowsAdapter.clear();

        if (isEmptyCategory(mType)) {
            createDefaultRows();
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
            return;
        }

        Log.e("createRows", "Oksusu");

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

        if(getMainFragmentAdapter() != null)
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
    }


    private class OksusuFetchVideoUrlTask extends FetchVideoUrlTask {
        protected Integer doInBackground(Integer... channelIndex) {
            getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mSpinnerFragment).commit();

            String authKey = mAuthKey.get(mType);
            ArrayList<ChannelData> chList = mChannels.get(mType);

            if (authKey == null || authKey.length() < 10)
                return Utils.Code.NoAuthKey_err.ordinal();

            int arrIndex = channelIndex[0];

            String url = getStringById(R.string.OKSUSUVIDEO_URL) + String.valueOf(chList.get(arrIndex).getId());

            HttpRequest request = HttpRequest.get(url)
                    .userAgent(getStringById(R.string.USERAGENT))
                    .header(getStringById(R.string.COOKIE_STR), authKey);

            String resultBody = request.body();


            Log.e("OksusuFetchVideoUrlTask", resultBody);


            if (resultBody.contains(getStringById(R.string.CONTENTINFO_STR))) {
                String jsonStr = resultBody.substring(resultBody.indexOf(getStringById(R.string.CONTENTINFO_STR)) + 14,
                        resultBody.indexOf(getStringById(R.string.OKSUSUJSONSUB_STR)));

                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(jsonStr);

                String videoUrl = Utils.removeQuote(jsonElement.getAsJsonObject().get(getStringById(R.string.STREAMURL_STR))
                        .getAsJsonObject().get(getQualityTag()).toString());

                if (videoUrl.equals("null") || videoUrl.length() == 0) {
                    return Utils.Code.NoVideoUrl_err.ordinal();
                }

                setVideoUrlByIndex(mType, arrIndex, videoUrl);
            }

            playVideo(chList.get(arrIndex));

            return Utils.Code.FetchVideoUrlTask_OK.ordinal();
        }

        private String getQualityTag() {
            switch (mQualityType) {
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
    }
}