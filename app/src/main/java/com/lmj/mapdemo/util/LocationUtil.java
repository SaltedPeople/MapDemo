package com.lmj.mapdemo.util;

import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.lmj.mapdemo.MapDemoApplication;
import com.lmj.mapdemo.data.LocationEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by TQ on 2017/11/8.
 */

public class LocationUtil implements AMapLocationListener {

    //声明mLocationClient对象
    private AMapLocationClient mLocationClient;

    private LocationCallBack mLocationCallBack;

    private static volatile LocationUtil instance;

    public static LocationUtil getInstance() {
        if (instance == null) {
            synchronized (LocationUtil.class) {
                if (instance == null) {
                    instance = new LocationUtil();
                }
            }
        }
        return instance;
    }

    private LocationUtil() {
        mLocationClient = new AMapLocationClient(MapDemoApplication.getContext());
        //初始化定位参数
        //声明mLocationOption对象
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        //设置定位监听
        mLocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        locationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        locationOption.setOnceLocation(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        locationOption.setMockEnable(false);
        //设置定位参数
        mLocationClient.setLocationOption(locationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

    }

    public void startLocation(LocationCallBack callBack){
        this.mLocationCallBack = callBack;
        //启动定位
        if(mLocationClient.isStarted()){
            mLocationClient.stopLocation();
        }
        mLocationClient.startLocation();
    }

    /**
     *   amapLocation.getLocationType();*获取当前定位结果来源，如网络定位结果，详见定位类型表
     *   amapLocation.getLatitude();*获取纬度
     *   amapLocation.getLongitude();*获取经度
     *   amapLocation.getAccuracy();*获取精度信息
     *   amapLocation.getAddress();*地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
     *   amapLocation.getCountry();*国家信息
     *   amapLocation.getProvince();*省信息
     *   amapLocation.getCity();*城市信息
     *   amapLocation.getDistrict();*城区信息
     *   amapLocation.getStreet();*街道信息
     *   amapLocation.getStreetNum();*街道门牌号信息
     *   amapLocation.getCityCode();*城市编码
     *   amapLocation.getAdCode();*地区编码
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                //Log.i("caicai","纬度：" + amapLocation.getLatitude() + " 经度：" + amapLocation.getLongitude());
                LocationEntity locationBean = new LocationEntity();
                locationBean.title = amapLocation.getPoiName();
                locationBean.locationType = amapLocation.getLocationType();
                locationBean.latitude = amapLocation.getLatitude();
                locationBean.longitude = amapLocation.getLongitude();
                locationBean.address = amapLocation.getAddress();
                locationBean.city = amapLocation.getCity();
                locationBean.cityCode = amapLocation.getCityCode();
                locationBean.street = amapLocation.getStreet();
                locationBean.province = amapLocation.getProvince();
                locationBean.date = df.toString();
                mLocationCallBack.onLocationSuccess(locationBean);
            } else {
                mLocationCallBack.onLocationError();
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("mapDemo","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    public void release() {
        if (instance != null){
            instance = null;
        }
        if (mLocationClient != null){
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    public interface LocationCallBack{

        void onLocationSuccess(LocationEntity locationBean);
        void onLocationError();
    }

}
