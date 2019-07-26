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


public class PooqSiteProcessor extends SiteProcessor {

    public PooqSiteProcessor(Context context) {
        super(context);

        mChannelDatas.clear();
    }

    @Override
    public int getChannelStrId() {
        return R.string.POOQ_CHANNELS_STR;
    }

    @Override
    public int getCategoryStrId() {
        return R.string.POOQ_CATEGORY_STR;
    }

    @Override
    public int getAuthKeyStrId() {
        return R.string.POOQAUTHKEY_STR;
    }

    @Override
    public boolean doProcess(SettingsData inSettingsData) {

        if (mAuthKey == null || mAuthKey.length() == 0)
            doLogin(inSettingsData);

        getLiveTvList();

        return true;
    }

    private void getLiveTvList() {

        try {
            if (mAuthKey == null || mAuthKey.length() == 0)
                return;

            String resultJson = HttpRequest.get(getAppDataString(R.string.POOQ_CHANNELLIST_URL), true,
                    getAppDataString(R.string.DEVICETYPEID_STR), getAppDataString(R.string.PC_STR),
                    getAppDataString(R.string.MARKETTYPEID_STR), getAppDataString(R.string.GENERIC_STR),
                    getAppDataString(R.string.CREDENTIAL_STR), getAppDataString(R.string.POOQ_API_ACCESSKEY_STR),
                    getAppDataString(R.string.POOQ_CREDENTIAL_STR), mAuthKey)
                    .userAgent(getAppDataString(R.string.USERAGENT))
                    .body();

            if (resultJson == null || resultJson.equals(getAppDataString(R.string.NULL_STR)) || resultJson.length() == 0)
                return;

            JsonParser jParser = new JsonParser();
            JsonArray jArray = jParser.parse(resultJson).getAsJsonObject().
                    get(getAppDataString(R.string.RESULT_STR)).getAsJsonObject().getAsJsonArray(getAppDataString(R.string.LIST_STR));

            mChannelDatas.clear();
            mCategoryDatas.clear();

            for (JsonElement arr : jArray) {
                JsonObject categoryObj = arr.getAsJsonObject();

                CategoryData ctData = new CategoryData();

                int categoryId = categoryObj.get(getAppDataString(R.string.GENRECODE_STR)).getAsInt();
                ctData.setId(categoryId);
                ctData.setTitle(Utils.removeQuote(categoryObj.get(getAppDataString(R.string.GENRETITLE_STR)).getAsString()));

                mCategoryDatas.add(ctData);

                JsonArray chArray = arr.getAsJsonObject().getAsJsonArray(getAppDataString(R.string.LIST_STR));

                for (JsonElement chEle : chArray) {

                    JsonObject chObj = chEle.getAsJsonObject();

                    ChannelData chData = new ChannelData();

                    String channelName = Utils.removeQuote(chObj.get(getAppDataString(R.string.CHANNELTITLE_STR)).getAsString());
                    String programName = Utils.removeQuote(chObj.get(getAppDataString(R.string.TITLENAME_STR)).getAsString());

                    chData.setTitle(channelName);
                    chData.setProgram(programName);
                    chData.setStillImageUrl(Utils.removeQuote(chObj.get(getAppDataString(R.string.CHANNELIMAGE_STR)).getAsString()));
                    chData.setId(Utils.removeQuote(chObj.get(getAppDataString(R.string.ID_STR)).getAsString()));
                    chData.setCategoryId(categoryId);
                    chData.setAudioChannel(Utils.removeQuote(chObj.get(getAppDataString(R.string.ISRADIO_TAG)).getAsString())
                            .equals(getAppDataString(R.string.YES_STR)));

                    mChannelDatas.add(chData);
                }
            }
        } catch (java.lang.ArithmeticException ex) {
            mChannelDatas.clear();
            mCategoryDatas.clear();
        } finally {

        }
    }

    private void doLogin(SettingsData inSettingsData) {

        try {
            String requestUrl = getAppDataString(R.string.POOQ_LOGIN_URL) + "?" +
                    getAppDataString(R.string.MODE_STR) + "=" + getAppDataString(R.string.ID_STR) + "&" +
                    getAppDataString(R.string.ID_STR) + "=" + inSettingsData.mPooqSettings.mId + "&" +
                    getAppDataString(R.string.PASSWORD_STR) + "=" + inSettingsData.mPooqSettings.mPassword + "&" +
                    getAppDataString(R.string.POOQ_CREDENTIAL_STR) + "=" + getAppDataString(R.string.POOQ_CREDENTIAL_STR) + "&" +
                    getAppDataString(R.string.DEVICETYPEID_STR) + "=" + getAppDataString(R.string.PC_STR) + "&" +
                    getAppDataString(R.string.MARKETTYPEID_STR) + "=" + getAppDataString(R.string.GENERIC_STR) + "&" +
                    getAppDataString(R.string.CREDENTIAL_STR) + "=" + getAppDataString(R.string.POOQ_API_ACCESSKEY_STR);

            String resultJson = HttpRequest.post(requestUrl, true)
                    .userAgent(getAppDataString(R.string.USERAGENT))
                    .body();

            if (resultJson == null || resultJson.equals(getAppDataString(R.string.NULL_STR)) || resultJson.length() == 0)
                return;

            JsonParser parser = new JsonParser();

            JsonElement returnCode = parser.parse(resultJson).getAsJsonObject().get(getAppDataString(R.string.RETURNCODE_TAG));

            if (returnCode == null || returnCode.getAsInt() != getAppDataInt(R.integer.POOQ_SUCCESS_CODE)) {
                return;
            }

            mAuthKey = Utils.removeQuote(parser.parse(resultJson).getAsJsonObject().
                    get(getAppDataString(R.string.RESULT_STR)).getAsJsonObject().
                    get(getAppDataString(R.string.POOQ_CREDENTIAL_STR)).getAsString());
        } catch (java.lang.ArithmeticException ex) {
            mAuthKey = "";
        } finally {

        }
    }
}