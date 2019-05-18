package com.zd.updateservice;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ZhangDi on 2017/1/5.
 */
public class ClientOption implements Parcelable{
    // True,Service就不会把结果派发给在其以下的Client了
    private boolean selfish = false;
    // 是否要忽略已缓存的Result
    private boolean ignoreCachedResult = false;

    public void copyFrom(ClientOption otherOption) {
        if(null == otherOption) {
            return;
        }
        selfish = otherOption.selfish;
        ignoreCachedResult = otherOption.ignoreCachedResult;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.selfish ? (byte) 1 : (byte) 0);
        dest.writeByte(this.ignoreCachedResult ? (byte) 1 : (byte) 0);
    }

    public ClientOption() {}

    protected ClientOption(Parcel in) {
        this.selfish = in.readByte() != 0;
        this.ignoreCachedResult = in.readByte() != 0;
    }

    public static final Creator<ClientOption> CREATOR = new Creator<ClientOption>() {
        @Override
        public ClientOption createFromParcel(Parcel source) {
            return new ClientOption(source);
        }

        @Override
        public ClientOption[] newArray(int size) {
            return new ClientOption[size];
        }
    };

    // *******************Getter/Setter*************************
    public boolean isSelfish() {
        return selfish;
    }

    public void setSelfish(boolean selfish) {
        this.selfish = selfish;
    }

    public boolean isIgnoreCachedResult() {
        return ignoreCachedResult;
    }

    public void setIgnoreCachedResult(boolean ignoreCachedResult) {
        this.ignoreCachedResult = ignoreCachedResult;
    }
    // *******************Getter/Setter*************************

    @Override
    public String toString() {
        return "ClientOption{" +
                "selfish=" + selfish +
                ", ignoreCachedResult=" + ignoreCachedResult +
                '}';
    }
}
