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

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;


public class OksusuSiteProcessor extends SiteProcessor {
    private static final String TAG = OksusuSiteProcessor.class.getSimpleName();

    public OksusuSiteProcessor(Context context) {
        super(context);

        mChannelDatas.clear();
    }

    @Override
    public int getChannelStrId() {
        return R.string.OKSUSU_CHANNELS_STR;
    }

    @Override
    public int getCategoryStrId() {
        return R.string.OKSUSU_CATEGORY_STR;
    }

    @Override
    public int getAuthKeyStrId() {
        return R.string.OKSUSUAUTHKEY_STR;
    }

    @Override
    public boolean doProcess(SettingsData inSettingsData) {

        if (mAuthKey == null || mAuthKey.length() == 0)
            doLogin(inSettingsData);

        getLiveTvList();

        return true;
    }

    private void getLiveTvList() {

        if (mAuthKey == null || mAuthKey.length() == 0)
            return;

        String resultHtml = HttpRequest.get(getAppDataString(R.string.OKSUSU_CHANNEL_URL))
                            .userAgent(getAppDataString(R.string.USERAGENT))
                            .body();

        if(resultHtml == null || resultHtml.equals(getAppDataString(R.string.NULL_STR)) || resultHtml.length() == 0)
            return;

        JsonParser jParser = new JsonParser();
        JsonArray jArray = jParser.parse(resultHtml).getAsJsonObject().getAsJsonArray(getAppDataString(R.string.CHANNELS_TAG));

        mChannelDatas.clear();
        mCategoryDatas.clear();

        // Channels
        for (JsonElement arr : jArray) {
            JsonObject channelObj = arr.getAsJsonObject();

            if (channelObj.get(getAppDataString(R.string.AUTOURL_TAG)).isJsonNull())
                continue;

            ChannelData chData = new ChannelData();

            String channelName = Utils.removeQuote(channelObj.get(getAppDataString(R.string.CHANNELNAME_TAG)).getAsString());

            chData.setTitle(channelName);

            JsonArray programs = channelObj.getAsJsonArray(getAppDataString(R.string.PROGRAMS_TAG));
            String programName = Utils.removeQuote(programs.get(0).getAsJsonObject().get(getAppDataString(R.string.PROGRAMNAME_TAG)).getAsString());

            chData.setProgram(programName);

            String stillImageUrl = Utils.removeQuote(channelObj.get(getAppDataString(R.string.STILLIMAGE_TAG)).getAsString());

            if (!channelObj.get(getAppDataString(R.string.UNDER19CONTENT_TAG)).getAsBoolean()) {
                stillImageUrl = getAppDataString(R.string.OKSUSULOGO_URL) +
                        Utils.removeQuote(channelObj.get(getAppDataString(R.string.CHANNELIMAGENAME_TAG)).getAsString());
            }

            chData.setStillImageUrl(stillImageUrl);
            chData.setId(Utils.removeQuote(channelObj.get(getAppDataString(R.string.SERVICEID_TAG)).getAsString()));
            chData.setCategoryId(channelObj.get(getAppDataString(R.string.CHANNELCATEGORY_TAG)).getAsInt());

            mChannelDatas.add(chData);
        }

        // Category
        jArray = jParser.parse(resultHtml).getAsJsonObject().getAsJsonArray(getAppDataString(R.string.JSONCATEGORY_TAG));

        for (JsonElement arr : jArray) {
            JsonObject channelObj = arr.getAsJsonObject();

            CategoryData ctData = new CategoryData();

            ctData.setId(channelObj.get(getAppDataString(R.string.CATEGORYNO_TAG)).getAsInt());
            ctData.setTitle(Utils.removeQuote(channelObj.get(getAppDataString(R.string.CATEGORYTITLE_TAG)).getAsString()));

            mCategoryDatas.add(ctData);
        }
    }

    private void doLogin(SettingsData inSettingsData) {

        if (inSettingsData.mOksusuSettings.mId == null || inSettingsData.mOksusuSettings.mPassword == null)
            return;

        Map<String, String> data = new HashMap<>();

        data.put(getAppDataString(R.string.USERID_STR), inSettingsData.mOksusuSettings.mId);
        data.put(getAppDataString(R.string.PASSWORD_STR), inSettingsData.mOksusuSettings.mPassword);
        data.put(getAppDataString(R.string.LOGINMODE_STR), "1");
        data.put(getAppDataString(R.string.RW_STR), "/");
        data.put(getAppDataString(R.string.SERVICEPROVIDE_STR), "");
        data.put(getAppDataString(R.string.ACCESSTOKEN_STR), "");

        HttpRequest postRequest = HttpRequest.post(getAppDataString(R.string.OKSUSU_LOGIN_URL))
                                    .userAgent(getAppDataString(R.string.USERAGENT))
                                    .form(data);

        if(postRequest == null || postRequest.isBodyEmpty())
            return;

        String receivedCookies = postRequest.header(getAppDataString(R.string.SETCOOKIE_STR));

        if (receivedCookies != null && receivedCookies.startsWith(getAppDataString(R.string.CORNAC_STR))) {
            mAuthKey = receivedCookies.substring(receivedCookies
                    .lastIndexOf(getAppDataString(R.string.CORNAC_STR)), receivedCookies.lastIndexOf(getAppDataString(R.string.DOMAIN_STR)));
        }
    }
}
