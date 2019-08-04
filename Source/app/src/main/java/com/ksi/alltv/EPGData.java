package com.ksi.alltv;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class EPGData implements Parcelable {

    private String mProgramName = "";
    private Date mStartTime = null;
    private Date mEndTime = null;
    private Boolean mAdultContent = false;

    public String getProgramName() {
        return mProgramName;
    }
    public void setProgramName(String name) {
        this.mProgramName = name;
    }

    public Date getStartTime() {
        return mStartTime;
    }
    public void setStartTime(Date date) {
        this.mStartTime = date;
    }

    public Date getEndTime() {
        return mEndTime;
    }
    public void setEndTime(Date date) {
        this.mEndTime = date;
    }

    public Boolean isAdultContent() {
        return mAdultContent;
    }
    public void setAdultContent(Boolean value) {
        this.mAdultContent = value;
    }


    public EPGData() {
        //Constructor
    }

    public EPGData(EPGData data) {
        //Constructor
        this.setProgramName(data.getProgramName());
        this.setStartTime(data.getStartTime());
        this.setEndTime(data.getEndTime());
        this.setAdultContent(data.isAdultContent());
    }

    public EPGData(String name, Date stime, Date etime, Boolean adult) {
        //Constructor
        this.setProgramName(name);
        this.setStartTime(stime);
        this.setEndTime(etime);
        this.setAdultContent(adult);
    }

    @Override
    public String toString() {
        return "EPGData{" +
                "mProgramName=" + mProgramName +
                ", mStartTime=" + mStartTime.toString() +
                ", mEndTime=" + mEndTime.toString() +
                ", mAdultContent=" + mAdultContent +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mProgramName);
        dest.writeSerializable(this.mStartTime);
        dest.writeSerializable(this.mEndTime);
        dest.writeByte((byte) (this.mAdultContent ? 1 : 0));
    }

    protected EPGData(Parcel in) {
        this.mProgramName = in.readString();
        this.mStartTime = (Date)in.readSerializable();
        this.mEndTime = (Date)in.readSerializable();
        this.mAdultContent = (in.readByte() != 0);
    }

    public static final Parcelable.Creator<EPGData> CREATOR = new Parcelable.Creator<EPGData>() {

        public EPGData createFromParcel(Parcel source) {
            return new EPGData(source);
        }

        public EPGData[] newArray(int size) {
            return new EPGData[size];
        }
    };

}
