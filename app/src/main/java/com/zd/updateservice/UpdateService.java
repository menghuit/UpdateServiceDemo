package com.zd.updateservice;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Random;

/**
 * Created by ZhangDi on 2016/10/14.
 */
public class UpdateService extends AbstractUpdateService<CheckUpdateResult> {
    private static final String TAG = "UpdateService";
    private static final long CHECK_UPDATE_TIME = 1 * 24 * 3600 * 1000; //1 day

    @Override
    public CheckUpdateResult call() throws Exception {
        getSystemService(ACTIVITY_SERVICE);
        // TODO:根据具体业务修改
        Random random = new Random();
        int time = /*random.nextInt(10*1000) + 5000*/1000 * 18;
        Log.d(TAG, "checkTask doInBackground " + time);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.e("TAGA", "call after sleep");
        try{
            ConnectivityManager cm=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo curNetInfo = cm.getActiveNetworkInfo();
            if(curNetInfo == null || !curNetInfo.isAvailable()){
                return null;
            }
        }catch (Exception e){
            return null;
        }
        return new CheckUpdateResult("have update");
    }

    @Override
    protected boolean needCheckUpdate() {
        // TODO：检查时间间隔是否达到
        //      status  status_check_done || status_idle;

        return super.needCheckUpdate();
    }

    @Override
    public void onConsumed() {
        super.onConsumed();

    }
}