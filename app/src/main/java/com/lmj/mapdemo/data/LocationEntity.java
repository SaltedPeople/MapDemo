package com.lmj.mapdemo.data;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by TQ on 2017/11/8.
 */

public class LocationEntity implements Parcelable{

    //获取当前定位结果来源，如网络定位结果，详见定位类型表
    public int locationType;
    //获取纬度
    public double latitude;
    //获取经度
    public double longitude;
    //获取城市
    public String city;
    //城市code
    public String cityCode;
    //定位时间
    public String date;
    //定位地址
    public String address;
    //
    public String street;

    public boolean selected;

    public String title;


    public String province;

    public LocationEntity() {
    }

    protected LocationEntity(Parcel in) {
        locationType = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        city = in.readString();
        cityCode = in.readString();
        date = in.readString();
        address = in.readString();
        street = in.readString();
        selected = in.readByte() != 0;
        title = in.readString();
        province = in.readString();
    }

    public static final Creator<LocationEntity> CREATOR = new Creator<LocationEntity>() {
        @Override
        public LocationEntity createFromParcel(Parcel in) {
            return new LocationEntity(in);
        }

        @Override
        public LocationEntity[] newArray(int size) {
            return new LocationEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(locationType);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(city);
        parcel.writeString(cityCode);
        parcel.writeString(date);
        parcel.writeString(address);
        parcel.writeString(street);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeString(title);
        parcel.writeString(province);
    }
}
