package com.zd.updateservice;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Stack;

/**
 * Created by ZhangDi on 2016/10/14.
 * 当从服务器请求回更新结果时，会通知{@link Dispatcher}分派【结果】.
 * 创建的时候，就会启动线程.
 * 如果没有【结果】，或者 candidates为空，线程就进入等待状态.
 */
public class Dispatcher extends Thread {
    private final String TAG = getClass().getSimpleName();

    private boolean isTerminated = false;
    private final Stack<CheckUpdateListener> candidates = new Stack<>();
    private Connection mConnection;
    private Poster poster;

    public Dispatcher() {
        init();
        start();
    }

    private void init() {
        isTerminated = false;
        poster = new Poster(this);
        notifyAllWithSyncBlock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Log.e("TAGA", "dispatcher run");
        while (!isTerminated) {
            Log.e("TAGA", "dispatcher while run");
            synchronized (this) {
                if (candidates.isEmpty() || null == mConnection || null == mConnection.getResult()) {
                    Log.d(TAG, "Wait,this stack is empty or no result");
                    notifyAllWithSyncBlock();
                    waitWithSyncBlock();
                    continue;
                }
            }

            Log.e("TAGA", "run complete pre " + candidates.size());
            final CheckUpdateListener lis = candidates.pop();
            final Object result = mConnection.getResult();
            poster.post(new Runnable() {
                @Override
                public void run() {
                    // on main thread
                    if (lis != null && mConnection != null && lis.onCheckResult(result)) {
                        // 如果目标消费掉了Result,就通知回调
                        mConnection.onConsumed();
                    } else {
                        notifyAllWithSyncBlock();
                    }
                }
            });
            waitWithSyncBlock();
            Log.e("TAGA", "run complete " + lis + "," + candidates.size());
        }
    }

    public void enqueue(CheckUpdateListener listener) {
        if (null == listener) {
            return;
        }
        candidates.push(listener);
        notifyAllWithSyncBlock();
        Log.e("TAGA", "enqueue " + listener + "," + candidates.size());
        Log.e("TAGA", "enqueue " + getState());
    }

    public void remove(CheckUpdateListener listener) {
        boolean foundIt = candidates.removeElement(listener);
        if (!foundIt) {
            Log.d(TAG, "When removing, Target was not found");
        }
        Log.e("TAGA", "remove " + listener + "," + candidates.size());
    }

    /**
     * 终止派发
     */
    public void terminate() {
        isTerminated = true;
        mConnection = null;
        if (!candidates.isEmpty()) {
            candidates.clear();
        }
        poster = null;
        notifyAllWithSyncBlock();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setConnection(Connection connection) {
        this.mConnection = connection;
    }

    interface Connection<T> {
        public T getResult();

        /**
         * 当消费掉结果时，才会回调这个方法
         */
        public void onConsumed();
    }

    private static class Poster extends Handler {
        WeakReference<Dispatcher> refer;

        public Poster(Dispatcher dispatcher) {
            super(Looper.getMainLooper());
            refer = new WeakReference<Dispatcher>(dispatcher);
        }

        @Override
        public void dispatchMessage(Message msg) {
            if (refer.get().isTerminated) {
                Log.e("TAGA", "In post,dispatcher is canceled");
                return;
            }
            super.dispatchMessage(msg);
        }
    }

    private void waitWithSyncBlock() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyWithSyncBlock() {
        synchronized (this) {
            notify();
        }
    }

    public void notifyAllWithSyncBlock() {
        synchronized (this) {
            notifyAll();
        }
    }
}
