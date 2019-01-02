package com.vpapps.onlinemp3;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.vpapps.utils.Constant;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        hideStatusBar();
        try {
            Constant.isFromPush = getIntent().getExtras().getBoolean("ispushnoti", false);
        } catch (Exception e) {
            Constant.isFromPush = false;
        }
        try {
            Constant.isFromNoti = getIntent().getExtras().getBoolean("isnoti", false);
        } catch (Exception e) {
            Constant.isFromNoti = false;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!Constant.pushCID.equals("0")) {
                    Intent intent = new Intent(SplashActivity.this, SongByCatActivity.class);
                    intent.putExtra("isPush", true);
                    intent.putExtra("type", getString(R.string.categories));
                    intent.putExtra("id", Constant.pushCID);
                    intent.putExtra("name", Constant.pushCName);
                    startActivity(intent);
                    finish();
                } else if (!Constant.pushArtID.equals("0")) {
                    Intent intent = new Intent(SplashActivity.this, SongByCatActivity.class);
                    intent.putExtra("isPush", true);
                    intent.putExtra("type", getString(R.string.artist));
                    intent.putExtra("id", Constant.pushArtID);
                    intent.putExtra("name", Constant.pushArtNAME);
                    startActivity(intent);
                    finish();
                } else if (!Constant.pushAlbID.equals("0")) {
                    Intent intent = new Intent(SplashActivity.this, SongByCatActivity.class);
                    intent.putExtra("isPush", true);
                    intent.putExtra("type", getString(R.string.albums));
                    intent.putExtra("id", Constant.pushAlbID);
                    intent.putExtra("name", Constant.pushAlbNAME);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 800);
    }

    void hideStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}