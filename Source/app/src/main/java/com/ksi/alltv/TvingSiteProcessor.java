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

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TvingSiteProcessor extends SiteProcessor {

    private static final String TAG = TvingSiteProcessor.class.getSimpleName();

    public TvingSiteProcessor(Context context) {
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

        Long ts = System.currentTimeMillis();
        String timestamp = ts.toString();

        int i = 1;

        mChannelDatas.clear();
        mCategoryDatas.clear();

        ArrayList<ChannelData> channelData = new ArrayList<>();

        try {

            while (true) {

                HttpRequest request = HttpRequest.get("http://api.tving.com/v1/media/lives", true,
                        "pageNo", Integer.toString(i),
                        "pageSize", "20",
                        "order", "chno",
                        "adult", "all",
                        "free", "all",
                        "guest", "all",
                        "scope", "all",
                        "channelType", "CPCS0100",
                        "screenCode", "CSSD0100",
                        "networkCode", "CSND0900",
                        "osCode", "CSOD0900",
                        "teleCode", "CSCD0900",
                        "apiKey", "1e7952d0917d6aab1f0293a063697610",
                        "_", timestamp)
                        .userAgent(getStringById(R.string.USERAGENT))
                        .header("Cookie", mAuthKey);

                if (request == null || request.badRequest() || request.isBodyEmpty())
                    return false;

                String resultJson = request.body();

                JsonParser jParser = new JsonParser();
                JsonObject jObject = jParser.parse(resultJson).getAsJsonObject().getAsJsonObject("body");

                Boolean hasMore = jObject.getAsJsonPrimitive("has_more").getAsString().equals("Y");

                JsonArray jArray = jObject.getAsJsonArray("result");

                // schedule
                for (JsonElement arr : jArray) {

                    JsonArray jsonArray = arr.getAsJsonObject().getAsJsonObject("schedule").getAsJsonArray("broadcast_url");
                    String broad_url = jsonArray.get(0).getAsJsonObject().get("broad_url1").getAsString();

                    // DRM contents skip ...
                    if( broad_url.contains("/manifest")) {
                        continue;
                    }

                    JsonObject channelObj = arr.getAsJsonObject().getAsJsonObject("schedule").getAsJsonObject("channel");

                    String channelId = channelObj.get("code").getAsString();
                    String channelName = channelObj.getAsJsonObject("name").get("ko").getAsString();

                    String categoryCode = channelObj.get("category_code").getAsString().substring(2);
                    String categoryName = channelObj.getAsJsonObject("category_name").get("ko").getAsString();

                    int categoryId = Integer.parseInt(categoryCode);
                    Boolean isExist = false;

                    for(int j=0; j<mCategoryDatas.size(); j++) {
                        if(mCategoryDatas.get(j).getId() == categoryId) {
                            isExist = true;
                            break;
                        }
                    }

                    if(!isExist) {
                        CategoryData ctData = new CategoryData();

                        ctData.setId(categoryId);
                        ctData.setTitle(categoryName);

                        mCategoryDatas.add(ctData);
                    }

                    JsonObject programObj = arr.getAsJsonObject().getAsJsonObject("schedule").getAsJsonObject("program");

                    String programName = programObj.getAsJsonObject("name").get("ko").getAsString();

                    ChannelData chData = new ChannelData();

                    chData.setSiteType(Utils.SiteType.Tving.ordinal());
                    chData.setQualityType(mQualityType);
                    chData.setAuthKey(mAuthKey);

                    chData.setTitle(channelName);
                    chData.setProgramName(programName);
                    chData.setStillImageUrl("http://stillshot.tving.com/thumbnail/" + channelId + "_0_320x180.jpg");
                    chData.setId(channelId);
                    chData.setCategoryId(categoryId);
                    chData.setAudioChannel(false);

                    channelData.add(chData);
                }

                if (!hasMore) break;
                else i += 1;
            }

            for (CategoryData ctData : mCategoryDatas) {
                for (int j = 0; j<channelData.size(); j++) {
                    if (ctData.getId() == channelData.get(j).getCategoryId()) {
                        mChannelDatas.add(channelData.get(j));
                    }
                }
            }

        } catch(Exception ex) {
            mChannelDatas.clear();
            mCategoryDatas.clear();
            return false;
        } finally{
            if(!getEPGList())
                return false;
        }

        return true;
    }

    public boolean getEPGList() {

        Long ts = System.currentTimeMillis();
        String timestamp = ts.toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String broadDate = sdf.format(new Date());

        sdf = new SimpleDateFormat("HH0000");
        String startTime = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 3);
        String endTime = sdf.format(calendar.getTime());

        String channelCode[] =
                {"C06941,C07381,C07382,C04601,C00551,C00579,C00590,C01141,C00575,C01142,C01143,C00544,C01381,C01482,C01361,C06541,C15152,C00593,C01723,C01101",
                        "C15347,C01583,C17141,C01581,C00585,C17341,C00611,C01582,C15741,C00588,C00805,C00708,C17142,C05661,C22041"};

        try {

            for(int i=0; i<2; i++) {

                HttpRequest request = HttpRequest.get("http://api.tving.com/v1/media/schedules", true,
                        "pageNo", "1",
                        "pageSize", "20",
                        "order", "chno",
                        "scope", "all",
                        "adult", "n",
                        "free", "all",
                        "broadDate", broadDate,
                        "broadcastDate", broadDate,
                        "startBroadTime", startTime,
                        "endBroadTime", endTime,
                        "channelCode", channelCode[i],
                        "screenCode", "CSSD0100",
                        "networkCode", "CSND0900",
                        "osCode", "CSOD0900",
                        "teleCode", "CSCD0900",
                        "apiKey", "1e7952d0917d6aab1f0293a063697610",
                        "_", timestamp)
                        .userAgent(getStringById(R.string.USERAGENT));

                if (request == null || request.badRequest() || request.isBodyEmpty())
                    return false;

                String resultJson = request.body();

                JsonParser jParser = new JsonParser();
                JsonObject jObject = jParser.parse(resultJson).getAsJsonObject().getAsJsonObject("body");

                JsonArray jArray = jObject.getAsJsonArray("result");

                // Channels
                for (JsonElement arr : jArray) {

                    JsonObject channelObj = arr.getAsJsonObject();
                    String serviceId = Utils.removeQuote(channelObj.get("channel_code").getAsString());
                    String channelName = channelObj.get("channel_name").getAsJsonObject().get("ko").getAsString();

                    ChannelData channelData = null;

                    for(int j=0; j<mChannelDatas.size(); j++) {
                        if(mChannelDatas.get(j).getId().equals(serviceId)) {
                            channelData = mChannelDatas.get(j);
                            break;
                        }
                    }

                    if(channelData == null) continue;

                    JsonArray programs = channelObj.getAsJsonArray("schedules");

                    ArrayList<EPGData> epgData = new ArrayList<>();

                    for (JsonElement arr1 : programs) {

                        JsonObject programObj = arr1.getAsJsonObject();

                        String stimeStr = Utils.removeQuote(programObj.get("broadcast_start_time").getAsString());
                        String etimeStr = Utils.removeQuote(programObj.get("broadcast_end_time").getAsString());

                        Date stime = new Date(0);
                        Date etime = new Date(0);
                        try {
                            stime = new SimpleDateFormat("yyyyMMddHHmmss").parse(stimeStr);
                            etime = new SimpleDateFormat("yyyyMMddHHmmss").parse(etimeStr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        JsonElement jsonElement = null;

                        jsonElement = programObj.get("program");
                        String programName = jsonElement.getAsJsonObject().get("name").getAsJsonObject().get("ko").getAsString();

                        jsonElement = programObj.get("episode");
                        if (!jsonElement.isJsonNull()) {
                            JsonObject episodeObj = jsonElement.getAsJsonObject();
                            programName = programName + ", " + episodeObj.get("frequency").getAsString() + "화";
                        }

                        programName = Utils.removeHTMLTag(programName);

                        epgData.add(new EPGData(programName, stime, etime, false));
                    }

                    channelData.setEPG(epgData);
                }
            }

        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public boolean doLogin(SettingsData inSettingsData) {

        mAuthKey = "";
        mQualityType = inSettingsData.mTvingSettings.mQualityType.ordinal();

        if (inSettingsData.mTvingSettings.mId == null || inSettingsData.mTvingSettings.mPassword == null)
            return false;

        try {

            String loginType = inSettingsData.mTvingSettings.mCJOneId ? "10" : "20";

            Map<String, String> data = new HashMap<>();

            data.put("userId", inSettingsData.mTvingSettings.mId);
            data.put("password", inSettingsData.mTvingSettings.mPassword);
            data.put("loginType", loginType);

            HttpRequest postRequest = HttpRequest.post("https://user.tving.com/user/doLogin.tving", true)
                    .userAgent(getStringById(R.string.USERAGENT))
                    .form(data);

            if(postRequest == null || postRequest.badRequest() || postRequest.isBodyEmpty()) {
                return false;
            }

            String resultBody = postRequest.body();

            if(resultBody == null || resultBody.equals("null") || resultBody.length() == 0) {
                return false;
            }

            String[] receivedCookies = postRequest.headers("Set-Cookie");

            for(int i=0; i<receivedCookies.length; i++) {
                String cookie = receivedCookies[i];
                String decode = URLDecoder.decode(cookie, "utf-8");

                if( decode.startsWith("_tving_token") ) {
                    mAuthKey = decode.substring(0, decode.lastIndexOf("; Domain="));
                    break;
                }
            }

        } catch (Exception ex) {
            mAuthKey = "";
            return false;
        }

        return true;
    }
}