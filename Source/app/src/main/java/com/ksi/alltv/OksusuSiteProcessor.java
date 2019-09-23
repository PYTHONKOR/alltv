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
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class OksusuSiteProcessor extends SiteProcessor {
    private static final String TAG = OksusuSiteProcessor.class.getSimpleName();

    public OksusuSiteProcessor(Context context) {
        super(context);
        mChannelDatas.clear();
    }

    @Override
    public boolean doProcess(SettingsData inSettingsData) {

        if (mAuthKey == null || mAuthKey.length() == 0)
            if(!doLogin(inSettingsData))
                return false;

        return getLiveTvList(inSettingsData);
    }

    @Override
    public boolean updateProcess() {
        return getEPGList();
    }

    private boolean getLiveTvList(SettingsData inSettingsData) {

        if (mAuthKey == null || mAuthKey.length() == 0)
            return false;

        mQualityType = inSettingsData.mOksusuSettings.mQualityType.ordinal();

        try {

            HttpRequest request = HttpRequest.get(getStringById(R.string.OKSUSU_CHANNEL_URL))
                                .userAgent(getStringById(R.string.USERAGENT))
                                .header(getStringById(R.string.COOKIE_STR), mAuthKey);

            if(request == null || request.badRequest() || request.isBodyEmpty())
                return false;

            String resultJson = request.body();

            JsonParser jParser = new JsonParser();
            JsonArray jArray = jParser.parse(resultJson).getAsJsonObject().getAsJsonArray(getStringById(R.string.CHANNELS_STR));

            mChannelDatas.clear();
            mCategoryDatas.clear();

            ArrayList<ChannelData> chList = new ArrayList<>();

            // Channels
            for (JsonElement arr : jArray) {
                JsonObject channelObj = arr.getAsJsonObject();

                if (channelObj.get(getStringById(R.string.AUTOURL_TAG)).isJsonNull())
                    continue;

                ChannelData chData = new ChannelData();

                chData.setSiteType(Utils.SiteType.Oksusu.ordinal());
                chData.setQualityType(mQualityType);
                chData.setAuthKey(mAuthKey);

                String channelName = Utils.removeQuote(channelObj.get(getStringById(R.string.CHANNELNAME_TAG)).getAsString());

                chData.setTitle(channelName);

                JsonArray programs = channelObj.getAsJsonArray(getStringById(R.string.PROGRAMS_TAG));

                if(programs.size() > 0) {
                    String programName = Utils.removeQuote(programs.get(0).getAsJsonObject().get(getStringById(R.string.PROGRAMNAME_TAG)).getAsString());
                    chData.setProgramName(programName);
                }

                String stillImageUrl = Utils.removeQuote(channelObj.get(getStringById(R.string.STILLIMAGE_TAG)).getAsString());

                if (!channelObj.get(getStringById(R.string.UNDER19CONTENT_TAG)).getAsBoolean()) {
                    stillImageUrl = getStringById(R.string.OKSUSULOGO_URL) +
                            Utils.removeQuote(channelObj.get(getStringById(R.string.CHANNELIMAGENAME_TAG)).getAsString());
                }

                chData.setStillImageUrl(stillImageUrl);
                chData.setId(Utils.removeQuote(channelObj.get(getStringById(R.string.SERVICEID_TAG)).getAsString()));
                chData.setCategoryId(channelObj.get(getStringById(R.string.CHANNELCATEGORY_TAG)).getAsInt());

                chList.add(chData);
            }

            // Category
            jArray = jParser.parse(resultJson).getAsJsonObject().getAsJsonArray(getStringById(R.string.JSONCATEGORY_TAG));

            int adultCdNo = -1;

            for (JsonElement arr : jArray) {
                JsonObject channelObj = arr.getAsJsonObject();
                JsonElement cdNo = channelObj.get(getStringById(R.string.CATEGORYNO_TAG));
                JsonElement cdTitle = channelObj.get(getStringById(R.string.CATEGORYTITLE_TAG));

                if(!cdNo.isJsonNull() && !cdTitle.isJsonNull()) {
                    if(cdTitle.getAsString().equals(getStringById(R.string.adult))) {
                        adultCdNo = cdNo.getAsInt();
                    } else {
                        CategoryData ctData = new CategoryData();
                        ctData.setId(cdNo.getAsInt());
                        ctData.setTitle(Utils.removeQuote(cdTitle.getAsString()));
                        mCategoryDatas.add(ctData);
                    }
                }
            }

            if(adultCdNo > 0) {
                CategoryData ctData = new CategoryData();
                ctData.setId(adultCdNo);
                ctData.setTitle(getStringById(R.string.adult));
                mCategoryDatas.add(ctData);
            }

            ArrayList<CategoryData> ctList = mCategoryDatas;
            Collections.reverse(chList);

            for (CategoryData ctData : ctList) {
                for (int i = (chList.size() - 1); i >= 0; i--) {
                    if (ctData.getId() == chList.get(i).getCategoryId()) {
                        mChannelDatas.add(chList.get(i));
                    }
                }
            }

        } catch (Exception ex) {
            mChannelDatas.clear();
            mCategoryDatas.clear();

            return false;
        } finally {
            if(!getEPGList())
                return false;
        }

        return true;
    }

    private boolean getEPGList() {

        Long ts = System.currentTimeMillis();
        String timestamp = ts.toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        String startTime = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 6);
        String endTime = sdf.format(calendar.getTime());

        // http://www.oksusu.com/api/live/channel?startTime=2019072912&endTime=2019072915&_=1564372401994

        try {

            HttpRequest request = HttpRequest.get(getStringById(R.string.OKSUSU_EPG_URL), true,
                    "startTime", startTime, "endTime", endTime, "_", timestamp)
                    .userAgent(getStringById(R.string.USERAGENT));

            if(request == null || request.badRequest() || request.isBodyEmpty())
                return false;

            String resultJson = request.body();

            JsonParser jParser = new JsonParser();
            JsonArray jArray = jParser.parse(resultJson).getAsJsonObject().getAsJsonArray(getStringById(R.string.CHANNELS_STR));

            // Channels
            for (JsonElement arr : jArray) {
                JsonObject channelObj = arr.getAsJsonObject();

                if (channelObj.get(getStringById(R.string.AUTOURL_TAG)).isJsonNull())
                    continue;

                String serviceId = Utils.removeQuote(channelObj.get(getStringById(R.string.SERVICEID_TAG)).getAsString());

                ChannelData channelData = null;

                for(int i=0; i<mChannelDatas.size(); i++) {
                    if(mChannelDatas.get(i).getId().equals(serviceId)) {
                        channelData = mChannelDatas.get(i);
                        break;
                    }
                }

                if(channelData == null) {
    //                Log.e(TAG, serviceId + ", " + channelName);
                    continue;
                }

                JsonArray programs = channelObj.getAsJsonArray(getStringById(R.string.PROGRAMS_TAG));

                ArrayList<EPGData> epgData = new ArrayList<>();

                for (JsonElement arr1 : programs) {
                    JsonObject programObj = arr1.getAsJsonObject();

                    String programName;
                    Boolean adultContent = programObj.get(getStringById(R.string.ADULTCONTENT_TAG)).getAsBoolean();

                    if(adultContent)
                        programName = getStringById(R.string.adult_contents);
                    else
                        programName = Utils.removeHTMLTag(programObj.get(getStringById(R.string.PROGRAMNAME_TAG)).getAsString());

                    String stime = Utils.removeQuote(programObj.get("startTime").getAsString());
                    String etime = Utils.removeQuote(programObj.get("endTime").getAsString());

                    epgData.add(new EPGData(programName,
                                new Date(Long.parseLong(stime)),
                                new Date(Long.parseLong(etime)),
                                adultContent));
                }

                channelData.setEPG(epgData);
            }

        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    private boolean doLogin(SettingsData inSettingsData) {

        mAuthKey = "";
        mQualityType = inSettingsData.mOksusuSettings.mQualityType.ordinal();

        if (inSettingsData.mOksusuSettings.mId == null || inSettingsData.mOksusuSettings.mPassword == null)
            return false;

        try {
            Map<String, String> data = new HashMap<>();

            data.put(getStringById(R.string.USERID_STR), inSettingsData.mOksusuSettings.mId);
            data.put(getStringById(R.string.PASSWORD_STR), inSettingsData.mOksusuSettings.mPassword);
            data.put(getStringById(R.string.LOGINMODE_STR), "1");
            data.put(getStringById(R.string.RW_STR), "/");
            data.put(getStringById(R.string.SERVICEPROVIDE_STR), "");
            data.put(getStringById(R.string.ACCESSTOKEN_STR), "");

            HttpRequest postRequest = HttpRequest.post(getStringById(R.string.OKSUSU_LOGIN_URL), true)
                    .userAgent(getStringById(R.string.USERAGENT))
                    .form(data);

            if (postRequest == null || postRequest.badRequest() || postRequest.isBodyEmpty())
                return false;

            String receivedCookies = postRequest.header(getStringById(R.string.SETCOOKIE_STR));

            if (receivedCookies != null && receivedCookies.startsWith(getStringById(R.string.CORNAC_STR))) {
                mAuthKey = receivedCookies.substring(receivedCookies
                        .lastIndexOf(getStringById(R.string.CORNAC_STR)), receivedCookies.lastIndexOf(getStringById(R.string.DOMAIN_STR)));
            }
        }  catch (Exception ex) {
            mAuthKey = "";
            return false;
        }

        return true;
    }
}
