package com.asysbang.serviceplugin;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     *
     */
    private void getPackageInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SERVICES);
            ServiceInfo[] services = packageInfo.services;
            if (null == services ) {
                Log.d(TAG, "no service"  );
            } else {
                Log.d(TAG, "getPackageInfo : " + services.length);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPackageInfo();
    }

    public void showNotification(View v) {
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(ServiceHookHelper.EXTRA_INTENT_HOOK, ServiceHookHelper.EXTRA_INTENT_HOOK);
        startService(intent);
    }

    public void startHideActivity(View v) {
        try {
            Intent intent = new Intent(this, HideActivity.class);
            intent.putExtra(ActivityHookHelper.EXTRA_INTENT_HOOK, ActivityHookHelper.EXTRA_INTENT_HOOK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this,"ActivityNotFoundException",Toast.LENGTH_SHORT).show();
        }
    }

    public void hookActivity(View v) {
        ActivityHookHelper.getInstance().hookStartActivity(getApplicationContext());
    }

    public void test(View v) {
        ServiceHookHelper.getInstance().hookStartService(this);
    }

}
