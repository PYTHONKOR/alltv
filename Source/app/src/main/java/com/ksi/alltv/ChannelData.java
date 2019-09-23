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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class ChannelData implements Parcelable {

    private int mSiteType;
    private int mQualityType;
    private String mAuthKey = "";
    private String mId = "";
    private int mCategoryId;
    private String mTitle = "";
    private String mProgramName = "";
    private String mStillImageUrl = "";
    private Boolean mAudioChannel = false;
    private int mFavorite = 0;
    private int mItemIndex = 0;
    private ArrayList<EPGData> mEPG = new ArrayList<>();

    public int getSiteType() { return mSiteType; }
    public void setSiteType(int type) {
        this.mSiteType = type;
    }

    public int getQualityType() {
        return mQualityType;
    }
    public void setQualityType(int type) {
        this.mQualityType = type;
    }

    public String getAuthkey() {
        return mAuthKey;
    }
    public void setAuthKey(String key) {
        this.mAuthKey = key;
    }

    public String getId() { return mId; }
    public void setId(String id) {
        this.mId = id;
    }

    public int getCategoryId() {
        return mCategoryId;
    }
    public void setCategoryId(int id) {
        this.mCategoryId = id;
    }

    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getProgramName() {
        return mProgramName;
    }
    public void setProgramName(String name) {
        this.mProgramName = name;
    }

    public String getStillImageUrl() {
        return mStillImageUrl;
    }
    public void setStillImageUrl(String imageUrl) {
        this.mStillImageUrl = imageUrl;
    }

    public Boolean isAudioChannel() {
        return mAudioChannel;
    }
    public void setAudioChannel(Boolean value) {
        this.mAudioChannel = value;
    }

    public int getFavorite() {
        return mFavorite;
    }
    public void setFavorite(int favorite) {
        this.mFavorite = favorite;
    }

    public int getItemIndex() {
        return mItemIndex;
    }
    public void setItemIndex(int index) {
        this.mItemIndex = index;
    }

    public ArrayList<EPGData> getEPG() {
        return mEPG;
    }

    public void setEPG(ArrayList<EPGData> epg) {

        if(!this.mEPG.isEmpty()) {
            this.mEPG.clear();
        }

        this.mEPG = epg;
    }

    public void setEPG(ArrayList<EPGData> epg, boolean clone) {

        if(!this.mEPG.isEmpty())
            this.mEPG.clear();

        if(clone) {
            for(int i=0; i<epg.size(); i++) {
                this.mEPG.add(new EPGData(epg.get(i)));
            }
        } else {
            this.mEPG = epg;
        }
    }

    public ChannelData() {
        //Constructor
    }

    public ChannelData(ChannelData data) {
        //Constructor
        this.setSiteType(data.getSiteType());
        this.setQualityType(data.getQualityType());
        this.setAuthKey(data.getAuthkey());
        this.setId(data.getId());
        this.setCategoryId(data.getCategoryId());
        this.setTitle(data.getTitle());
        this.setProgramName(data.getProgramName());
        this.setStillImageUrl(data.getStillImageUrl());
        this.setAudioChannel(data.isAudioChannel());
        this.setFavorite(data.getFavorite());
        this.setItemIndex(data.getItemIndex());

        for(int i=0; i<data.getEPG().size(); i++) {
            this.mEPG.add(new EPGData(data.getEPG().get(i)));
        }
    }

    @Override
    public String toString() {
        return "ChannelData{" +
                "mSiteType=" + Integer.toString(mSiteType) +
                ", mQualityType=" + Integer.toString(mQualityType) +
                ", mAuthKey=" + mAuthKey +
                ", mId=" + mId +
                ", mCategoryId=" + Integer.toString(mCategoryId) +
                ", mTitle='" + mTitle + '\'' +
                ", mProgramName='" + mProgramName + '\'' +
                ", mStillImageUrl='" + mStillImageUrl + '\'' +
                ", mAudioChannel=" + mAudioChannel +
                ", mFavorite=" + mFavorite +
                ", mItemIndex=" + mItemIndex +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSiteType);
        dest.writeInt(this.mQualityType);
        dest.writeString(this.mAuthKey);
        dest.writeString(this.mId);
        dest.writeInt(this.mCategoryId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mProgramName);
        dest.writeString(this.mStillImageUrl);
        dest.writeByte((byte) (this.mAudioChannel ? 1 : 0));
        dest.writeInt(this.mFavorite);
        dest.writeInt(this.mItemIndex);
        dest.writeList(this.mEPG);
    }

    @SuppressWarnings("unchecked")
    protected ChannelData(Parcel in) {
        this.mSiteType = in.readInt();
        this.mQualityType = in.readInt();
        this.mAuthKey = in.readString();
        this.mId = in.readString();
        this.mCategoryId = in.readInt();
        this.mTitle = in.readString();
        this.mProgramName = in.readString();
        this.mStillImageUrl = in.readString();
        this.mAudioChannel = (in.readByte() != 0);
        this.mFavorite = in.readInt();
        this.mItemIndex = in.readInt();
        this.mEPG = in.readArrayList(EPGData.class.getClassLoader());
    }

    public static final Parcelable.Creator<ChannelData> CREATOR = new Parcelable.Creator<ChannelData>() {

        public ChannelData createFromParcel(Parcel source) {
            return new ChannelData(source);
        }

        public ChannelData[] newArray(int size) {
            return new ChannelData[size];
        }
    };
}

