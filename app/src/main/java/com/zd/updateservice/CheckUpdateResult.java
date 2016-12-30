package com.zd.updateservice;

import android.text.TextUtils;

/**
 * Created by ZhangDi on 2016/10/18.
 */
public class CheckUpdateResult {
    String msg = "";

    public CheckUpdateResult(String mm){
        this.msg = mm;
    }

    @Override
    public String toString() {
        return TextUtils.isEmpty(msg) ? "" : msg;
    }
}
