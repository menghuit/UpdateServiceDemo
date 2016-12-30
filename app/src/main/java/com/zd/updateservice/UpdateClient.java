package com.zd.updateservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.lang.ref.WeakReference;

/**
 * Created by ZhangDi on 2016/11/10.
 */
public class UpdateClient {

    private Context ctx;
    private Class<? extends AbstractUpdateService> targetService;
    private Messenger mServiceMessenger;
    private ServiceConnHelper mConnHelper;
    private boolean isStarted = false;

    public UpdateClient(Context ctx, Class<? extends AbstractUpdateService> cls){
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

    public void start(){
        onStart();
    }

    private void onStart() {
        mConnHelper.bindToService();
        isStarted = true;
    }

    public void stop(){
        onStop();
    }

    private void onStop() {
        mConnHelper.unbindFromService();
        isStarted = false;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void requestCheck(){
//        Intent it = new Intent(ctx, targetService);
//        ctx.startService(it);
        //TODO: no idea
    }

    public void registerCheckListener(CheckUpdateListener lis) {
        Message msg = Message.obtain(null, AbstractUpdateService.MSG_REGISTER_CHECK_LISTENER, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterCheckListener(CheckUpdateListener lis){
        Message msg = Message.obtain(null, AbstractUpdateService.MSG_UNREGISTER_CHECK_LISTENER, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void setServiceMessenger(Messenger serviceMessenger){
        mServiceMessenger = serviceMessenger;
    }

    private static class InnerHandler extends Handler {
        WeakReference<UpdateClient> client;
        InnerHandler(UpdateClient client){
            this.client = new WeakReference<UpdateClient>(client);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            client.setServiceMessenger(null);
        }
    }
}
