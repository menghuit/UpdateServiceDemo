package com.zd.updateservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by ZhangDi on 2016/11/10.
 */
public class UpdateClient {
    private static final int CLINET_MSG_FIRST = 1000;
    private static final int MSG_START = CLINET_MSG_FIRST + 1;
    private static final int MSG_STOP = CLINET_MSG_FIRST + 2;
    private static final int MSG_REQUEST_CHECK = CLINET_MSG_FIRST + 3;
    private static final int MSG_REGISTER_CHECK_LISTENER = CLINET_MSG_FIRST + 4;
    private static final int MSG_UNREGISTER_CHECK_LISTENER = CLINET_MSG_FIRST + 5;

    private Context ctx;
    private Class<? extends UpdateService> targetService;
    private List<CheckUpdateListener> checkListeners = new ArrayList<>();

    private ServiceConnHelper mConnHelper;
    private boolean isStarted = false;

    private Messenger mServiceMessenger;
    private InnerHandler mClientHandler = new InnerHandler(this);
    private Messenger mClientMessenger = new Messenger(mClientHandler);

    public UpdateClient(Context ctx, Class<? extends UpdateService> cls){
        if(null == ctx || null == cls){
            throw new IllegalArgumentException("Argument is null");
        }
        this.ctx = ctx;
        targetService = cls;

        mConnHelper = new ServiceConnHelper.Builder()
                .service(targetService)
                .serviceConnection(new UpdateServiceConn(this))
                .flags(Context.BIND_AUTO_CREATE)
                .build(ctx);
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void start(){
        Message msg = mClientHandler.obtainMessage(MSG_START);
        msg.sendToTarget();
    }

    private void onStart() {
        mConnHelper.bindToService();
        isStarted = true;
    }

    public void stop(){
        Message msg = mClientHandler.obtainMessage(MSG_STOP);
        msg.sendToTarget();
    }

    private void onStop() {
        mConnHelper.unbindFromService();
        isStarted = false;

        checkListeners.clear();
        checkListeners = null;
    }

    public void registerCheckListener(CheckUpdateListener lis) {
        if(null == lis){
            return;
        }
        Message msg = mClientHandler.obtainMessage(MSG_REGISTER_CHECK_LISTENER);
        msg.obj = lis;
        msg.sendToTarget();
    }

    private void onRegisterCheckListener(Message msg) {
        CheckUpdateListener lis = (CheckUpdateListener) msg.obj;
        if(null == checkListeners){
            checkListeners = new ArrayList<>();
        }
        if(!checkListeners.contains(lis)) {
            checkListeners.add(lis);
        }
    }

    public void unregisterCheckListener(CheckUpdateListener lis){
        if(null == lis) {
            return;
        }
        Message msg = mClientHandler.obtainMessage(MSG_UNREGISTER_CHECK_LISTENER);
        msg.obj = lis;
        msg.sendToTarget();
    }

    private void onUnregisterCheckListener(Message msg){
        CheckUpdateListener lis = (CheckUpdateListener) msg.obj;
        if(null == checkListeners){
            checkListeners = new ArrayList<>();
        }
        if(!checkListeners.contains(lis)) {
            checkListeners.remove(lis);
        }
    }

    public void requestCheck(){
        Message msg = mClientHandler.obtainMessage(MSG_REQUEST_CHECK);
        msg.sendToTarget();
    }

    private void onRequestCheck() {
        try {
            Message msg = Message.obtain(null, UpdateService.MSG_REQUEST_CHECK);
            msg.replyTo = mClientMessenger;
            // TODO 这里可以传给service一些参数
            msg.setData(new Bundle());
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setServiceMessenger(Messenger serviceMessenger){
        mServiceMessenger = serviceMessenger;
    }

    private void onGetCheckResult(Object result){
        if(null == checkListeners || checkListeners.isEmpty()){
            return;
        }
        Log.e("TAGA", "onGetCheckResult:"+result+","+checkListeners.size());
        Iterator<CheckUpdateListener> iterator = checkListeners.iterator();
        while (iterator.hasNext()) {
            CheckUpdateListener listener = iterator.next();
            listener.onCheckResult(result);
        }
    }

    /**
     * Client 内部 Handler
     * 大部分对外暴露的方法，都用Handler处理
     */
    private static class InnerHandler extends Handler {
        WeakReference<UpdateClient> client;
        InnerHandler(UpdateClient client){
            this.client = new WeakReference<UpdateClient>(client);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateClient c = client.get();
            if(null == c) {
                return;
            }
            Log.e("TAGA", "msg.what:"+msg.what);
            switch (msg.what){
                case MSG_START:
                    c.onStart();
                    break;
                case MSG_STOP:
                    c.onStop();
                    break;
                case MSG_REQUEST_CHECK:
                    c.onRequestCheck();
                    break;
                case UpdateService.MSG_CHECK_RESULT:
                    Object obj = msg.obj;
                    c.onGetCheckResult(obj);
                    break;
                case MSG_REGISTER_CHECK_LISTENER:
                    c.onRegisterCheckListener(msg);
                    break;
                case MSG_UNREGISTER_CHECK_LISTENER:
                    c.onUnregisterCheckListener(msg);
                    break;
            }
        }
    }

    private static class UpdateServiceConn implements ServiceConnection {

        private UpdateClient client;

        UpdateServiceConn(UpdateClient client){
            if(null == client){
                throw new IllegalArgumentException("Client is null");
            }
            this.client = client;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            client.setServiceMessenger(new Messenger(service));
            client.isStarted = true;

            client.requestCheck();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            client.setServiceMessenger(null);
            client.isStarted = false;
        }
    }
}
