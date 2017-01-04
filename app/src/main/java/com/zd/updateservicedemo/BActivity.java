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
import com.zd.updateservice.UpdateClient;
import com.zd.updateservice.UpdateServiceImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BActivity extends AppCompatActivity {

    @BindView(R.id.start)
    Button startBtn;
    @BindView(R.id.to_next)
    Button bToB;
    Button bToA;

    CheckUpdateListener<CheckUpdateResult> listener = new CheckUpdateListener<CheckUpdateResult>() {
        @Override
        public boolean onCheckResult(CheckUpdateResult result) {
            Log.e("TAGA", BActivity.this.getClass().getSimpleName() + " " + (result == null ? "null" : result.toString()));
            AlertDialog.Builder customBuilder = new AlertDialog.Builder(BActivity.this);
            customBuilder.setTitle("Btitle");
            customBuilder.setMessage("Bmessage " + result.toString());
            customBuilder.setNegativeButton("Bcancel", null);
            Dialog dialog = customBuilder.create();
            dialog.show();
            return true;
        }
    };
    UpdateClient client = new UpdateClient(BActivity.this, UpdateServiceImpl.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
        getSupportActionBar().setTitle("B-Activity");
        ButterKnife.bind(this);
        bToA.setText("to A");
    }

    @OnClick({R.id.start, R.id.to_next, R.id.stop, R.id.destroyService})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.start:
                LogUtils.e(" TAGA ");
                client.start();
                client.registerCheckListener(listener);
                break;
            case R.id.to_next:
                Intent itB = new Intent(this, AActivity.class);
                itB.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(itB);
                break;
            case R.id.stop:
                client.stop();
                break;
            case R.id.destroyService:
                client.requestCheck();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
