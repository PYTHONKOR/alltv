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


public class ChannelData implements Parcelable {

    public enum QualityType {Auto, FullHd, Hd, Sd}

    private String mId;
    private int mCategoryId;
    private String mTitle;
    private String mProgram;
    private String mStillImageUrl;
    private String[] mVideoUrl = new String[QualityType.values().length];
    private Boolean mAudioChannel = false;

    public String getId() {
        return mId;
    }

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

    public String getProgram() {
        return mProgram;
    }

    public void setProgram(String program) {
        this.mProgram = program;
    }

    public String getVideoUrl() {
        return mVideoUrl[QualityType.Auto.ordinal()];
    }

    public void setVideoUrl(String VideoUrl) {
        this.mVideoUrl[QualityType.Auto.ordinal()] = VideoUrl;
    }

    public String getStillImageUrl() {
        return mStillImageUrl;
    }

    public void setStillImageUrl(String imageUrl) {
        this.mStillImageUrl = imageUrl;
    }

    public Boolean isAudioChannel() { return mAudioChannel; }

    public void setAudioChannel(Boolean setAudio) { this.mAudioChannel = setAudio; }

    public ChannelData() {
        //Constructor
    }

    @Override
    public String toString() {
        return "ChannelData{" +
                "mId=" + mId +
                "mCategoryId=" + mCategoryId +
                ", mTitle='" + mTitle + '\'' +
                ", mProgram='" + mProgram + '\'' +
                ", mStillImageUrl='" + mStillImageUrl + '\'' +
                ", mVideoUrl='" + mVideoUrl.toString() + '\'' +
                "mAudioChannel=" + mAudioChannel +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeInt(this.mCategoryId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mProgram);
        dest.writeString(this.mStillImageUrl);
        dest.writeStringArray(this.mVideoUrl);
        dest.writeByte((byte) (this.mAudioChannel ? 1 : 0));
    }

    protected ChannelData(Parcel in) {
        this.mId = in.readString();
        this.mCategoryId = in.readInt();
        this.mTitle = in.readString();
        this.mProgram = in.readString();
        this.mStillImageUrl = in.readString();
        in.readStringArray(this.mVideoUrl);
        this.mAudioChannel = in.readByte() != 0;
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