package com.example.idan.plusplus;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.example.idan.plusplus.Classes.LocaleManager;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.DaggerIRetrofitServices;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.IRetrofitServices;

public class app extends Application {
    private final String TAG = "DEBUG";

    // for the sake of simplicity. use DI in real apps instead
    public static LocaleManager localeManager;
    private static IRetrofitServices sRetrofitServices;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.setAppContext(getApplicationContext());
        sRetrofitServices = DaggerIRetrofitServices.create();

    }

    @Override
    protected void attachBaseContext(Context base) {
        localeManager = new LocaleManager(base);
        localeManager.setNewLocale(base,"iw");
        super.attachBaseContext(localeManager.setLocale(base));
        Log.d(TAG, "attachBaseContext");
       // ACRA.init(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localeManager.setLocale(this);
        Log.d(TAG, "onConfigurationChanged: " + newConfig.locale.getLanguage());
    }

    public static IRetrofitServices getsRetrofitServices() {
        return sRetrofitServices;
    }

}
