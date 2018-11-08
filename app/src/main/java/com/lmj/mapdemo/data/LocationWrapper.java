package com.lmj.mapdemo.data;

import java.util.ArrayList;
import java.util.List;

/**
 * author: lmj
 * date  : 2018/8/14.
 */

public class LocationWrapper  {

    private List<LocationEntity> mList = new ArrayList<>();

    private String mCityCode;

    private String mKeyWord;

    //获取纬度
    private double mLatitude;
    //获取经度
    private double mLongitude;

    public boolean atLastPage = false;

    public String url;

    public int currentPage;

    public List<LocationEntity> getDataList() {
        return mList;
    }

    public void setDataList(List<LocationEntity> list) {
        mList = list;
    }

    public String getCityCode() {
        return mCityCode;
    }

    public void setCityCode(String cityCode) {
        mCityCode = cityCode;
    }

    public String getKeyWord() {
        return mKeyWord;
    }

    public void setKeyWord(String keyWord) {
        mKeyWord = keyWord;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
}
