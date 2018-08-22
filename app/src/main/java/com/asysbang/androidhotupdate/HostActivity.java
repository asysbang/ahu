package com.asysbang.androidhotupdate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.asysbang.androidhotupdate.demo.DevideByZero;
import com.asysbang.androidhotupdate.update.DexUpdateManager;

public class HostActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        mBtn = findViewById(R.id.host_btn);
        mBtn.setOnClickListener(this);
        requestPermission();
        updateDexToFixBug();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void updateDexToFixBug() {
        String dataPath = getApplicationInfo().dataDir;
        Log.e("====", "==dataPath==" + dataPath);
        String path = "/data/data/com.asysbang.androidhotupdate";
        try {
            DexUpdateManager.loadPatchDex(this, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        DevideByZero.test();
    }


}
