package com.zd.updateservicedemo;

import android.app.Application;
import android.content.Intent;

import com.zd.updateservice.UpdateServiceImpl;

/**
 * Created by ZhangDi on 2016/10/18.
 */
public class MApp extends Application {

    private static MApp mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;

        Intent it = new Intent(this, UpdateServiceImpl.class);
        startService(it);
    }

    public static MApp getContext(){
        return mApp;
    }
}
