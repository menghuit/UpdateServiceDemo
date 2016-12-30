package com.zd.updateservice;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceConnHelper implements ServiceConnection {
    private boolean attemptingToBind = false;
    private boolean bounded = false;

    private final Context context;
    private final Class<? extends Service> service;
    private ServiceConnection delegateConn;
    private int bindFlags;

    ServiceConnHelper(Context context, Class<? extends Service> service, ServiceConnection delegate, int bindFlags) {
        this.context = context;
        this.service = service;
        this.delegateConn = delegate;
        this.bindFlags = bindFlags;
    }

    public void bindToService() {
        if (!attemptingToBind) {
            attemptingToBind = true;
            context.bindService(new Intent(context, service), this, bindFlags);
        }
    }

    public void unbindFromService() {
        attemptingToBind = false;
        if (bounded) {
            context.unbindService(this);
            bounded = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        attemptingToBind = false;
        bounded = true;
        if (delegateConn != null) {
            delegateConn.onServiceConnected(componentName, iBinder);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        bounded = false;
        if (delegateConn != null) {
            delegateConn.onServiceDisconnected(componentName);
        }
    }

    public static final class Builder {
        private Context context;
        private Class<? extends Service> service;
        private ServiceConnection connection;
        private int bindFlags;

        public Builder service(Class<? extends Service> service) {
            this.service = checkNotNull(service, "Builder service is null");
            return this;
        }

        public Builder serviceConnection(ServiceConnection conn) {
            this.connection = checkNotNull(conn, "Builder ServiceConnection is null");
            return this;
        }

        public Builder flags(int flags) {
            this.bindFlags = flags;
            return this;
        }

        public ServiceConnHelper build(Context ctx) {
            this.context = checkNotNull(ctx, "Builder context is null");
            return new ServiceConnHelper(context, service, connection, bindFlags);
        }

        static <T> T checkNotNull(T object, String message) {
            if (object == null) {
                throw new NullPointerException(message);
            }
            return object;
        }
    }
}