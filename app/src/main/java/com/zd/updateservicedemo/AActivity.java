package com.zd.updateservicedemo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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

public class AActivity extends AppCompatActivity {

    @BindView(R.id.start)
    Button startBtn;
    @BindView(R.id.to_next)
    Button bToB;

    CheckUpdateListener<CheckUpdateResult> listener = new CheckUpdateListener<CheckUpdateResult>() {
        @Override
        public boolean onCheckResult(CheckUpdateResult result) {
            Log.e("TAGA", AActivity.this.getClass().getSimpleName() + " " + (result == null ? "null" : result.toString()));

            AlertDialog.Builder customBuilder = new AlertDialog.Builder(AActivity.this);
            customBuilder.setTitle("Title");
            customBuilder.setMessage("message "+ result.toString());
            customBuilder.setNegativeButton("cancel", null);
            Dialog dialog = customBuilder.create();
            dialog.show();

            return true;
        }
    };
    UpdateClient client = new UpdateClient(AActivity.this, UpdateServiceImpl.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("A-Activity");

        Log.e("TAGA", "activity onCreate");
    }

    @OnClick({R.id.start, R.id.to_next, R.id.stop, R.id.destroyService})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.start:
                LogUtils.e(" TAGA ");
                client.start();
                client.registerCheckListener(listener);
                break;
            case R.id.stop:
                client.stop();
            break;
            case R.id.to_next:
                Intent itB = new Intent(this, BActivity.class);
                startActivity(itB);
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
