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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;


public class FetchChannelService extends IntentService {
    private static final String TAG = FetchChannelService.class.getSimpleName();

    private SiteProcessor mSiteProcessor;

    public FetchChannelService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            ResultReceiver channelResultReceiver = intent
                    .getParcelableExtra(getResources().getString(R.string.FETCHCHANNELRESULTRECEIVER_STR));

            Gson gson = new Gson();
            SettingsData receivedData = gson.fromJson(intent
                    .getStringExtra(getResources().getString(R.string.SETTINGSDATA_STR)), SettingsData.class);

            Utils.SiteType siteType = (Utils.SiteType) intent.getSerializableExtra(getResources().getString(R.string.SITETYPE_STR));

            if (siteType == Utils.SiteType.Oksusu) {
                mSiteProcessor = new OksusuSiteProcessor(getApplicationContext());
            } else if (siteType == Utils.SiteType.Pooq) {
                mSiteProcessor = new PooqSiteProcessor(getApplicationContext());
            }

            String authkey = (String) intent.getSerializableExtra(getResources().getString(R.string.AUTHKEY_STR));

            if (authkey == null || authkey.length() == 0)
                mSiteProcessor.setAuthKey(authkey);
            else
                mSiteProcessor.setAuthKey("");

            ArrayList<ChannelData> channels = new ArrayList<>();
            ArrayList<CategoryData> category = new ArrayList<>();

            if (mSiteProcessor.doProcess(receivedData)) {
                channels.addAll(mSiteProcessor.getChannelList());
                category.addAll(mSiteProcessor.getCategorylList());
            }

            int channelStrId = mSiteProcessor.getChannelStrId();
            int CategoryStrId = mSiteProcessor.getCategoryStrId();
            int AuthKeyStrId = mSiteProcessor.getAuthKeyStrId();

            Bundle retBundle = new Bundle();
            retBundle.putParcelableArrayList(getResources().getString(channelStrId), channels);
            retBundle.putParcelableArrayList(getResources().getString(CategoryStrId), category);
            retBundle.putString(getResources().getString(AuthKeyStrId), mSiteProcessor.getAuthKey());
            retBundle.putSerializable(getResources().getString(R.string.SITETYPE_STR), siteType);

            int retCode;
            String authKey = mSiteProcessor.getAuthKey();

            if(authKey == null || authKey.length() == 0) {
                retCode = Utils.Code.ServiceIntent_Fail.ordinal();
            } else {
                retCode = Utils.Code.ServiceIntent_OK.ordinal();
            }

            channelResultReceiver.send(retCode, retBundle);
        }
    }
}