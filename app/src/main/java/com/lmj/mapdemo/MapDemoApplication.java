package com.lmj.mapdemo;

import android.app.Application;


public class MapDemoApplication extends Application{
    private static Application mContext = null;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    /*
     * 获取全局Application
     */
    public static Application getContext() {
        return mContext;
    }
}
