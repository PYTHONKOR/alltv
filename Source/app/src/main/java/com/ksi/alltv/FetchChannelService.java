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

import com.google.gson.Gson;

import java.util.ArrayList;


public class FetchChannelService extends IntentService {
    private static final String TAG = FetchChannelService.class.getSimpleName();

    private SiteProcessor mSiteProcessor;

    public FetchChannelService() {
        super(TAG);
    }

    private String getStringById(int resourceId) {
        return this.getResources().getString(resourceId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            ResultReceiver channelResultReceiver = intent
                    .getParcelableExtra(getStringById(R.string.FETCHCHANNELRESULTRECEIVER_STR));

            Gson gson = new Gson();
            SettingsData settingsData = gson.fromJson(intent
                    .getStringExtra(getStringById(R.string.SETTINGSDATA_STR)), SettingsData.class);

            Utils.SiteType siteType = (Utils.SiteType) intent.getSerializableExtra(getStringById(R.string.SITETYPE_STR));

            if (siteType == Utils.SiteType.Pooq) {
                mSiteProcessor = new PooqSiteProcessor(getApplicationContext());
            } else if (siteType == Utils.SiteType.Tving) {
                mSiteProcessor = new TvingSiteProcessor(getApplicationContext());
            } else if (siteType == Utils.SiteType.Oksusu) {
                mSiteProcessor = new OksusuSiteProcessor(getApplicationContext());
            }

            String authkey = (String) intent.getSerializableExtra(getStringById(R.string.AUTHKEY_STR));

            if (authkey == null || authkey.length() == 0) {
                mSiteProcessor.setAuthKey("");
            } else {
                mSiteProcessor.setAuthKey(authkey);
            }

            String mode = (String) intent.getSerializableExtra(getStringById(R.string.FETCHMODE_STR));

            ArrayList<ChannelData> channels = new ArrayList<>();
            ArrayList<CategoryData> category = new ArrayList<>();
            boolean error = false;

            if(mode.equals("create")) {
                if (mSiteProcessor.doProcess(settingsData)) {
                    channels = mSiteProcessor.getChannelList();
                    category = mSiteProcessor.getCategorylList();
                } else {
                    error = true;
                }
            } else if(mode.equals("refresh")) {
                channels = intent.getParcelableArrayListExtra(getStringById(R.string.CHANNELS_STR));
                category = intent.getParcelableArrayListExtra(getStringById(R.string.CATEGORY_STR));

                mSiteProcessor.setChannelList(channels);
                mSiteProcessor.setCategorylList(category);

                if(!mSiteProcessor.updateProcess()) {
                    error = true;
                }
            }

            Bundle retBundle = new Bundle();
            retBundle.putParcelableArrayList(getStringById(R.string.CHANNELS_STR), channels);
            retBundle.putParcelableArrayList(getStringById(R.string.CATEGORY_STR), category);
            retBundle.putString(getStringById(R.string.AUTHKEY_STR), mSiteProcessor.getAuthKey());
            retBundle.putSerializable(getStringById(R.string.SITETYPE_STR), siteType);
            retBundle.putString(getStringById(R.string.FETCHMODE_STR), mode);

            int retCode;

            if(error) retCode = Utils.Code.ServiceIntent_Fail.ordinal();
            else      retCode = Utils.Code.ServiceIntent_OK.ordinal();

            channelResultReceiver.send(retCode, retBundle);
        }
    }
}