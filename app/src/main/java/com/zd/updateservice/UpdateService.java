package com.zd.updateservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

/**
 * Created by ZhangDi on 2016/10/14.
 * 检查更新的服务,有结果就会派发出去
 * 创建就开始请求检查更新
 */
public abstract class UpdateService<T> extends Service implements Callable<T>, Dispatcher.Connection<T> {
    private static final String TAG = "UpdateService";

    public static final int SERVICE_MSG_FIRST = 2000;
    public static final int MSG_REQUEST_CHECK = SERVICE_MSG_FIRST + 1;
    public static final int MSG_CHECK_RESULT = SERVICE_MSG_FIRST + 2;
    public static final int MSG_STOP_CLIENT = SERVICE_MSG_FIRST + 3;

    public static final int status_idle = 0;
    public static final int status_checking = 1;
    public static final int status_check_done = 2;
    public static final int status_posted = 3;

    protected int status = status_idle;
    protected final Messenger messenger = new Messenger(new InnerHandler(this));
    private CheckTask<T> checkTask = new CheckTask<>(this);
    private Dispatcher mDispatcher;
    private T result;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

//        registerNetReceiver();

        if(!needCheckUpdate()){
            return;
        }
        mDispatcher = new Dispatcher();
        mDispatcher.setConnection(this);
        // 开启检查更新的任务
        checkTask.execute();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
//        unregisterNetReceiver();
        checkTask.cancel(true);
        mDispatcher.terminate();
        mDispatcher = null;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public void onConsumed() {
        Log.d(TAG, "service onConsumed "+ mDispatcher);
        // 当检查更新的结果 已经分派通知到目标，消费掉之后，就把 Dispatcher 停掉
        if(mDispatcher != null){
            mDispatcher.terminate();
        }
        // 注意：此处可能没有完全结束掉service。
        // 因为这个service是在Application中通过startService启动的，然后Client通过 bindService绑定
        // 所以如果有绑定的Client，仅stopService 不能完全停掉Service，还需所有的Client与Service 解绑
        stopSelf();
    }

    /**
     * 是否需要检查更新
     * @return True 需要检查
     */
    protected boolean needCheckUpdate() {
        return true;
    }

    private void restartCheckTask(){
        if(checkTask != null && !checkTask.isFinished()){
            checkTask.cancel(true);
        }
        checkTask = new CheckTask<>(this);
        checkTask.execute();
    }

    /**
     * 从服务器获取检查更新结果
     */
    private static class CheckTask<Result> extends AsyncTask<Void, Void, Result> {
        WeakReference<UpdateService> refer;

        CheckTask(UpdateService service){
            refer = new WeakReference<UpdateService>(service);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Result doInBackground(Void... params) {
            refer.get().status = status_checking;
            try {
                Callable<Result> c = refer.get();
                return c.call();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            Log.d(TAG, "checkTask onPostExecute");
            refer.get().status = status_check_done;
            refer.get().result = result;

            Log.d(TAG, "onPost:"+refer.get().mDispatcher.getState());
            refer.get().mDispatcher.notifyWithSyncBlock();
        }

        public boolean isPending(){
            return getStatus() == Status.PENDING;
        }

        public boolean isFinished(){
            return getStatus() == Status.FINISHED;
        }
    };

    private final static class InnerHandler extends Handler {
        WeakReference<UpdateService> service;

        InnerHandler(UpdateService service){
            this.service = new WeakReference<UpdateService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_REQUEST_CHECK:
                    service.get().mDispatcher.enqueue(msg.replyTo);
                    break;
                case MSG_STOP_CLIENT:
                    service.get().mDispatcher.remove(msg.replyTo);
                    break;
                default:
                    break;
            }
        }
    }

//    private void registerNetReceiver(){
//        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        this.registerReceiver(netStateReceiver, filter);
//    }
//
//    private void unregisterNetReceiver(){
//        unregisterReceiver(netStateReceiver);
//    }
//
//    private BroadcastReceiver netStateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            ConnectivityManager cm=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo curNetInfo = cm.getActiveNetworkInfo();
//
////            Log.e("TAGA", "***************************");
////            Log.e("TAGA", "intent:"+intent.toString()+",action:"+intent.getAction());
////            Log.e("TAGA", "cur:"+(curNetInfo==null ? "null":curNetInfo.toString()));
//
//            boolean isAvailable = curNetInfo != null && curNetInfo.isAvailable();
////            Log.e("TAGA", "isAvailable:"+isAvailable+",status:"+status+",result:"+result);
//            if(isAvailable && status == status_check_done && result == null){
////                Log.e("TAGA", "restart check task");
//                restartCheckTask();
//            }
//        }
//    };
}