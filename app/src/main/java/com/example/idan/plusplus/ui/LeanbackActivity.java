package com.example.idan.plusplus.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.app;

/**
 * This parent class contains common methods that run in every activity such as search_vod.
 */
public abstract class LeanbackActivity extends FragmentActivity {
    public static final String SHARED_ELEMENT_NAME = "hero";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(app.localeManager.setLocale(base));
    }


    @Override
    public boolean onSearchRequested() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       //Utils.disposedServices();
    }
}
