package com.zd.updateservicedemo;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.zd.updateservice.CheckUpdateListener;
import com.zd.updateservice.CheckUpdateResult;
import com.zd.updateservice.UpdateService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BActivity extends AppCompatActivity {

    @BindView(R.id.bind)
    Button bind;
    @BindView(R.id.to_next)
    Button bToA;

    boolean isBinded = false;
    UpdateService.UpdateBinder mBinder;
    CheckUpdateListener<CheckUpdateResult> listener = new CheckUpdateListener<CheckUpdateResult>() {
        @Override
        public boolean onCheckResult(CheckUpdateResult result) {
            Log.e("TAGA", BActivity.this.getClass().getSimpleName() + " " + (result == null ? "null" : result.toString()));
            bind.setText(result.toString());

            AlertDialog.Builder customBuilder = new AlertDialog.Builder(BActivity.this);
            customBuilder.setTitle("Btitle");
            customBuilder.setMessage("Bmessage");
            customBuilder.setNegativeButton("Bcancel", null);
            Dialog dialog = customBuilder.create();
            dialog.show();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
        ButterKnife.bind(this);
        bind.setText(this.getClass().getSimpleName());
        bToA.setText("to A");
    }

    @OnClick({R.id.bind, R.id.to_next, R.id.ubbind, R.id.destroyService})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.bind:
                Intent it = new Intent(this, UpdateService.class);
                this.bindService(it, conn, Context.BIND_AUTO_CREATE);
                break;
            case R.id.to_next:
                Intent itB = new Intent(this, AActivity.class);
                itB.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(itB);
                break;
            case R.id.ubbind:
                unbindService(conn);
                break;
            case R.id.destroyService:
//                unbindService(conn);
                stopService(new Intent(this, UpdateService.class));
                break;
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBinded = true;
            mBinder = (UpdateService.UpdateBinder) service;
            mBinder.registerUpdateListener(listener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBinded = false;
            mBinder.unregisterUpdateListener(listener);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        if(isBinded){
            mBinder.unregisterUpdateListener(listener);
            super.unbindService(conn);
            isBinded = false;
        }else{
            Log.d("TAGA", "no bind");
        }
    }
}
