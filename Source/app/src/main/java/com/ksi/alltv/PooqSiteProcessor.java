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

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class PooqSiteProcessor extends SiteProcessor {

    private static final String TAG = PooqSiteProcessor.class.getSimpleName();

    public PooqSiteProcessor(Context context) {
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

        mQualityType = inSettingsData.mPooqSettings.mQualityType.ordinal();

        try {

            HttpRequest request = HttpRequest.get(getStringById(R.string.POOQ_CHANNELLIST_URL), true,
                    getStringById(R.string.DEVICETYPEID_STR), getStringById(R.string.PC_STR),
                    getStringById(R.string.MARKETTYPEID_STR), getStringById(R.string.GENERIC_STR),
                    getStringById(R.string.CREDENTIAL_STR), getStringById(R.string.POOQ_API_ACCESSKEY_STR),
                    getStringById(R.string.POOQ_CREDENTIAL_STR), mAuthKey)
                    .userAgent(getStringById(R.string.USERAGENT));

            if(request == null || request.badRequest() || request.isBodyEmpty())
                return false;

            String resultJson = request.body();

            JsonParser jParser = new JsonParser();
            JsonArray jArray = jParser.parse(resultJson).getAsJsonObject().
                    get(getStringById(R.string.RESULT_STR)).getAsJsonObject().getAsJsonArray(getStringById(R.string.LIST_STR));

            mChannelDatas.clear();
            mCategoryDatas.clear();

            for (JsonElement arr : jArray) {
                JsonObject categoryObj = arr.getAsJsonObject();

                CategoryData ctData = new CategoryData();

                int categoryId = categoryObj.get(getStringById(R.string.GENRECODE_STR)).getAsInt();
                ctData.setId(categoryId);
                ctData.setTitle(Utils.removeQuote(categoryObj.get(getStringById(R.string.GENRETITLE_STR)).getAsString()));

                mCategoryDatas.add(ctData);

                JsonArray chArray = arr.getAsJsonObject().getAsJsonArray(getStringById(R.string.LIST_STR));

                for (JsonElement chEle : chArray) {

                    JsonObject chObj = chEle.getAsJsonObject();

                    ChannelData chData = new ChannelData();

                    String channelName = Utils.removeQuote(chObj.get(getStringById(R.string.CHANNELTITLE_STR)).getAsString());
                    String programName = Utils.removeQuote(chObj.get(getStringById(R.string.TITLENAME_STR)).getAsString());

                    chData.setSiteType(Utils.SiteType.Pooq.ordinal());
                    chData.setQualityType(mQualityType);
                    chData.setAuthKey(mAuthKey);
                    chData.setTitle(channelName);
                    chData.setProgramName(programName);
                    chData.setStillImageUrl(Utils.removeQuote(chObj.get(getStringById(R.string.CHANNELIMAGE_STR)).getAsString()));
                    chData.setId(Utils.removeQuote(chObj.get(getStringById(R.string.ID_STR)).getAsString()));
                    chData.setCategoryId(categoryId);
                    chData.setAudioChannel(Utils.removeQuote(chObj.get(getStringById(R.string.ISRADIO_TAG)).getAsString())
                            .equals(getStringById(R.string.YES_STR)));

                    mChannelDatas.add(chData);
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

    public boolean getEPGList() {

        Long ts = System.currentTimeMillis();
        String timestamp = ts.toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00");
        String startTime = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 6);
        String endTime = sdf.format(calendar.getTime());

        // https://apis.pooq.co.kr/live/epgs?apikey=E5F3E0D30947AA5440556471321BB6D9
        // &device=pc
        // &partner=pooq
        // &region=kor
        // &targetage=auto
        // &credential=none
        // &pooqzone=none
        // &drm=wm
        // &genre=all
        // &startdatetime=2019-07-29+12:00
        // &enddatetime=2019-07-29+15:00
        // &offset=0&limit=100

        try {

            HttpRequest request = HttpRequest.get(getStringById(R.string.POOQ_EPG_URL), true,
                    "apikey", getStringById(R.string.POOQ_API_ACCESSKEY_STR),
                    "device", getStringById(R.string.PC_STR),
                    "partner", "pooq",
                    "region", "kor",
                    "targetage", "auto",
                    "credential", "none",
                    "pooqzone", "none",
                    "drm", "wm",
                    "genre", "all",
                    "startdatetime", startTime,
                    "enddatetime", endTime,
                    "offset", "0",
                    "limit", "100")
                    .userAgent(getStringById(R.string.USERAGENT));

            if(request == null || request.badRequest() || request.isBodyEmpty())
                return false;

            String resultJson = request.body();

            JsonParser jParser = new JsonParser();
            JsonArray jArray = jParser.parse(resultJson).getAsJsonObject().getAsJsonArray(getStringById(R.string.LIST_STR));

            // Channels
            for (JsonElement arr : jArray) {
                JsonObject channelObj = arr.getAsJsonObject();

                String serviceId = Utils.removeQuote(channelObj.get("channelid").getAsString());

                ChannelData channelData = null;

                for(int i=0; i<mChannelDatas.size(); i++) {
                    if(mChannelDatas.get(i).getId().equals(serviceId)) {
                        channelData = mChannelDatas.get(i);
                        break;
                    }
                }

                if(channelData == null) {
    //                Log.e(TAG, serviceId + ", " + channelName + " is not exist.");
                    continue;
                }

                JsonArray programs = channelObj.getAsJsonArray(getStringById(R.string.LIST_STR));

                ArrayList<EPGData> epgData = new ArrayList<>();

                for (JsonElement arr1 : programs) {
                    JsonObject programObj = arr1.getAsJsonObject();

                    String programName = Utils.removeHTMLTag(programObj.get("title").getAsString());
                    String stimeStr = Utils.removeQuote(programObj.get("starttime").getAsString());
                    String etimeStr = Utils.removeQuote(programObj.get("endtime").getAsString());

                    Date stime = new Date(0);
                    Date etime = new Date(0);
                    try {
                        stime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(stimeStr);
                        etime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(etimeStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    epgData.add(new EPGData(programName, stime, etime, false));
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
        mQualityType = inSettingsData.mPooqSettings.mQualityType.ordinal();

        if (inSettingsData.mPooqSettings.mId == null || inSettingsData.mPooqSettings.mPassword == null)
            return false;

        try {

            String requestUrl = getStringById(R.string.POOQ_LOGIN_URL) + "?" +
                    getStringById(R.string.MODE_STR) + "=" + getStringById(R.string.ID_STR) + "&" +
                    getStringById(R.string.ID_STR) + "=" + inSettingsData.mPooqSettings.mId + "&" +
                    getStringById(R.string.PASSWORD_STR) + "=" + URLEncoder.encode(inSettingsData.mPooqSettings.mPassword, getStringById(R.string.UTF8_STR)) + "&" +
                    getStringById(R.string.POOQ_CREDENTIAL_STR) + "=" + getStringById(R.string.POOQ_CREDENTIAL_STR) + "&" +
                    getStringById(R.string.DEVICETYPEID_STR) + "=" + getStringById(R.string.PC_STR) + "&" +
                    getStringById(R.string.MARKETTYPEID_STR) + "=" + getStringById(R.string.GENERIC_STR) + "&" +
                    getStringById(R.string.CREDENTIAL_STR) + "=" + getStringById(R.string.POOQ_API_ACCESSKEY_STR);

            HttpRequest postRequest = HttpRequest.post(requestUrl, false)
                    .userAgent(getStringById(R.string.USERAGENT));

            if(postRequest == null || postRequest.badRequest() || postRequest.isBodyEmpty())
                return false;

            String resultJson = postRequest.body();

            if(resultJson == null || resultJson.equals(getStringById(R.string.NULL_STR)) || resultJson.length() == 0)
                return false;

            JsonParser parser = new JsonParser();

            JsonElement returnCode = parser.parse(resultJson).getAsJsonObject().get(getStringById(R.string.RETURNCODE_TAG));

            if (returnCode == null || returnCode.getAsInt() != getIntById(R.integer.POOQ_SUCCESS_CODE)) {
                return false;
            }

            mAuthKey = Utils.removeQuote(parser.parse(resultJson).getAsJsonObject().
                    get(getStringById(R.string.RESULT_STR)).getAsJsonObject().
                    get(getStringById(R.string.POOQ_CREDENTIAL_STR)).getAsString());

        } catch (Exception ex) {
            mAuthKey = "";
            return false;
        }

        return true;
    }
}