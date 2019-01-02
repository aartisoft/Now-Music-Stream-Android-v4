package com.vpapps.onlinemp3;

import android.app.Application;
import android.os.StrictMode;

import com.onesignal.OneSignal;
import com.vpapps.utils.DBHelper;

import java.io.IOException;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/poppins_reg.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        OneSignal.startInit(getApplicationContext()).init();

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        try {
            dbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}