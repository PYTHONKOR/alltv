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

import java.util.ArrayList;


public abstract class SiteProcessor {

    private Context mContext;

    protected String mAuthKey = new String();
    protected int mQualityType = 0;

    protected ArrayList<ChannelData> mChannelDatas = new ArrayList<>();
    protected ArrayList<CategoryData> mCategoryDatas = new ArrayList<>();

    public SiteProcessor(Context context) {
        this.mContext = context;
    }

    public abstract boolean doProcess(SettingsData inSettingsData);
    public abstract boolean updateProcess();

    public final String getAuthKey() {
        return mAuthKey;
    }
    public final void setAuthKey(String authkey) {
        mAuthKey = authkey;
    }

    public final String getStringById(int resourceId) {
        return mContext.getResources().getString(resourceId);
    }

    public final int getIntById(int resourceId) {
        return mContext.getResources().getInteger(resourceId);
    }

    public void setChannelList(ArrayList<ChannelData> channelList) {
        mChannelDatas.clear();
        mChannelDatas = channelList;
    }

    public ArrayList<ChannelData> getChannelList() {
        return mChannelDatas;
    }

    public void setCategorylList(ArrayList<CategoryData> categorylList) {
        mCategoryDatas.clear();
        mCategoryDatas = categorylList;
    }

    public ArrayList<CategoryData> getCategorylList() { return mCategoryDatas; }

}