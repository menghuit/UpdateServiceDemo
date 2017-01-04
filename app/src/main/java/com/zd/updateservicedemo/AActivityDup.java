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
import com.zd.updateservice.ServiceConnHelper;
import com.zd.updateservice.UpdateServiceImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AActivityDup extends AppCompatActivity {

    @BindView(R.id.start)
    Button bind;
    @BindView(R.id.to_next)
    Button bToB;

    CheckUpdateListener<CheckUpdateResult> listener = new CheckUpdateListener<CheckUpdateResult>() {
        @Override
        public boolean onCheckResult(CheckUpdateResult result) {
            Log.e("TAGA", AActivityDup.this.getClass().getSimpleName() + " " + (result == null ? "null" : result.toString()));
            bind.setText(result.toString());

            AlertDialog.Builder customBuilder = new AlertDialog.Builder(AActivityDup.this);
            customBuilder.setTitle("Title");
            customBuilder.setMessage("message");
            customBuilder.setNegativeButton("cancel", null);
            Dialog dialog = customBuilder.create();
            dialog.show();

            return true;
        }
    };
    ServiceConnHelper scm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
        ButterKnife.bind(this);
        bind.setText(this.getClass().getSimpleName()+ "startBtn");

        scm = new ServiceConnHelper.Builder()
                .service(UpdateServiceImpl.class)
                .serviceConnection(conn)
                .flags(Context.BIND_AUTO_CREATE)
                .build(this);

        Log.e("TAGA", "activity onCreate");
    }

    @OnClick({R.id.start, R.id.to_next, R.id.stop, R.id.destroyService})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.start:
                LogUtils.e(" TAGA ");
                scm.bindToService();
                break;
            case R.id.to_next:
                Intent itB = new Intent(this, BActivity.class);
                startActivity(itB);
                break;
            case R.id.stop:
                scm.unbindFromService();
                break;
            case R.id.destroyService:
                stopService(new Intent(this, UpdateServiceImpl.class));
                break;
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            mBinder = (UpdateServiceImpl.UpdateBinder) service;
//            mBinder.registerUpdateListener(listener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            mBinder.unregisterUpdateListener(listener);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
